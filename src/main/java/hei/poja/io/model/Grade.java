package hei.poja.io.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record Grade (
        UUID id,
        Student student,
        Exam exam,
        BigDecimal value,
        Instant lastModifiedAt
){}
