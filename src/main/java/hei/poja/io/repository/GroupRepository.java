package hei.poja.io.repository;

import hei.poja.io.repository.model.JGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<JGroup, UUID> {
  List<JGroup> findByAcademicYear(int academicYear);
}
