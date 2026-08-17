package hei.poja.io.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "grade_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JGradeHistory {
  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private JGrade grade;

  private BigDecimal previousValue;

  private BigDecimal newValue;

  private String reason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "modified_by")
  private JAppUser modifiedBy;

  private Instant modifiedAt;
}
