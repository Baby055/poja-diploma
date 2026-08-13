package hei.poja.io.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record Student (
        UUID id,
        AppUser user,
        String firstName,
        String lastName,
        Track track,
        int enrollmentYear
){}
