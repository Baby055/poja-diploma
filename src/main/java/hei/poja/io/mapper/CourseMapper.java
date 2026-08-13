package hei.poja.io.mapper;

import hei.poja.io.model.Course;
import hei.poja.io.repository.model.JCourse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

  public List<Course> toModel(List<JCourse> jCourses) {
    return jCourses.stream().map(this::toModel).toList();
  }

  public Course toModel(JCourse jCourse) {
    return Course.builder()
        .id(jCourse.getId())
        .ref(jCourse.getRef())
        .title(jCourse.getTitle())
        .credits(jCourse.getCredits())
        .track(jCourse.getTrack())
        .build();
  }

  public List<JCourse> toEntity(List<Course> courses) {
    return courses.stream().map(this::toEntity).toList();
  }

  public JCourse toEntity(Course course) {
    return JCourse.builder()
        .id(course.id())
        .ref(course.ref())
        .title(course.title())
        .credits(course.credits())
        .track(course.track())
        .build();
  }
}
