package hei.poja.io.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import hei.poja.io.model.Course;
import hei.poja.io.model.Track;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.ExamRepository;
import hei.poja.io.repository.GradeRepository;
import hei.poja.io.repository.model.JCourse;
import hei.poja.io.repository.model.JExam;
import hei.poja.io.repository.model.JGrade;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseAverageServiceTest {

  @Mock private CourseRepository courseRepository;
  @Mock private ExamRepository examRepository;
  @Mock private GradeRepository gradeRepository;

  private CourseAverageService courseAverageService;

  private final UUID studentId = UUID.randomUUID();
  private final UUID courseId = UUID.randomUUID();
  private final int academicYear = 2024;

  @BeforeEach
  void setUp() {
    courseAverageService =
        new CourseAverageService(courseRepository, examRepository, gradeRepository);
  }

  @Test
  void averageForCourse_weights_by_coefficient() {
    JCourse course = JCourse.builder().id(courseId).build();
    JExam exam1 = JExam.builder().course(course).coefficient(new BigDecimal("0.25")).build();
    JExam exam2 = JExam.builder().course(course).coefficient(new BigDecimal("0.75")).build();
    JGrade grade1 = JGrade.builder().exam(exam1).value(new BigDecimal("10")).build();
    JGrade grade2 = JGrade.builder().exam(exam2).value(new BigDecimal("14")).build();

    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, courseId, academicYear))
        .thenReturn(List.of(grade1, grade2));

    var average = courseAverageService.averageForCourse(studentId, courseId, academicYear);

    assertThat(average).isEqualByComparingTo("13");
  }

  @Test
  void averageForCourse_returns_null_when_no_grade() {
    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, courseId, academicYear))
        .thenReturn(List.of());

    assertThat(courseAverageService.averageForCourse(studentId, courseId, academicYear)).isNull();
  }

  @Test
  void averagesByCourse_returns_map_of_averages() {
    UUID course2Id = UUID.randomUUID();
    Course course1 = Course.builder().id(courseId).ref("A").credits(5).track(Track.EL).build();
    Course course2 = Course.builder().id(course2Id).ref("B").credits(3).track(Track.EL).build();

    JCourse jCourse1 = JCourse.builder().id(courseId).build();
    JCourse jCourse2 = JCourse.builder().id(course2Id).build();
    JExam exam1 = JExam.builder().course(jCourse1).coefficient(BigDecimal.ONE).build();
    JExam exam2 = JExam.builder().course(jCourse2).coefficient(BigDecimal.ONE).build();
    JGrade grade1 = JGrade.builder().exam(exam1).value(new BigDecimal("12")).build();
    JGrade grade2 = JGrade.builder().exam(exam2).value(new BigDecimal("16")).build();

    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, courseId, academicYear))
        .thenReturn(List.of(grade1));
    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, course2Id, academicYear))
        .thenReturn(List.of(grade2));

    var result =
        courseAverageService.averagesByCourse(studentId, List.of(course1, course2), academicYear);

    assertThat(result).hasSize(2);
    assertThat(result.get(course1)).isEqualByComparingTo("12");
    assertThat(result.get(course2)).isEqualByComparingTo("16");
  }

  @Test
  void isEligibleForDiploma_false_when_one_course_below_ten() {
    Course course1 =
        Course.builder().id(UUID.randomUUID()).ref("A").credits(5).track(Track.EL).build();
    Course course2 =
        Course.builder().id(UUID.randomUUID()).ref("B").credits(5).track(Track.EL).build();

    JCourse jCourse1 = JCourse.builder().id(course1.id()).build();
    JCourse jCourse2 = JCourse.builder().id(course2.id()).build();
    JExam exam1 = JExam.builder().course(jCourse1).coefficient(BigDecimal.ONE).build();
    JExam exam2 = JExam.builder().course(jCourse2).coefficient(BigDecimal.ONE).build();
    JGrade grade1 = JGrade.builder().exam(exam1).value(new BigDecimal("12")).build();
    JGrade grade2 = JGrade.builder().exam(exam2).value(new BigDecimal("8")).build();

    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, course1.id(), academicYear))
        .thenReturn(List.of(grade1));
    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, course2.id(), academicYear))
        .thenReturn(List.of(grade2));

    assertThat(
            courseAverageService.isEligibleForDiploma(
                studentId, List.of(course1, course2), academicYear))
        .isFalse();
  }

  @Test
  void isEligibleForDiploma_false_when_no_grade_at_all() {
    Course course = Course.builder().id(courseId).ref("A").credits(5).track(Track.EL).build();
    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, courseId, academicYear))
        .thenReturn(List.of());

    assertThat(courseAverageService.isEligibleForDiploma(studentId, List.of(course), academicYear))
        .isFalse();
  }

  @Test
  void isEligibleForDiploma_true_when_all_courses_above_ten() {
    Course course1 =
        Course.builder().id(UUID.randomUUID()).ref("A").credits(5).track(Track.EL).build();
    Course course2 =
        Course.builder().id(UUID.randomUUID()).ref("B").credits(5).track(Track.EL).build();

    JCourse jCourse1 = JCourse.builder().id(course1.id()).build();
    JCourse jCourse2 = JCourse.builder().id(course2.id()).build();
    JExam exam1 = JExam.builder().course(jCourse1).coefficient(BigDecimal.ONE).build();
    JExam exam2 = JExam.builder().course(jCourse2).coefficient(BigDecimal.ONE).build();
    JGrade grade1 = JGrade.builder().exam(exam1).value(new BigDecimal("12")).build();
    JGrade grade2 = JGrade.builder().exam(exam2).value(new BigDecimal("15")).build();

    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, course1.id(), academicYear))
        .thenReturn(List.of(grade1));
    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, course2.id(), academicYear))
        .thenReturn(List.of(grade2));

    assertThat(
            courseAverageService.isEligibleForDiploma(
                studentId, List.of(course1, course2), academicYear))
        .isTrue();
  }

  @Test
  void generalAverage_weights_by_credits() {
    Course course1 =
        Course.builder().id(UUID.randomUUID()).ref("A").credits(5).track(Track.EL).build();
    Course course2 =
        Course.builder().id(UUID.randomUUID()).ref("B").credits(3).track(Track.EL).build();

    JCourse jCourse1 = JCourse.builder().id(course1.id()).build();
    JCourse jCourse2 = JCourse.builder().id(course2.id()).build();
    JExam exam1 = JExam.builder().course(jCourse1).coefficient(BigDecimal.ONE).build();
    JExam exam2 = JExam.builder().course(jCourse2).coefficient(BigDecimal.ONE).build();
    JGrade grade1 = JGrade.builder().exam(exam1).value(new BigDecimal("10")).build();
    JGrade grade2 = JGrade.builder().exam(exam2).value(new BigDecimal("18")).build();

    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, course1.id(), academicYear))
        .thenReturn(List.of(grade1));
    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, course2.id(), academicYear))
        .thenReturn(List.of(grade2));

    // (10*5 + 18*3) / 8 = 13.0
    assertThat(
            courseAverageService.generalAverage(studentId, List.of(course1, course2), academicYear))
        .isEqualByComparingTo("13");
  }

  @Test
  void generalAverage_skips_courses_with_no_grades() {
    Course course1 =
        Course.builder().id(UUID.randomUUID()).ref("A").credits(5).track(Track.EL).build();
    Course course2 =
        Course.builder().id(UUID.randomUUID()).ref("B").credits(3).track(Track.EL).build();

    JCourse jCourse1 = JCourse.builder().id(course1.id()).build();
    JExam exam1 = JExam.builder().course(jCourse1).coefficient(BigDecimal.ONE).build();
    JGrade grade1 = JGrade.builder().exam(exam1).value(new BigDecimal("14")).build();

    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, course1.id(), academicYear))
        .thenReturn(List.of(grade1));
    when(gradeRepository.findByStudentIdAndExam_CourseIdAndExam_AcademicYear(
            studentId, course2.id(), academicYear))
        .thenReturn(List.of());

    // Only course1 counts: (14*5) / 5 = 14.0
    assertThat(
            courseAverageService.generalAverage(studentId, List.of(course1, course2), academicYear))
        .isEqualByComparingTo("14");
  }

  @Test
  void generalAverage_returns_zero_when_no_courses() {
    assertThat(courseAverageService.generalAverage(studentId, List.of(), academicYear))
        .isEqualByComparingTo("0");
  }
}
