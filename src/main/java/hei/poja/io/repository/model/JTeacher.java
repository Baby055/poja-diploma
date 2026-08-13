package hei.poja.io.repository.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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
    private JAppUser user;

    private  String firstName;
    private String lastName;
}
