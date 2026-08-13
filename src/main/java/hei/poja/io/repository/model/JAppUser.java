package hei.poja.io.repository.model;

import hei.poja.io.model.Role;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JAppUser {
  @Id private UUID id;

  private String email;
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  private Role role;
}
