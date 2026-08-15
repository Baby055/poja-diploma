package hei.poja.io.service;

import hei.poja.io.exception.NotFoundException;
import hei.poja.io.file.bucket.BucketComponent;
import hei.poja.io.mail.Email;
import hei.poja.io.mail.Mailer;
import hei.poja.io.mapper.CourseMapper;
import hei.poja.io.mapper.StudentMapper;
import hei.poja.io.model.Course;
import hei.poja.io.model.Role;
import hei.poja.io.model.Student;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.StudentRepository;
import hei.poja.io.security.AppUserDetails;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
public class TranscriptService {
  private final TemplateEngine templateEngine;
  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;
  private final CourseAverageService courseAverageService;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;
  private final StudentMapper studentMapper;
  private final CourseMapper courseMapper;

  public void assertCanRequestTranscript(UUID studentId, AppUserDetails principal) {
    boolean isStaff = principal.hasRole(Role.TEACHER) || principal.hasRole(Role.ADMIN);
    if (!isStaff && !principal.getId().equals(studentId)) {
      throw new AccessDeniedException("Vous ne pouvez demander que votre propre releve");
    }
  }

  @Async
  public void generateAndSendTranscript(UUID studentId, boolean complete) {
    Student student =
        studentMapper.toModel(
            studentRepository
                .findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student introuvable")));

    List<Course> courses =
        courseMapper.toModel(courseRepository.findByTrackIsNullOrTrack(student.track()));
    Map<Course, BigDecimal> averages = courseAverageService.averagesByCourse(studentId, courses);

    Context context = new Context();
    context.setVariable("student", student);
    context.setVariable("averages", averages);
    context.setVariable("complete", complete);
    context.setVariable("generalAverage", courseAverageService.generalAverage(studentId, courses));

    String html = templateEngine.process("transcript", context);
    File pdfFile = renderPdf(html);

    String bucketKey =
        "transcripts/"
            + studentId
            + "-"
            + (complete ? "complet" : "provisoire")
            + "-"
            + UUID.randomUUID()
            + ".pdf";
    bucketComponent.upload(pdfFile, bucketKey);
    var presignedUrl = bucketComponent.presign(bucketKey, java.time.Duration.ofDays(7));

    sendEmail(student, presignedUrl.toString(), complete);
  }

  @SneakyThrows
  private File renderPdf(String html) {
    File pdfFile = File.createTempFile("transcript", ".pdf");
    try (var os = java.nio.file.Files.newOutputStream(pdfFile.toPath())) {
      var builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
      builder.useFastMode();
      builder.withHtmlContent(html, null);
      builder.toStream(os);
      builder.run();
    }
    return pdfFile;
  }

  @SneakyThrows
  private void sendEmail(Student student, String downloadUrl, boolean complete) {
    String subject = "Votre relevé de notes " + (complete ? "complet" : "provisoire");
    String body =
        "<p>Bonjour "
            + student.firstName()
            + ",</p><p>Votre relevé de notes est disponible via le lien suivant (valable 7 jours) :"
            + " <a href=\""
            + downloadUrl
            + "\">telecharger le relevé</a></p>";
    mailer.accept(
        new Email(
            new InternetAddress(student.user().email()),
            List.of(),
            List.of(),
            subject,
            body,
            List.of()));
  }
}
