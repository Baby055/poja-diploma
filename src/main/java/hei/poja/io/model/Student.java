package hei.poja.io.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Student(
    UUID id, AppUser user, String firstName, String lastName, Track track, int enrollmentYear) {}
