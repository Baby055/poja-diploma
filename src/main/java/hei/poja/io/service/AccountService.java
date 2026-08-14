package hei.poja.io.service;

import hei.poja.io.exception.ConflictException;
import hei.poja.io.mapper.StudentMapper;
import hei.poja.io.mapper.TeacherMapper;
import hei.poja.io.model.Role;
import hei.poja.io.model.Student;
import hei.poja.io.model.Teacher;
import hei.poja.io.model.Track;
import hei.poja.io.repository.AppUserRepository;
import hei.poja.io.repository.StudentRepository;
import hei.poja.io.repository.TeacherRepository;
import hei.poja.io.repository.model.JAppUser;
import hei.poja.io.repository.model.JStudent;
import hei.poja.io.repository.model.JTeacher;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {
    private final AppUserRepository appUserRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    @Transactional
    public Student createStudent(
            String email,
            String rawPassword,
            String firstName,
            String lastName,
            Track track,
            int enrollmentYear){
        JAppUser user = newUser(email, rawPassword, Role.STUDENT);

        JStudent student =
                JStudent.builder()
                        .id(user.getId())
                        .user(user)
                        .firstName(firstName)
                        .lastName(lastName)
                        .track(track)
                        .enrollmentYear(enrollmentYear)
                        .build();
        return studentMapper.toModel(studentRepository.save(student));
    }

    @Transactional
    public Teacher createTeacher(
            String email,
            String rawPassword,
            String firstName,
            String lastName){
        JAppUser user = newUser(email, rawPassword, Role.TEACHER);

        JTeacher teacher =
                JTeacher.builder()
                        .id(user.getId())
                        .user(user)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build();
        return teacherMapper.toModel(teacherRepository.save(teacher));
    }

    @Transactional
    public void createAdmin(
            String email,
            String rawPassword){
        newUser(email, rawPassword, Role.ADMIN);
    }

    private JAppUser newUser(String email, String rawPassword, Role role) {
        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Un compte existe deja avec cet email");
        }
        JAppUser user =
                JAppUser.builder()
                        .id(UUID.randomUUID())
                        .email(email)
                        .passwordHash(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .build();
        return appUserRepository.save(user);
    }
}
