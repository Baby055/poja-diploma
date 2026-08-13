package hei.poja.io.repository.model;

import hei.poja.io.model.Track;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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
    private JAppUser user;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Track track;

    private int enrollmentYear;
}
