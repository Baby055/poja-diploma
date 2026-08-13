package hei.poja.io.mapper;

import hei.poja.io.model.CourseAssignment;
import hei.poja.io.repository.model.JCourseAssignment;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CourseAssignmentMapper {
  private final CourseMapper courseMapper;
  private final TeacherMapper teacherMapper;
  private final GroupMapper groupMapper;

  public List<CourseAssignment> toModel(List<JCourseAssignment> jCourseAssignments) {
    return jCourseAssignments.stream().map(this::toModel).toList();
  }

  public CourseAssignment toModel(JCourseAssignment jCourseAssignment) {
    return CourseAssignment.builder()
        .id(jCourseAssignment.getId())
        .course(courseMapper.toModel(jCourseAssignment.getCourse()))
        .teacher(teacherMapper.toModel(jCourseAssignment.getTeacher()))
        .group(groupMapper.toModel(jCourseAssignment.getGroup()))
        .academicYear(jCourseAssignment.getAcademicYear())
        .semester(jCourseAssignment.getSemester())
        .build();
  }

  public List<JCourseAssignment> toEntity(List<CourseAssignment> courseAssignments) {
    return courseAssignments.stream().map(this::toEntity).toList();
  }

  public JCourseAssignment toEntity(CourseAssignment courseAssignment) {
    return JCourseAssignment.builder()
        .id(courseAssignment.id())
        .course(courseMapper.toEntity(courseAssignment.course()))
        .teacher(teacherMapper.toEntity(courseAssignment.teacher()))
        .group(groupMapper.toEntity(courseAssignment.group()))
        .academicYear(courseAssignment.academicYear())
        .semester(courseAssignment.semester())
        .build();
  }
}
