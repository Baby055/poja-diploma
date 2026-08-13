package hei.poja.io.repository;

import hei.poja.io.repository.model.JStudentGroupHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentGroupHistoryRepository extends JpaRepository<JStudentGroupHistory, UUID> {
  List<JStudentGroupHistory> findByStudentIdOrderByFromDateDesc(UUID studentId);

  Optional<JStudentGroupHistory> findByStudentIdAndToDateIsNull(UUID studentId);
}
