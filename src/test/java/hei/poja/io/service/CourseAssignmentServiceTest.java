package hei.poja.io.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.AppUserMapper;
import hei.poja.io.mapper.CourseAssignmentMapper;
import hei.poja.io.mapper.CourseMapper;
import hei.poja.io.mapper.GroupMapper;
import hei.poja.io.mapper.TeacherMapper;
import hei.poja.io.model.Track;
import hei.poja.io.repository.CourseAssignmentRepository;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.GroupRepository;
import hei.poja.io.repository.TeacherRepository;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JCourse;
import hei.poja.io.repository.model.JGroup;
import hei.poja.io.repository.model.JTeacher;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseAssignmentServiceTest {

    @Mock private CourseAssignmentRepository repository;
    @Mock private CourseRepository courseRepository;
    @Mock private TeacherRepository teacherRepository;
    @Mock private GroupRepository groupRepository;

    private CourseAssignmentService service;

    @BeforeEach
    void setUp() {
        AppUserMapper appUserMapper = new AppUserMapper();
        service =
                new CourseAssignmentService(
                        repository,
                        courseRepository,
                        teacherRepository,
                        groupRepository,
                        new CourseAssignmentMapper(new CourseMapper(), new TeacherMapper(appUserMapper), new GroupMapper()));
    }

    @Test
    void findAll_returns_assignments() {
        UUID courseId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        JCourse course = JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        JTeacher teacher = buildTeacher(teacherId);
        JGroup group = JGroup.builder().id(groupId).ref("EL1-A").track(Track.EL).academicYear(2024).build();
        var entity = hei.poja.io.repository.model.JCourseAssignment.builder()
                .id(UUID.randomUUID()).course(course).teacher(teacher).group(group).academicYear(2024).semester(1).build();
        when(repository.findAll()).thenReturn(List.of(entity));

        var result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).academicYear()).isEqualTo(2024);
    }

    @Test
    void findById_returns_assignment_when_found() {
        UUID id = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        JCourse course = JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        JTeacher teacher = buildTeacher(teacherId);
        JGroup group = JGroup.builder().id(groupId).ref("EL1-A").track(Track.EL).academicYear(2024).build();
        var entity = hei.poja.io.repository.model.JCourseAssignment.builder()
                .id(id).course(course).teacher(teacher).group(group).academicYear(2024).semester(1).build();
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        var result = service.findById(id);

        assertThat(result.course().ref()).isEqualTo("A");
        assertThat(result.teacher().firstName()).isEqualTo("Ada");
        assertThat(result.group().ref()).isEqualTo("EL1-A");
        assertThat(result.academicYear()).isEqualTo(2024);
        assertThat(result.semester()).isEqualTo(1);
    }

    @Test
    void findById_throws_when_missing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_throws_NotFoundException_when_course_missing() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.create(courseId, UUID.randomUUID(), UUID.randomUUID(), 2024, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_throws_NotFoundException_when_teacher_missing() {
        UUID courseId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(courseId, teacherId, UUID.randomUUID(), 2024, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_throws_NotFoundException_when_group_missing() {
        UUID courseId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        JTeacher teacher = buildTeacher(teacherId);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(courseId, teacherId, groupId, 2024, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_succeeds_when_all_references_exist() {
        UUID courseId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        JTeacher teacher = buildTeacher(teacherId);
        JGroup group = JGroup.builder().id(groupId).ref("EL1-A").track(Track.EL).academicYear(2024).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.create(courseId, teacherId, groupId, 2024, 1);

        assertThat(result.course().id()).isEqualTo(courseId);
        assertThat(result.teacher().id()).isEqualTo(teacherId);
        assertThat(result.group().id()).isEqualTo(groupId);
        assertThat(result.academicYear()).isEqualTo(2024);
        assertThat(result.semester()).isEqualTo(1);
    }

    @Test
    void update_throws_when_assignment_not_found() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.update(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 2024, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_throws_when_course_missing() {
        UUID id = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        var existing = hei.poja.io.repository.model.JCourseAssignment.builder()
                .id(id).academicYear(2023).semester(1).build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.update(id, courseId, UUID.randomUUID(), UUID.randomUUID(), 2024, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_throws_when_teacher_missing() {
        UUID id = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        var existing = hei.poja.io.repository.model.JCourseAssignment.builder()
                .id(id).academicYear(2023).semester(1).build();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.update(id, courseId, teacherId, UUID.randomUUID(), 2024, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_throws_when_group_missing() {
        UUID id = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        var existing = hei.poja.io.repository.model.JCourseAssignment.builder()
                .id(id).academicYear(2023).semester(1).build();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        JTeacher teacher = buildTeacher(teacherId);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.update(id, courseId, teacherId, groupId, 2024, 1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_modifies_existing_assignment() {
        UUID id = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        JCourse course =
                JCourse.builder().id(courseId).ref("A").title("t").credits(1).track(Track.EL).build();
        JTeacher teacher = buildTeacher(teacherId);
        JGroup group = JGroup.builder().id(groupId).ref("EL1-A").track(Track.EL).academicYear(2024).build();
        var existing = hei.poja.io.repository.model.JCourseAssignment.builder()
                .id(id).academicYear(2023).semester(2).build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.update(id, courseId, teacherId, groupId, 2024, 1);

        assertThat(result.course().id()).isEqualTo(courseId);
        assertThat(result.teacher().id()).isEqualTo(teacherId);
        assertThat(result.group().id()).isEqualTo(groupId);
        assertThat(result.academicYear()).isEqualTo(2024);
        assertThat(result.semester()).isEqualTo(1);
    }

    @Test
    void deleteById_delegates_to_repository() {
        UUID id = UUID.randomUUID();
        service.deleteById(id);
        Mockito.verify(repository).deleteById(id);
    }

    private JTeacher buildTeacher(UUID teacherId) {
        JAppUser user = JAppUser.builder().id(teacherId).build();
        return JTeacher.builder().id(teacherId).user(user).firstName("Ada").lastName("Lovelace").build();
    }
}