package hei.poja.io.mapper;

import hei.poja.io.model.Student;
import hei.poja.io.repository.model.JStudent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class StudentMapper {
    private final AppUserMapper appUserMapper;

    public List<Student> toModel(List<JStudent> jStudents) {
        return jStudents.stream().map(this::toModel).toList();
    }

    public Student toModel(JStudent jStudent) {
        return Student.builder()
                .id(jStudent.getId())
                .user(appUserMapper.toModel(jStudent.getUser()))
                .firstName(jStudent.getFirstName())
                .lastName(jStudent.getLastName())
                .track(jStudent.getTrack())
                .enrollmentYear(jStudent.getEnrollmentYear())
                .build();
    }

    public List<JStudent> toEntity(List<Student> students) {
        return students.stream().map(this::toEntity).toList();
    }

    public JStudent toEntity(Student student) {
        return JStudent.builder()
                .id(student.id())
                .user(appUserMapper.toEntity(student.user()))
                .firstName(student.firstName())
                .lastName(student.lastName())
                .track(student.track())
                .enrollmentYear(student.enrollmentYear())
                .build();
    }
}
