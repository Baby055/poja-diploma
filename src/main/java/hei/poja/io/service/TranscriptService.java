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
import hei.poja.io.repository.AppUserRepository;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.StudentRepository;
import hei.poja.io.repository.model.JAppUser;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.ObjectProvider;
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
  private final AppUserRepository appUserRepository;
  private final StudentMapper studentMapper;
  private final CourseMapper courseMapper;
  private final ObjectProvider<TranscriptService> selfProvider;

  public void assertCanRequestTranscript(UUID studentId, UUID actingUserId) {
    JAppUser actingUser =
        appUserRepository
            .findById(actingUserId)
            .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
    boolean isStaff = actingUser.getRole() == Role.TEACHER || actingUser.getRole() == Role.ADMIN;
    if (!isStaff && !actingUserId.equals(studentId)) {
      throw new AccessDeniedException("Vous ne pouvez demander que votre propre releve");
    }
  }

  public String generateAndSendTranscript(UUID studentId, boolean complete) {
    Student student =
        studentMapper.toModel(
            studentRepository
                .findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student introuvable")));
    int academicYear = student.enrollmentYear();

    List<Course> courses =
        courseMapper.toModel(courseRepository.findByTrackIsNullOrTrack(student.track()));
    Map<Course, BigDecimal> averages =
        courseAverageService.averagesByCourse(studentId, courses, academicYear);

    Context context = new Context();
    context.setVariable("student", student);
    context.setVariable("averages", averages);
    context.setVariable("complete", complete);
    context.setVariable(
        "generalAverage", courseAverageService.generalAverage(studentId, courses, academicYear));

    String html = templateEngine.process("transcript", context);

    String bucketKey =
        "transcripts/"
            + studentId
            + "-"
            + (complete ? "complet" : "provisoire")
            + "-"
            + UUID.randomUUID()
            + ".pdf";
    String downloadUrl = bucketComponent.presign(bucketKey, Duration.ofDays(7)).toString();

    selfProvider.getObject().sendTranscriptAsync(html, bucketKey, downloadUrl, student, complete);
    return downloadUrl;
  }

  @Async
  public void sendTranscriptAsync(
      String html, String bucketKey, String downloadUrl, Student student, boolean complete) {
    File pdfFile = renderPdf(html);
    bucketComponent.upload(pdfFile, bucketKey);
    sendEmail(student, downloadUrl, complete);
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
