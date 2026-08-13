package hei.poja.io.repository.model;

import hei.poja.io.model.Track;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JCourse {
  @Id private UUID id;

  private String ref;
  private String title;
  private int credits;

  @Enumerated(EnumType.STRING)
  private Track track;
}
