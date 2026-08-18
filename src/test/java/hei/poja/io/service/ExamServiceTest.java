package hei.poja.io.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.CourseMapper;
import hei.poja.io.mapper.ExamMapper;
import hei.poja.io.model.Role;
import hei.poja.io.model.Track;
import hei.poja.io.repository.AppUserRepository;
import hei.poja.io.repository.CourseAssignmentRepository;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.ExamRepository;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JCourse;
import hei.poja.io.repository.model.JCourseAssignment;
import hei.poja.io.repository.model.JTeacher;
import java.math.BigDecimal;
import java.time.Instant;
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
class ExamServiceTest {

    @Mock private ExamRepository examRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseAssignmentRepository courseAssignmentRepository;
    @Mock private AppUserRepository appUserRepository;

    private ExamService examService;

    private final UUID courseId = UUID.randomUUID();
    private final UUID actingUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        examService =
                new ExamService(
                        examRepository, courseRepository, courseAssignmentRepository, appUserRepository, new ExamMapper(new CourseMapper()));
    }

    @Test
    void findByCourse_returns_exams() {
        UUID examId = UUID.randomUUID();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        var entity = hei.poja.io.repository.model.JExam.builder()
                .id(examId).course(course).title("Final").coefficient(BigDecimal.ONE).academicYear(2024).semester(1).build();
        when(examRepository.findByCourseId(courseId)).thenReturn(List.of(entity));

        var result = examService.findByCourse(courseId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Final");
        assertThat(result.get(0).coefficient()).isEqualTo(BigDecimal.ONE);
        assertThat(result.get(0).academicYear()).isEqualTo(2024);
        assertThat(result.get(0).semester()).isEqualTo(1);
    }

    @Test
    void createExam_throws_NotFoundException_when_actingUser_missing() {
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () ->
                        examService.createExam(
                                courseId, "Final", Instant.now(), BigDecimal.ONE, 2024, 1, actingUserId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createExam_throws_NotFoundException_when_course_missing() {
        JAppUser admin = JAppUser.builder().id(actingUserId).role(Role.ADMIN).build();
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(admin));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () ->
                        examService.createExam(
                                courseId, "Final", Instant.now(), BigDecimal.ONE, 2024, 1, actingUserId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createExam_admin_bypasses_assignment_check() {
        JAppUser admin = JAppUser.builder().id(actingUserId).role(Role.ADMIN).build();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(admin));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(examRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant now = Instant.now();
        var exam =
                examService.createExam(courseId, "Final", now, BigDecimal.ONE, 2024, 1, actingUserId);

        assertThat(exam.title()).isEqualTo("Final");
        assertThat(exam.dateExam()).isEqualTo(now);
        assertThat(exam.coefficient()).isEqualTo(BigDecimal.ONE);
        assertThat(exam.academicYear()).isEqualTo(2024);
        assertThat(exam.semester()).isEqualTo(1);
        assertThat(exam.course().id()).isEqualTo(courseId);
    }

    @Test
    void createExam_rejects_teacher_not_assigned_to_course() {
        JAppUser teacher = JAppUser.builder().id(actingUserId).role(Role.TEACHER).build();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseAssignmentRepository.findByCourseIdAndAcademicYear(courseId, 2024)).thenReturn(List.of());

        assertThatThrownBy(
                () ->
                        examService.createExam(
                                courseId, "Final", Instant.now(), BigDecimal.ONE, 2024, 1, actingUserId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createExam_allows_teacher_assigned_to_course() {
        JAppUser teacher = JAppUser.builder().id(actingUserId).role(Role.TEACHER).build();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        JTeacher jTeacher = JTeacher.builder().id(actingUserId).firstName("Ada").lastName("Lovelace").build();
        JCourseAssignment assignment = JCourseAssignment.builder().id(UUID.randomUUID()).teacher(jTeacher).build();
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseAssignmentRepository.findByCourseIdAndAcademicYear(courseId, 2024))
                .thenReturn(List.of(assignment));
        when(examRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant now = Instant.now();
        var exam =
                examService.createExam(courseId, "Final", now, new BigDecimal("2.5"), 2024, 2, actingUserId);

        assertThat(exam.title()).isEqualTo("Final");
        assertThat(exam.dateExam()).isEqualTo(now);
        assertThat(exam.coefficient()).isEqualTo(new BigDecimal("2.5"));
        assertThat(exam.academicYear()).isEqualTo(2024);
        assertThat(exam.semester()).isEqualTo(2);
        assertThat(exam.course().id()).isEqualTo(courseId);
    }

    @Test
    void createExam_rejects_student_role() {
        JAppUser student = JAppUser.builder().id(actingUserId).role(Role.STUDENT).build();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        when(appUserRepository.findById(actingUserId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        assertThatThrownBy(
                () ->
                        examService.createExam(
                                courseId, "Final", Instant.now(), BigDecimal.ONE, 2024, 1, actingUserId))
                .isInstanceOf(AccessDeniedException.class);
    }
}