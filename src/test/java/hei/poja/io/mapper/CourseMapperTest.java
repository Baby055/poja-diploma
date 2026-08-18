package hei.poja.io.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import hei.poja.io.model.Course;
import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JCourse;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseMapperTest {

  private final CourseMapper mapper = new CourseMapper();

  @Test
  void toModel_maps_all_fields() {
    UUID id = UUID.randomUUID();
    JCourse entity =
        JCourse.builder().id(id).ref("ALG101").title("Algo").credits(5).track(Track.EL).build();

    Course model = mapper.toModel(entity);

    assertThat(model.id()).isEqualTo(id);
    assertThat(model.ref()).isEqualTo("ALG101");
    assertThat(model.credits()).isEqualTo(5);
    assertThat(model.track()).isEqualTo(Track.EL);
  }

  @Test
  void toModel_handles_null_track_as_tronc_commun() {
    JCourse entity =
        JCourse.builder()
            .id(UUID.randomUUID())
            .ref("COM101")
            .title("Commun")
            .credits(2)
            .track(null)
            .build();

    assertThat(mapper.toModel(entity).track()).isNull();
  }

  @Test
  void toEntity_maps_all_fields() {
    UUID id = UUID.randomUUID();
    Course model =
        Course.builder().id(id).ref("RES101").title("Reseaux").credits(4).track(Track.TN).build();

    JCourse entity = mapper.toEntity(model);

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getTrack()).isEqualTo(Track.TN);
  }
}
