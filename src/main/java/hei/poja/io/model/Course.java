package hei.poja.io.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Course(UUID id, String ref, String title, int credits, Track track) {}
