package hei.poja.io.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.UUID;

@Entity
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
    private JTeacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    private JGroup group;

    private int academicYear;

    private int semester;
}
