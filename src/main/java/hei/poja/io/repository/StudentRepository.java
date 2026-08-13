package hei.poja.io.repository;

import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JStudent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<JStudent, UUID> {
  List<JStudent> findByTrackAndEnrollmentYear(Track track, int enrollmentYear);
}
