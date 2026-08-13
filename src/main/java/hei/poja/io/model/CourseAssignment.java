package hei.poja.io.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CourseAssignment(
    UUID id, Course course, Teacher teacher, Group group, int academicYear, int semester) {}
