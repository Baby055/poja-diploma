package hei.poja.io.service;

import hei.poja.io.file.bucket.BucketComponent;
import hei.poja.io.mapper.CourseMapper;
import hei.poja.io.mapper.StudentMapper;
import hei.poja.io.model.Course;
import hei.poja.io.model.Student;
import hei.poja.io.model.Track;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.StudentRepository;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GraduateExportService {
  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;
  private final CourseAverageService courseAverageService;
  private final BucketComponent bucketComponent;
  private final StudentMapper studentMapper;
  private final CourseMapper courseMapper;

  private static final String GRADUATES_KEY_PREFIX = "graduates/";

  public record GraduateRow(int rank, Student student, BigDecimal generalAverage) {}

  public record Promotion(int enrollmentYear, Track track) {}

  @Transactional(readOnly = true)
  public List<Promotion> promotions() {
    return studentMapper.toModel(studentRepository.findAll()).stream()
        .map(s -> new Promotion(s.enrollmentYear(), s.track()))
        .distinct()
        .sorted(
            Comparator.comparing(Promotion::enrollmentYear).thenComparing(p -> p.track().name()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<GraduateRow> computeGraduates(Track track, int enrollmentYear) {
    List<Student> students =
        studentMapper.toModel(
            studentRepository.findByTrackAndEnrollmentYear(track, enrollmentYear));
    List<Course> curriculum =
        courseMapper.toModel(courseRepository.findByTrackIsNullOrTrack(track));

    List<GraduateRow> unranked =
        students.stream()
            .filter(
                student ->
                    courseAverageService.isEligibleForDiploma(
                        student.id(), curriculum, enrollmentYear))
            .map(
                student ->
                    new GraduateRow(
                        0,
                        student,
                        courseAverageService.generalAverage(
                            student.id(), curriculum, enrollmentYear)))
            .sorted(Comparator.comparing(GraduateRow::generalAverage).reversed())
            .toList();

    List<GraduateRow> ranked = new ArrayList<>();
    for (int i = 0; i < unranked.size(); i++) {
      GraduateRow row = unranked.get(i);
      ranked.add(new GraduateRow(i + 1, row.student, row.generalAverage));
    }
    return ranked;
  }

  @SneakyThrows
  public URL exportAndUpload(Track track, int enrollmentYear) {
    List<GraduateRow> graduates = computeGraduates(track, enrollmentYear);

    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      XSSFSheet sheet = workbook.createSheet("Diplomes " + track + " " + enrollmentYear);
      Row header = sheet.createRow(0);
      writeCell(header, 0, "rang");
      writeCell(header, 1, "STD");
      writeCell(header, 2, "nom");
      writeCell(header, 3, "prenom");
      writeCell(header, 4, "moyenne generale");

      int rowIndex = 1;
      for (GraduateRow graduate : graduates) {
        Row row = sheet.createRow(rowIndex++);
        writeCell(row, 0, graduate.rank());
        writeCell(row, 1, graduate.student().id().toString());
        writeCell(row, 2, graduate.student().lastName());
        writeCell(row, 3, graduate.student().firstName());
        writeCell(row, 4, graduate.generalAverage().doubleValue());
      }
      for (int col = 0; col < 5; col++) {
        sheet.autoSizeColumn(col);
      }

      File xlsxFile = File.createTempFile("graduates", ".xlsx");
      try (var os = new java.io.FileOutputStream(xlsxFile)) {
        workbook.write(os);
      }

      String bucketKey =
          GRADUATES_KEY_PREFIX + track + "-" + enrollmentYear + "-" + UUID.randomUUID() + ".xlsx";
      bucketComponent.upload(xlsxFile, bucketKey);
      return bucketComponent.presign(bucketKey, Duration.ofMinutes(30));
    }
  }

  private void writeCell(Row row, int col, String value) {
    row.createCell(col).setCellValue(value);
  }

  private void writeCell(Row row, int col, double value) {
    row.createCell(col).setCellValue(value);
  }
}
