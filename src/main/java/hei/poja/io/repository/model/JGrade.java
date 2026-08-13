package hei.poja.io.repository.model;

import hei.poja.io.model.Student;
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
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY)
  private JExam exam;

  private BigDecimal value;

  private Instant lastModifiedAt;
}
