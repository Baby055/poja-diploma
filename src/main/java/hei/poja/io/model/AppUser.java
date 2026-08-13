package hei.poja.io.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AppUser(UUID id, String email, @JsonIgnore String passwordHash, Role role) {}
