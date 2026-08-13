package hei.poja.io.mapper;

import hei.poja.io.model.GradeHistory;
import hei.poja.io.repository.model.JGradeHistory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class GradeHistoryMapper {
    private final GradeMapper gradeMapper;
    private final AppUserMapper appUserMapper;

    public List<GradeHistory> toModel(List<JGradeHistory> jGradeHistories) {
        return jGradeHistories.stream().map(this::toModel).toList();
    }

    public GradeHistory toModel(JGradeHistory jGradeHistory) {
        return GradeHistory.builder()
                .id(jGradeHistory.getId())
                .grade(gradeMapper.toModel(jGradeHistory.getGrade()))
                .previousValue(jGradeHistory.getPreviousValue())
                .newValue(jGradeHistory.getNewValue())
                .reason(jGradeHistory.getReason())
                .modifiedBy(appUserMapper.toModel(jGradeHistory.getModifiedBy()))
                .modifiedAt(jGradeHistory.getModifiedAt())
                .build();
    }

    public List<JGradeHistory> toEntity(List<GradeHistory> gradeHistories) {
        return gradeHistories.stream().map(this::toEntity).toList();
    }

    public JGradeHistory toEntity(GradeHistory gradeHistory) {
        return JGradeHistory.builder()
                .id(gradeHistory.id())
                .grade(gradeMapper.toEntity(gradeHistory.grade()))
                .previousValue(gradeHistory.previousValue())
                .newValue(gradeHistory.newValue())
                .reason(gradeHistory.reason())
                .modifiedBy(appUserMapper.toEntity(gradeHistory.modifiedBy()))
                .modifiedAt(gradeHistory.modifiedAt())
                .build();
    }
}