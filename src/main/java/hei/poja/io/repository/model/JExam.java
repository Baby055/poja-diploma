package hei.poja.io.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "exam")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JExam {
  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private JCourse course;

  private String title;

  private Instant dateExam;

  private BigDecimal coefficient;

  private int academicYear;

  private int semester;
}
