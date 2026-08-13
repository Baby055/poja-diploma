package hei.poja.io.repository;

import hei.poja.io.model.Track;
import hei.poja.io.repository.model.JCourse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<JCourse, UUID> {
  List<JCourse> findByTrackIsNullOrTrack(Track track);
}
