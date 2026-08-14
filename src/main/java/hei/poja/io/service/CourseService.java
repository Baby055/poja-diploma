package hei.poja.io.service;

import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.CourseMapper;
import hei.poja.io.model.Course;
import hei.poja.io.model.Track;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.model.JCourse;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;
  private final CourseMapper mapper;

  public List<Course> findAll() {
    return mapper.toModel(repository.findAll());
  }

  public Course findById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new NotFoundException("Course introuvable")));
  }

  public Course create(String ref, String title, int credits, Track track) {
    JCourse course =
        JCourse.builder()
            .id(UUID.randomUUID())
            .ref(ref)
            .title(title)
            .credits(credits)
            .track(track)
            .build();
    return mapper.toModel(repository.save(course));
  }

  public Course update(UUID id, String ref, String title, int credits, Track track) {
    JCourse course =
        repository.findById(id).orElseThrow(() -> new NotFoundException("Course introuvable"));
    course.setRef(ref);
    course.setTitle(title);
    course.setCredits(credits);
    course.setTrack(track);
    return mapper.toModel(repository.save(course));
  }

  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
