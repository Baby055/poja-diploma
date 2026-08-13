package hei.poja.io.repository.model;

import hei.poja.io.model.Track;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "app_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JGroup {
    @Id private UUID id;

    private String ref;

    @Enumerated(EnumType.STRING)
    private Track track;

    private int academicYear;
}
