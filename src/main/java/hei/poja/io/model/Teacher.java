package hei.poja.io.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record Teacher (
      UUID id,
      AppUser user,
      String firstName,
      String lastName
){}
