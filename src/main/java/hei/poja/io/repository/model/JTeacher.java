package hei.poja.io.repository.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "teacher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JTeacher {
  @Id private UUID id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "id")
  private JAppUser user;

  private String firstName;
  private String lastName;
}
