package hei.poja.io.service;

import hei.poja.io.model.Course;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.ExamRepository;
import hei.poja.io.repository.GradeRepository;
import hei.poja.io.repository.model.JGrade;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseAverageService {
  private final CourseRepository courseRepository;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;

  @Transactional(readOnly = true)
  public BigDecimal averageForCourse(UUID studentId, UUID courseId) {
    List<JGrade> grades = gradeRepository.findByStudentIdAndExam_CourseId(studentId, courseId);
    if (grades.isEmpty()) {
      return null;
    }
    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal totalCoeff = BigDecimal.ZERO;
    for (JGrade grade : grades) {
      BigDecimal coeff = grade.getExam().getCoefficient();
      weightedSum = weightedSum.add(grade.getValue().multiply(coeff));
      totalCoeff = totalCoeff.add(coeff);
    }
    if (totalCoeff.signum() == 0) {
      return null;
    }
    return weightedSum.divide(totalCoeff, new MathContext(4, RoundingMode.HALF_UP));
  }

  @Transactional(readOnly = true)
  public Map<Course, BigDecimal> averagesByCourse(UUID studentId, List<Course> courses) {
    return courses.stream()
        .collect(
            Collectors.toMap(
                course -> course,
                course -> averageForCourse(studentId, course.id()),
                (a, b) -> a,
                java.util.LinkedHashMap::new));
  }

  @Transactional(readOnly = true)
  public boolean isEligibleForDiploma(UUID studentId, List<Course> curriculumCourses) {
    for (Course course : curriculumCourses) {
      BigDecimal average = averageForCourse(studentId, course.id());
      if (average == null || average.compareTo(BigDecimal.TEN) < 0) {
        return false;
      }
    }
    return true;
  }

  @Transactional(readOnly = true)
  public BigDecimal generalAverage(UUID studentId, List<Course> curriculumCourses) {
    BigDecimal weightedSum = BigDecimal.ZERO;
    int totalCredits = 0;
    for (Course course : curriculumCourses) {
      BigDecimal average = averageForCourse(studentId, course.id());
      if (average == null) {
        continue;
      }
      weightedSum = weightedSum.add(average.multiply(BigDecimal.valueOf(course.credits())));
      totalCredits += course.credits();
    }
    if (totalCredits == 0) {
      return BigDecimal.ZERO;
    }
    return weightedSum.divide(
        BigDecimal.valueOf(totalCredits), new MathContext(4, RoundingMode.HALF_UP));
  }
}
