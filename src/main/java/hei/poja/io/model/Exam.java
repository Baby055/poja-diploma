package hei.poja.io.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Exam(
    UUID id,
    Course course,
    String title,
    Instant dateExam,
    BigDecimal coefficient,
    int academicYear,
    int semester) {}
