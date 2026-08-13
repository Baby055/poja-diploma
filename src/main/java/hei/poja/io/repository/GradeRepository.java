package hei.poja.io.repository;

import hei.poja.io.repository.model.JGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<JGrade, UUID> {
    List<JGrade> findByStudentId(UUID studentId);

    Optional<JGrade> findByStudentIdAndExamId(UUID studentId, UUID examId);

    List<JGrade> findByStudentIdAndExam_CourseId(UUID studentId, UUID courseId);
}
