package hei.poja.io.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import hei.poja.io.exception.BadRequestException;
import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.AppUserMapper;
import hei.poja.io.mapper.CourseMapper;
import hei.poja.io.mapper.ExamMapper;
import hei.poja.io.mapper.GradeHistoryMapper;
import hei.poja.io.mapper.GradeMapper;
import hei.poja.io.mapper.StudentMapper;
import hei.poja.io.model.Role;
import hei.poja.io.model.Track;
import hei.poja.io.repository.*;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JCourse;
import hei.poja.io.repository.model.JCourseAssignment;
import hei.poja.io.repository.model.JExam;
import hei.poja.io.repository.model.JGrade;
import hei.poja.io.repository.model.JGradeHistory;
import hei.poja.io.repository.model.JStudent;
import hei.poja.io.repository.model.JTeacher;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock private GradeRepository gradeRepository;
    @Mock private GradeHistoryRepository gradeHistoryRepository;
    @Mock private ExamRepository examRepository;
    @Mock private CourseAssignmentRepository courseAssignmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private AppUserRepository appUserRepository;

    private GradeService gradeService;

    private final UUID studentId = UUID.randomUUID();
    private final UUID examId = UUID.randomUUID();
    private final UUID actingUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AppUserMapper appUserMapper = new AppUserMapper();
        GradeMapper gradeMapper =
                new GradeMapper(new StudentMapper(appUserMapper), new ExamMapper(new CourseMapper()));
        gradeService =
                new GradeService(
                        gradeRepository,
                        gradeHistoryRepository,
                        examRepository,
                        courseAssignmentRepository,
                        studentRepository,
                        appUserRepository,
                        gradeMapper,
                        new GradeHistoryMapper(gradeMapper, appUserMapper));
    }

    @Test
    void getGradesForStudent_throws_when_actingUser_missing() {
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.getGradesForStudent(studentId, actingUserId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getGradesForStudent_forbids_other_students() {
        JAppUser otherStudent = JAppUser.builder().id(UUID.randomUUID()).role(Role.STUDENT).build();
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(otherStudent));

        assertThatThrownBy(() -> gradeService.getGradesForStudent(studentId, actingUserId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getGradesForStudent_allows_own_student() {
        JAppUser self = JAppUser.builder().id(studentId).role(Role.STUDENT).build();
        when(appUserRepository.findById(studentId)).thenReturn(Optional.of(self));
        when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of());

        assertThat(gradeService.getGradesForStudent(studentId, studentId)).isEmpty();
    }

    @Test
    void getGradesForStudent_allows_teacher_to_see_any_student() {
        UUID teacherId = UUID.randomUUID();
        JAppUser teacher = JAppUser.builder().id(teacherId).role(Role.TEACHER).build();
        when(appUserRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of());

        assertThat(gradeService.getGradesForStudent(studentId, teacherId)).isEmpty();
    }

    @Test
    void getGradesForStudent_allows_admin_to_see_any_student() {
        UUID adminId = UUID.randomUUID();
        JAppUser admin = JAppUser.builder().id(adminId).role(Role.ADMIN).build();
        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of());

        assertThat(gradeService.getGradesForStudent(studentId, adminId)).isEmpty();
    }

    @Test
    void getGradesForStudent_returns_grades() {
        JAppUser self = JAppUser.builder().id(studentId).role(Role.STUDENT).build();
        JCourse course = JCourse.builder().id(UUID.randomUUID()).ref("A").title("t").credits(1).track(Track.EL).build();
        JExam exam = JExam.builder().id(examId).course(course).academicYear(2024).build();
        JStudent student = JStudent.builder().id(studentId).user(self).firstName("Grace").lastName("Hopper").track(Track.EL).enrollmentYear(2024).build();
        JGrade grade = JGrade.builder().id(UUID.randomUUID()).value(new BigDecimal("12")).exam(exam).student(student).build();
        when(appUserRepository.findById(studentId)).thenReturn(Optional.of(self));
        when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of(grade));

        var result = gradeService.getGradesForStudent(studentId, studentId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).value()).isEqualByComparingTo("12");
    }

    @Test
    void getHistory_returns_history_entries() {
        UUID gradeId = UUID.randomUUID();
        JAppUser self = JAppUser.builder().id(studentId).role(Role.STUDENT).build();
        JCourse course = JCourse.builder().id(UUID.randomUUID()).ref("A").title("t").credits(1).track(Track.EL).build();
        JExam exam = JExam.builder().id(examId).course(course).academicYear(2024).build();
        JStudent student = JStudent.builder().id(studentId).user(self).firstName("Grace").lastName("Hopper").track(Track.EL).enrollmentYear(2024).build();
        JGrade gradeEntity = JGrade.builder().id(gradeId).value(new BigDecimal("12")).exam(exam).student(student).build();
        JAppUser modifier = JAppUser.builder().id(UUID.randomUUID()).email("admin@hei.school").role(Role.ADMIN).build();
        JGradeHistory history = JGradeHistory.builder()
                .id(UUID.randomUUID())
                .grade(gradeEntity)
                .previousValue(new BigDecimal("10"))
                .newValue(new BigDecimal("12"))
                .reason("Correction")
                .modifiedBy(modifier)
                .build();
        when(gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId)).thenReturn(List.of(history));

        var result = gradeService.getHistory(gradeId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).reason()).isEqualTo("Correction");
    }

    @Test
    void setGrade_throws_NotFoundException_when_actingUser_missing() {
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> gradeService.setGrade(studentId, examId, new BigDecimal("10"), null, actingUserId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setGrade_throws_NotFoundException_when_exam_missing() {
        JAppUser admin = JAppUser.builder().id(actingUserId).role(Role.ADMIN).build();
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(admin));
        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> gradeService.setGrade(studentId, examId, new BigDecimal("10"), null, actingUserId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setGrade_rejects_student_role() {
        JAppUser student = JAppUser.builder().id(actingUserId).role(Role.STUDENT).build();
        JCourse course = JCourse.builder().id(UUID.randomUUID()).ref("A").title("t").credits(1).track(Track.EL).build();
        JExam exam = JExam.builder().id(examId).course(course).academicYear(2025).build();
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(student));
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

        assertThatThrownBy(
                () -> gradeService.setGrade(studentId, examId, new BigDecimal("10"), null, actingUserId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void setGrade_requires_reason_when_grade_already_exists() {
        JCourse course =
                JCourse.builder().id(UUID.randomUUID()).ref("A").title("t").credits(1).track(Track.EL).build();
        JExam exam = JExam.builder().id(examId).course(course).academicYear(2025).build();
        JAppUser admin = JAppUser.builder().id(actingUserId).role(Role.ADMIN).build();
        JGrade existing = JGrade.builder().id(UUID.randomUUID()).value(new BigDecimal("8")).build();

        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(admin));
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(gradeRepository.findByStudentIdAndExamId(studentId, examId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(
                () -> gradeService.setGrade(studentId, examId, new BigDecimal("12"), null, actingUserId))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void setGrade_rejects_teacher_not_assigned_to_course() {
        JCourse course =
                JCourse.builder().id(UUID.randomUUID()).ref("A").title("t").credits(1).track(Track.EL).build();
        JExam exam = JExam.builder().id(examId).course(course).academicYear(2025).build();
        JAppUser teacher = JAppUser.builder().id(actingUserId).role(Role.TEACHER).build();

        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(teacher));
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(courseAssignmentRepository.findByCourseIdAndAcademicYear(course.getId(), 2025)).thenReturn(List.of());

        assertThatThrownBy(
                () ->
                        gradeService.setGrade(
                                studentId, examId, new BigDecimal("12"), "premiere saisie", actingUserId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void setGrade_creates_new_grade_as_admin() {
        JCourse course =
                JCourse.builder().id(UUID.randomUUID()).ref("A").title("t").credits(1).track(Track.EL).build();
        JExam exam = JExam.builder().id(examId).course(course).academicYear(2025).build();
        JAppUser admin = JAppUser.builder().id(actingUserId).email("admin@hei.school").role(Role.ADMIN).build();
        JAppUser studentUser = JAppUser.builder().id(studentId).email("stu@hei.school").role(Role.STUDENT).build();
        JStudent studentRef = JStudent.builder().id(studentId).user(studentUser).firstName("Grace").lastName("Hopper").track(Track.EL).enrollmentYear(2024).build();

        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(admin));
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(gradeRepository.findByStudentIdAndExamId(studentId, examId)).thenReturn(Optional.empty());
        when(studentRepository.getReferenceById(studentId)).thenReturn(studentRef);
        when(gradeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var grade = gradeService.setGrade(studentId, examId, new BigDecimal("12"), null, actingUserId);

        assertThat(grade.value()).isEqualByComparingTo("12");
    }

    @Test
    void setGrade_allows_teacher_assigned_to_course_first_time() {
        JCourse course =
                JCourse.builder().id(UUID.randomUUID()).ref("A").title("t").credits(1).track(Track.EL).build();
        JExam exam = JExam.builder().id(examId).course(course).academicYear(2025).build();
        JAppUser teacher = JAppUser.builder().id(actingUserId).email("prof@hei.school").role(Role.TEACHER).build();
        JAppUser teacherUser = JAppUser.builder().id(actingUserId).email("prof@hei.school").role(Role.TEACHER).build();
        JTeacher jTeacher = JTeacher.builder().id(actingUserId).user(teacherUser).firstName("Ada").lastName("Lovelace").build();
        JCourseAssignment assignment = JCourseAssignment.builder().id(UUID.randomUUID()).teacher(jTeacher).build();
        JAppUser studentUser = JAppUser.builder().id(studentId).email("stu@hei.school").role(Role.STUDENT).build();
        JStudent studentRef = JStudent.builder().id(studentId).user(studentUser).firstName("Grace").lastName("Hopper").track(Track.EL).enrollmentYear(2024).build();

        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(teacher));
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(courseAssignmentRepository.findByCourseIdAndAcademicYear(course.getId(), 2025))
                .thenReturn(List.of(assignment));
        when(gradeRepository.findByStudentIdAndExamId(studentId, examId)).thenReturn(Optional.empty());
        when(studentRepository.getReferenceById(studentId)).thenReturn(studentRef);
        when(gradeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var grade = gradeService.setGrade(studentId, examId, new BigDecimal("12"), null, actingUserId);

        assertThat(grade.value()).isEqualByComparingTo("12");
    }

    @Test
    void setGrade_updates_existing_grade_with_reason() {
        UUID gradeId = UUID.randomUUID();
        JCourse course =
                JCourse.builder().id(UUID.randomUUID()).ref("A").title("t").credits(1).track(Track.EL).build();
        JExam exam = JExam.builder().id(examId).course(course).academicYear(2025).build();
        JAppUser admin = JAppUser.builder().id(actingUserId).email("admin@hei.school").role(Role.ADMIN).build();
        JAppUser studentUser = JAppUser.builder().id(studentId).email("stu@hei.school").role(Role.STUDENT).build();
        JStudent studentRef = JStudent.builder().id(studentId).user(studentUser).firstName("Grace").lastName("Hopper").track(Track.EL).enrollmentYear(2024).build();
        JGrade existing = JGrade.builder().id(gradeId).value(new BigDecimal("8")).student(studentRef).exam(exam).build();

        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(admin));
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(gradeRepository.findByStudentIdAndExamId(studentId, examId)).thenReturn(Optional.of(existing));
        when(gradeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var grade = gradeService.setGrade(studentId, examId, new BigDecimal("15"), "Correction suite a reclamation", actingUserId);

        assertThat(grade.value()).isEqualByComparingTo("15");
    }
}