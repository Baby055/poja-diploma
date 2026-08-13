package hei.poja.io.mapper;

import hei.poja.io.model.Grade;
import hei.poja.io.repository.model.JGrade;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GradeMapper {
  private final StudentMapper studentMapper;
  private final ExamMapper examMapper;

  public List<Grade> toModel(List<JGrade> jGrades) {
    return jGrades.stream().map(this::toModel).toList();
  }

  public Grade toModel(JGrade jGrade) {
    return Grade.builder()
        .id(jGrade.getId())
        .student(studentMapper.toModel(jGrade.getStudent()))
        .exam(examMapper.toModel(jGrade.getExam()))
        .value(jGrade.getValue())
        .lastModifiedAt(jGrade.getLastModifiedAt())
        .build();
  }

  public List<JGrade> toEntity(List<Grade> grades) {
    return grades.stream().map(this::toEntity).toList();
  }

  public JGrade toEntity(Grade grade) {
    return JGrade.builder()
        .id(grade.id())
        .student(studentMapper.toEntity(grade.student()))
        .exam(examMapper.toEntity(grade.exam()))
        .value(grade.value())
        .lastModifiedAt(grade.lastModifiedAt())
        .build();
  }
}
