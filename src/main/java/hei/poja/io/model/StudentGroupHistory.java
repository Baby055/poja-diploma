package hei.poja.io.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StudentGroupHistory(
    UUID id, Student student, Group group, Instant fromDate, Instant toDate) {}
