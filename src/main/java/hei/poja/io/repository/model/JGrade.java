package hei.poja.io.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "grade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JGrade {
  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private JStudent student;

  @ManyToOne(fetch = FetchType.LAZY)
  private JExam exam;

  private BigDecimal value;

  private Instant lastModifiedAt;
}
