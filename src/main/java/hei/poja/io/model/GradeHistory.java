package hei.poja.io.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GradeHistory(
    UUID id,
    Grade grade,
    BigDecimal previousValue,
    BigDecimal newValue,
    String reason,
    AppUser modifiedBy,
    Instant modifiedAt) {}
