package hei.poja.io.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.AppUserMapper;
import hei.poja.io.mapper.GroupMapper;
import hei.poja.io.mapper.StudentGroupHistoryMapper;
import hei.poja.io.mapper.StudentMapper;
import hei.poja.io.model.Role;
import hei.poja.io.model.Track;
import hei.poja.io.repository.AppUserRepository;
import hei.poja.io.repository.GroupRepository;
import hei.poja.io.repository.StudentGroupHistoryRepository;
import hei.poja.io.repository.StudentRepository;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JGroup;
import hei.poja.io.repository.model.JStudent;
import hei.poja.io.repository.model.JStudentGroupHistory;
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
class GroupServiceTest {

  @Mock private GroupRepository groupRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;
  @Mock private AppUserRepository appUserRepository;

  private GroupService groupService;

  @BeforeEach
  void setUp() {
    AppUserMapper appUserMapper = new AppUserMapper();
    StudentMapper studentMapper = new StudentMapper(appUserMapper);
    GroupMapper groupMapper = new GroupMapper();
    groupService =
        new GroupService(
            groupRepository,
            studentRepository,
            studentGroupHistoryRepository,
            appUserRepository,
            groupMapper,
            new StudentGroupHistoryMapper(studentMapper, groupMapper));
  }

  @Test
  void findAll_returns_groups() {
    JGroup group =
        JGroup.builder()
            .id(UUID.randomUUID())
            .ref("EL1-A")
            .track(Track.EL)
            .academicYear(2024)
            .build();
    when(groupRepository.findAll()).thenReturn(List.of(group));

    var groups = groupService.findAll();

    assertThat(groups).hasSize(1);
    assertThat(groups.get(0).ref()).isEqualTo("EL1-A");
  }

  @Test
  void findById_returns_group_when_found() {
    UUID id = UUID.randomUUID();
    JGroup group = JGroup.builder().id(id).ref("EL1-A").track(Track.EL).academicYear(2024).build();
    when(groupRepository.findById(id)).thenReturn(Optional.of(group));

    var result = groupService.findById(id);

    assertThat(result.ref()).isEqualTo("EL1-A");
    assertThat(result.track()).isEqualTo(Track.EL);
    assertThat(result.academicYear()).isEqualTo(2024);
  }

  @Test
  void findById_throws_when_missing() {
    UUID id = UUID.randomUUID();
    when(groupRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.findById(id)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void create_saves_new_group() {
    when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var group = groupService.create("EL1-A", Track.EL, 2024);

    assertThat(group.ref()).isEqualTo("EL1-A");
    assertThat(group.track()).isEqualTo(Track.EL);
    assertThat(group.academicYear()).isEqualTo(2024);
  }

  @Test
  void update_modifies_existing_group() {
    UUID id = UUID.randomUUID();
    JGroup existing = JGroup.builder().id(id).ref("OLD").track(Track.EL).academicYear(2023).build();
    when(groupRepository.findById(id)).thenReturn(Optional.of(existing));
    when(groupRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var updated = groupService.update(id, "NEW", Track.TN, 2024);

    assertThat(updated.ref()).isEqualTo("NEW");
    assertThat(updated.track()).isEqualTo(Track.TN);
    assertThat(updated.academicYear()).isEqualTo(2024);
  }

  @Test
  void update_throws_when_missing() {
    UUID id = UUID.randomUUID();
    when(groupRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.update(id, "REF", Track.EL, 2024))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void deleteById_delegates_to_repository() {
    UUID id = UUID.randomUUID();
    groupService.deleteById(id);
    org.mockito.Mockito.verify(groupRepository).deleteById(id);
  }

  @Test
  void changeGroup_throws_when_student_not_found() {
    UUID studentId = UUID.randomUUID();
    UUID newGroupId = UUID.randomUUID();
    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.changeGroup(studentId, newGroupId))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void changeGroup_throws_when_group_not_found() {
    UUID studentId = UUID.randomUUID();
    UUID newGroupId = UUID.randomUUID();
    JAppUser user = JAppUser.builder().id(studentId).build();
    JStudent student =
        JStudent.builder()
            .id(studentId)
            .user(user)
            .firstName("Grace")
            .lastName("Hopper")
            .track(Track.EL)
            .enrollmentYear(2024)
            .build();
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(groupRepository.findById(newGroupId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.changeGroup(studentId, newGroupId))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void changeGroup_closes_previous_entry_and_opens_new_one() {
    UUID studentId = UUID.randomUUID();
    UUID newGroupId = UUID.randomUUID();
    JAppUser user = JAppUser.builder().id(studentId).build();
    JStudent student =
        JStudent.builder()
            .id(studentId)
            .user(user)
            .firstName("Grace")
            .lastName("Hopper")
            .track(Track.EL)
            .enrollmentYear(2024)
            .build();
    JGroup newGroup =
        JGroup.builder().id(newGroupId).ref("EL1-B").track(Track.EL).academicYear(2024).build();
    JStudentGroupHistory current =
        JStudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .student(student)
            .group(newGroup)
            .build();

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(groupRepository.findById(newGroupId)).thenReturn(Optional.of(newGroup));
    when(studentGroupHistoryRepository.findByStudentIdAndToDateIsNull(studentId))
        .thenReturn(Optional.of(current));
    when(studentGroupHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var result = groupService.changeGroup(studentId, newGroupId);

    assertThat(result.group().id()).isEqualTo(newGroupId);
    assertThat(result.student().id()).isEqualTo(studentId);
    assertThat(result.fromDate()).isNotNull();
    assertThat(current.getToDate()).isNotNull();
  }

  @Test
  void changeGroup_creates_entry_when_no_previous() {
    UUID studentId = UUID.randomUUID();
    UUID newGroupId = UUID.randomUUID();
    JAppUser user = JAppUser.builder().id(studentId).build();
    JStudent student =
        JStudent.builder()
            .id(studentId)
            .user(user)
            .firstName("Grace")
            .lastName("Hopper")
            .track(Track.EL)
            .enrollmentYear(2024)
            .build();
    JGroup newGroup =
        JGroup.builder().id(newGroupId).ref("EL1-B").track(Track.EL).academicYear(2024).build();

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(groupRepository.findById(newGroupId)).thenReturn(Optional.of(newGroup));
    when(studentGroupHistoryRepository.findByStudentIdAndToDateIsNull(studentId))
        .thenReturn(Optional.empty());
    when(studentGroupHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var result = groupService.changeGroup(studentId, newGroupId);

    assertThat(result.group().id()).isEqualTo(newGroupId);
    assertThat(result.fromDate()).isNotNull();
  }

  @Test
  void history_throws_when_user_not_found() {
    UUID studentId = UUID.randomUUID();
    UUID actingUserId = UUID.randomUUID();
    when(appUserRepository.findById(actingUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.history(studentId, actingUserId))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void history_allows_student_to_see_own_history() {
    UUID studentId = UUID.randomUUID();
    JAppUser studentUser = JAppUser.builder().id(studentId).role(Role.STUDENT).build();
    when(appUserRepository.findById(studentId)).thenReturn(Optional.of(studentUser));
    when(studentGroupHistoryRepository.findByStudentIdOrderByFromDateDesc(studentId))
        .thenReturn(List.of());

    assertThat(groupService.history(studentId, studentId)).isEmpty();
  }

  @Test
  void history_forbids_other_students() {
    UUID studentId = UUID.randomUUID();
    UUID otherStudentId = UUID.randomUUID();
    JAppUser otherUser = JAppUser.builder().id(otherStudentId).role(Role.STUDENT).build();
    when(appUserRepository.findById(otherStudentId)).thenReturn(Optional.of(otherUser));

    assertThatThrownBy(() -> groupService.history(studentId, otherStudentId))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void history_allows_admin_to_see_any_student() {
    UUID studentId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    JAppUser admin = JAppUser.builder().id(adminId).role(Role.ADMIN).build();
    when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
    when(studentGroupHistoryRepository.findByStudentIdOrderByFromDateDesc(studentId))
        .thenReturn(List.of());

    assertThat(groupService.history(studentId, adminId)).isEmpty();
  }

  @Test
  void history_allows_staff_to_see_any_student() {
    UUID studentId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    JAppUser teacher = JAppUser.builder().id(teacherId).role(Role.TEACHER).build();
    when(appUserRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
    when(studentGroupHistoryRepository.findByStudentIdOrderByFromDateDesc(studentId))
        .thenReturn(List.of());

    assertThat(groupService.history(studentId, teacherId)).isEmpty();
  }
}
