package hei.poja.io.mapper;

import hei.poja.io.model.Group;
import hei.poja.io.repository.model.JGroup;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

  public List<Group> toModel(List<JGroup> jGroups) {
    return jGroups.stream().map(this::toModel).toList();
  }

  public Group toModel(JGroup jGroup) {
    return Group.builder()
        .id(jGroup.getId())
        .ref(jGroup.getRef())
        .track(jGroup.getTrack())
        .academicYear(jGroup.getAcademicYear())
        .build();
  }

  public List<JGroup> toEntity(List<Group> groups) {
    return groups.stream().map(this::toEntity).toList();
  }

  public JGroup toEntity(Group group) {
    return JGroup.builder()
        .id(group.id())
        .ref(group.ref())
        .track(group.track())
        .academicYear(group.academicYear())
        .build();
  }
}
