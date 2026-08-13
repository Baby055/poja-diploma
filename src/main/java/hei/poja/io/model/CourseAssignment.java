package hei.poja.io.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CourseAssignment (
        UUID id,
        Course course,
        Teacher teacher,
        Group group,
        int academicYear,
        int semester
){}
