package hei.poja.io.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.CourseMapper;
import hei.poja.io.model.Track;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.model.JCourse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

  @Mock private CourseRepository repository;

  private CourseService courseService;

  @BeforeEach
  void setUp() {
    courseService = new CourseService(repository, new CourseMapper());
  }

  @Test
  void findAll_maps_entities() {
    JCourse entity =
        JCourse.builder()
            .id(UUID.randomUUID())
            .ref("A")
            .title("t")
            .credits(1)
            .track(Track.EL)
            .build();
    when(repository.findAll()).thenReturn(List.of(entity));

    var courses = courseService.findAll();

    assertThat(courses).hasSize(1);
    assertThat(courses.get(0).ref()).isEqualTo("A");
    assertThat(courses.get(0).title()).isEqualTo("t");
  }

  @Test
  void findById_returns_course_when_found() {
    UUID id = UUID.randomUUID();
    JCourse entity =
        JCourse.builder().id(id).ref("ALG101").title("Algo").credits(5).track(Track.EL).build();
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    var course = courseService.findById(id);

    assertThat(course.ref()).isEqualTo("ALG101");
    assertThat(course.title()).isEqualTo("Algo");
    assertThat(course.credits()).isEqualTo(5);
    assertThat(course.track()).isEqualTo(Track.EL);
  }

  @Test
  void findById_throws_NotFoundException_when_missing() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courseService.findById(id)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void create_saves_new_course() {
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var course = courseService.create("ALG101", "Algo", 5, Track.EL);

    assertThat(course.ref()).isEqualTo("ALG101");
    assertThat(course.title()).isEqualTo("Algo");
    assertThat(course.credits()).isEqualTo(5);
    assertThat(course.track()).isEqualTo(Track.EL);
  }

  @Test
  void update_modifies_existing_course() {
    UUID id = UUID.randomUUID();
    JCourse existing =
        JCourse.builder().id(id).ref("OLD").title("old").credits(1).track(Track.EL).build();
    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var updated = courseService.update(id, "NEW", "new", 3, Track.TN);

    assertThat(updated.ref()).isEqualTo("NEW");
    assertThat(updated.title()).isEqualTo("new");
    assertThat(updated.credits()).isEqualTo(3);
    assertThat(updated.track()).isEqualTo(Track.TN);
  }

  @Test
  void update_throws_NotFoundException_when_missing() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courseService.update(id, "REF", "title", 5, Track.EL))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void deleteById_delegates_to_repository() {
    UUID id = UUID.randomUUID();
    courseService.deleteById(id);
    Mockito.verify(repository).deleteById(id);
  }
}
