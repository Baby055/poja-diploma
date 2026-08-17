package hei.poja.io.repository.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "teacher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JTeacher implements Persistable<UUID> {
  @Id private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  private JAppUser user;

  private String firstName;
  private String lastName;

  @Override
  public boolean isNew() {
    return true;
  }
}
