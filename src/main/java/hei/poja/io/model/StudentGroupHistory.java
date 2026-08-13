package hei.poja.io.model;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record StudentGroupHistory (
        UUID id,
        Student student,
        Group group,
        Instant fromDate,
        Instant toDate
){}
