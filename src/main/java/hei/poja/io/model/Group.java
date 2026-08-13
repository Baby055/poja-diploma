package hei.poja.io.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record Group (
        UUID id,
        String ref,
        Track track,
        int academicYear
){}
