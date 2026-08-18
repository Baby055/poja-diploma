package hei.poja.io.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import hei.poja.io.exception.ConflictException;
import hei.poja.io.mapper.AppUserMapper;
import hei.poja.io.mapper.StudentMapper;
import hei.poja.io.mapper.TeacherMapper;
import hei.poja.io.model.Role;
import hei.poja.io.model.Track;
import hei.poja.io.repository.AppUserRepository;
import hei.poja.io.repository.StudentRepository;
import hei.poja.io.repository.TeacherRepository;
import hei.poja.io.repository.model.JAppUser;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock private AppUserRepository appUserRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private TeacherRepository teacherRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private AccountService accountService;

  @BeforeEach
  void setUp() {
    AppUserMapper appUserMapper = new AppUserMapper();
    accountService =
        new AccountService(
            appUserRepository,
            studentRepository,
            teacherRepository,
            passwordEncoder,
            new StudentMapper(appUserMapper),
            new TeacherMapper(appUserMapper));
  }

  @Test
  void createStudent_should_reject_duplicate_email() {
    when(appUserRepository.findByEmail("etu@hei.school")).thenReturn(Optional.of(new JAppUser()));

    assertThatThrownBy(
            () ->
                accountService.createStudent(
                    "etu@hei.school", "pwd", "Grace", "Hopper", Track.EL, 2024))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void createStudent_should_hash_password_and_save() {
    when(appUserRepository.findByEmail("etu@hei.school")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("pwd")).thenReturn("hashed-pwd");
    when(appUserRepository.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));
    when(studentRepository.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

    var student =
        accountService.createStudent("etu@hei.school", "pwd", "Grace", "Hopper", Track.EL, 2024);

    assertThat(student.firstName()).isEqualTo("Grace");
    assertThat(student.lastName()).isEqualTo("Hopper");
    assertThat(student.track()).isEqualTo(Track.EL);
    assertThat(student.enrollmentYear()).isEqualTo(2024);
    assertThat(student.user().passwordHash()).isEqualTo("hashed-pwd");
    assertThat(student.user().role()).isEqualTo(Role.STUDENT);
  }

  @Test
  void createTeacher_should_reject_duplicate_email() {
    when(appUserRepository.findByEmail("prof@hei.school")).thenReturn(Optional.of(new JAppUser()));

    assertThatThrownBy(
            () -> accountService.createTeacher("prof@hei.school", "pwd", "Ada", "Lovelace"))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void createTeacher_should_hash_password_and_save() {
    when(appUserRepository.findByEmail("prof@hei.school")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("pwd")).thenReturn("hashed-pwd");
    when(appUserRepository.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));
    when(teacherRepository.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

    var teacher = accountService.createTeacher("prof@hei.school", "pwd", "Ada", "Lovelace");

    assertThat(teacher.firstName()).isEqualTo("Ada");
    assertThat(teacher.lastName()).isEqualTo("Lovelace");
    assertThat(teacher.user().passwordHash()).isEqualTo("hashed-pwd");
    assertThat(teacher.user().role()).isEqualTo(Role.TEACHER);
  }

  @Test
  void createAdmin_should_reject_duplicate_email() {
    when(appUserRepository.findByEmail("admin@hei.school")).thenReturn(Optional.of(new JAppUser()));

    assertThatThrownBy(() -> accountService.createAdmin("admin@hei.school", "pwd"))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void createAdmin_should_save_when_email_free() {
    when(appUserRepository.findByEmail("admin@hei.school")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("pwd")).thenReturn("hashed");
    when(appUserRepository.save(ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

    accountService.createAdmin("admin@hei.school", "pwd");

    org.mockito.Mockito.verify(appUserRepository)
        .save(
            ArgumentMatchers.argThat(
                user ->
                    user.getEmail().equals("admin@hei.school")
                        && user.getRole() == Role.ADMIN
                        && user.getPasswordHash().equals("hashed")));
  }
}
