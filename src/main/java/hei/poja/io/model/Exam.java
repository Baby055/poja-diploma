package hei.poja.io.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record Exam (
        UUID id,
        Course course,
        String title,
        Instant dateExam,
        BigDecimal coefficient,
        int academicYear,
        int semester
){}
