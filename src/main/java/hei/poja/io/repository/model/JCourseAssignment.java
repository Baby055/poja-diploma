package hei.poja.io.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "course_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JCourseAssignment {
  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private JCourse course;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "teacher_id")
  private JTeacher teacher;

  @ManyToOne(fetch = FetchType.LAZY)
  private JGroup group;

  private int academicYear;

  private int semester;
}
