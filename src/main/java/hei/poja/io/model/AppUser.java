package hei.poja.io.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AppUser (
        UUID id,
        String email,
        @JsonIgnore String passwordHash,
        Role role
){}
