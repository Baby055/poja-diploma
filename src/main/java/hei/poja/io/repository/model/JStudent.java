package hei.poja.io.repository.model;

import hei.poja.io.model.Track;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JStudent {
  @Id private UUID id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private JAppUser user;

  private String firstName;
  private String lastName;

  @Enumerated(EnumType.STRING)
  private Track track;

  private int enrollmentYear;
}
