package hei.poja.io.repository;

import hei.poja.io.repository.model.JCourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<JCourseAssignment, UUID> {
    List<JCourseAssignment> findByTeacherIdAndAcademicYear(UUID teacherId, int academicYear);

    List<JCourseAssignment> findByCourseIdAndAcademicYear(UUID courseId, int academicYear);

    List<JCourseAssignment> findByGroupId(UUID groupId);
}
