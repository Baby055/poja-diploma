package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.Group;
import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JGroup;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupMapperTest {

    private final GroupMapper mapper = new GroupMapper();

    @Test
    void toModel_maps_all_fields() {
        UUID id = UUID.randomUUID();
        JGroup entity = JGroup.builder().id(id).ref("EL1-A").track(Track.EL).academicYear(2024).build();

        Group model = mapper.toModel(entity);

        assertThat(model.id()).isEqualTo(id);
        assertThat(model.ref()).isEqualTo("EL1-A");
        assertThat(model.academicYear()).isEqualTo(2024);
    }

    @Test
    void toEntity_maps_all_fields() {
        UUID id = UUID.randomUUID();
        Group model = Group.builder().id(id).ref("TN1-A").track(Track.TN).academicYear(2023).build();

        JGroup entity = mapper.toEntity(model);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTrack()).isEqualTo(Track.TN);
    }
}