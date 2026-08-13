package hei.poja.io.repository;

import hei.poja.io.repository.model.JExam;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<JExam, UUID> {
  List<JExam> findByCourseId(UUID courseId);

  List<JExam> findByCourseIdIn(List<UUID> courseIds);
}
