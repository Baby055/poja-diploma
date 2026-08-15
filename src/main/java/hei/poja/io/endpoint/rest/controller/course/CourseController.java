package hei.poja.io.endpoint.rest.controller.course;

import hei.poja.io.model.Course;
import hei.poja.io.model.Track;
import hei.poja.io.service.CourseService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {
  private final CourseService courseService;

  public record CourseRequest(String ref, String title, int credits, Track track) {}

  @GetMapping
  public List<Course> getCourses() {
    return courseService.findAll();
  }

  @GetMapping("/{id}")
  public Course getCourse(@PathVariable UUID id) {
    return courseService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Course createCourse(@RequestBody CourseRequest request) {
    return courseService.create(request.ref(), request.title(), request.credits(), request.track());
  }

  @PutMapping("/{id}")
  public Course updateCourse(@PathVariable UUID id, @RequestBody CourseRequest request) {
    return courseService.update(
        id, request.ref(), request.title(), request.credits(), request.track());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCourse(@PathVariable UUID id) {
    courseService.deleteById(id);
  }
}
