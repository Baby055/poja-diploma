package hei.poja.io.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Group(UUID id, String ref, Track track, int academicYear) {}
