package hei.poja.io.repository.model;

import hei.poja.io.model.Track;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JStudent implements Persistable<UUID> {
  @Id private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  private JAppUser user;

  private String firstName;
  private String lastName;

  @Enumerated(EnumType.STRING)
  private Track track;

  private int enrollmentYear;

  @Override
  public boolean isNew() {
    return true;
  }
}
