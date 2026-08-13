package hei.poja.io.repository.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "student_group_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JStudentGroupHistory {
  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private JStudent student;

  @ManyToOne(fetch = FetchType.LAZY)
  private JGroup group;

  private Instant fromDate;
  private Instant toDate;
}
