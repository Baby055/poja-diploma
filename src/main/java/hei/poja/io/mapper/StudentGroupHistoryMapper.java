package hei.poja.io.mapper;

import hei.poja.io.model.StudentGroupHistory;
import hei.poja.io.repository.model.JStudentGroupHistory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class StudentGroupHistoryMapper {
    private final StudentMapper studentMapper;
    private final GroupMapper groupMapper;

    public List<StudentGroupHistory> toModel(List<JStudentGroupHistory> jStudentGroupHistories) {
        return jStudentGroupHistories.stream().map(this::toModel).toList();
    }

    public StudentGroupHistory toModel(JStudentGroupHistory jStudentGroupHistory) {
        return StudentGroupHistory.builder()
                .id(jStudentGroupHistory.getId())
                .student(studentMapper.toModel(jStudentGroupHistory.getStudent()))
                .group(groupMapper.toModel(jStudentGroupHistory.getGroup()))
                .fromDate(jStudentGroupHistory.getFromDate())
                .toDate(jStudentGroupHistory.getToDate())
                .build();
    }

    public List<JStudentGroupHistory> toEntity(List<StudentGroupHistory> studentGroupHistories) {
        return studentGroupHistories.stream().map(this::toEntity).toList();
    }

    public JStudentGroupHistory toEntity(StudentGroupHistory studentGroupHistory) {
        return JStudentGroupHistory.builder()
                .id(studentGroupHistory.id())
                .student(studentMapper.toEntity(studentGroupHistory.student()))
                .group(groupMapper.toEntity(studentGroupHistory.group()))
                .fromDate(studentGroupHistory.fromDate())
                .toDate(studentGroupHistory.toDate())
                .build();
    }
}
