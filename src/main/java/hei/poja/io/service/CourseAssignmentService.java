package hei.poja.io.service;

import hei.poja.io.exception.NotFoundException;
import hei.poja.io.mapper.CourseAssignmentMapper;
import hei.poja.io.model.CourseAssignment;
import hei.poja.io.repository.CourseAssignmentRepository;
import hei.poja.io.repository.CourseRepository;
import hei.poja.io.repository.GroupRepository;
import hei.poja.io.repository.TeacherRepository;
import hei.poja.io.repository.model.JCourseAssignment;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseAssignmentService {
  private final CourseAssignmentRepository repository;
  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;
  private final GroupRepository groupRepository;
  private final CourseAssignmentMapper mapper;

  @Transactional(readOnly = true)
  public List<CourseAssignment> findAll() {
    return mapper.toModel(repository.findAll());
  }

  @Transactional(readOnly = true)
  public CourseAssignment findById(UUID id) {
    return mapper.toModel(
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("CourseAssignment introuvable ")));
  }

  @Transactional
  public CourseAssignment create(
      UUID courseId, UUID teacherId, UUID groupId, int academicYear, int semester) {
    JCourseAssignment assignment =
        JCourseAssignment.builder()
            .id(UUID.randomUUID())
            .course(
                courseRepository
                    .findById(courseId)
                    .orElseThrow(() -> new NotFoundException("Course Introuvable")))
            .teacher(
                teacherRepository
                    .findById(teacherId)
                    .orElseThrow(() -> new NotFoundException("Teacher introuvable")))
            .group(
                groupRepository
                    .findById(groupId)
                    .orElseThrow(() -> new NotFoundException("Group introuvable")))
            .academicYear(academicYear)
            .semester(semester)
            .build();
    return mapper.toModel(repository.save(assignment));
  }

  @Transactional
  public CourseAssignment update(
      UUID id, UUID courseId, UUID teacherId, UUID groupId, int academicYear, int semester) {
    JCourseAssignment assignment =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("CourseAssignment Introuvable"));
    assignment.setCourse(
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course Introuvable")));
    assignment.setTeacher(
        teacherRepository
            .findById(teacherId)
            .orElseThrow(() -> new NotFoundException("Teacher Introuvable")));
    assignment.setGroup(
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new NotFoundException("Group introuvable")));
    assignment.setAcademicYear(academicYear);
    assignment.setSemester(semester);
    return mapper.toModel(repository.save(assignment));
  }

  @Transactional
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
