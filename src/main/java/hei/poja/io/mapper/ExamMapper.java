package hei.poja.io.mapper;

import hei.poja.io.model.Exam;
import hei.poja.io.repository.model.JExam;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExamMapper {
  private final CourseMapper courseMapper;

  public List<Exam> toModel(List<JExam> jExams) {
    return jExams.stream().map(this::toModel).toList();
  }

  public Exam toModel(JExam jExam) {
    return Exam.builder()
        .id(jExam.getId())
        .course(courseMapper.toModel(jExam.getCourse()))
        .title(jExam.getTitle())
        .dateExam(jExam.getDateExam())
        .coefficient(jExam.getCoefficient())
        .academicYear(jExam.getAcademicYear())
        .semester(jExam.getSemester())
        .build();
  }

  public List<JExam> toEntity(List<Exam> exams) {
    return exams.stream().map(this::toEntity).toList();
  }

  public JExam toEntity(Exam exam) {
    return JExam.builder()
        .id(exam.id())
        .course(courseMapper.toEntity(exam.course()))
        .title(exam.title())
        .dateExam(exam.dateExam())
        .coefficient(exam.coefficient())
        .academicYear(exam.academicYear())
        .semester(exam.semester())
        .build();
  }
}
