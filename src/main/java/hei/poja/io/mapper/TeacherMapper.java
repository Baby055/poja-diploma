package hei.poja.io.mapper;

import hei.poja.io.model.Teacher;
import hei.poja.io.repository.model.JTeacher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TeacherMapper {
    private final AppUserMapper appUserMapper;

    public List<Teacher> toModel(List<JTeacher> jTeachers) {
        return jTeachers.stream().map(this::toModel).toList();
    }

    public Teacher toModel(JTeacher jTeacher) {
        return Teacher.builder()
                .id(jTeacher.getId())
                .user(appUserMapper.toModel(jTeacher.getUser()))
                .firstName(jTeacher.getFirstName())
                .lastName(jTeacher.getLastName())
                .build();
    }

    public List<JTeacher> toEntity(List<Teacher> teachers) {
        return teachers.stream().map(this::toEntity).toList();
    }

    public JTeacher toEntity(Teacher teacher) {
        return JTeacher.builder()
                .id(teacher.id())
                .user(appUserMapper.toEntity(teacher.user()))
                .firstName(teacher.firstName())
                .lastName(teacher.lastName())
                .build();
    }
}
