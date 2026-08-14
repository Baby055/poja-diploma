package hei.poja.io.endpoint.rest.controller.grade;

import hei.poja.io.model.Grade;
import hei.poja.io.security.AppUserDetails;
import hei.poja.io.service.GradeService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/students/{studentId}/grades")
@AllArgsConstructor
public class GradeController {
    private final GradeService gradeService;

    public record SetGradeRequest(UUID examId, BigDecimal value, String reason) {}

    @GetMapping
    public List<Grade> getGrades(
            @PathVariable UUID studentId, @AuthenticationPrincipal AppUserDetails principal) {
        return gradeService.getGradesForStudent(studentId, principal);
    }

    @PostMapping
    public Grade setGrade(
            @PathVariable UUID studentId,
            @RequestBody SetGradeRequest request,
            @AuthenticationPrincipal AppUserDetails principal) {
        return gradeService.setGrade(
                studentId, request.examId(), request.value(), request.reason(), principal.getId());
    }
}
