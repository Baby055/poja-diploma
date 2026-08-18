package hei.poja.io.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import hei.poja.io.conf.FacadeIT;
import hei.poja.io.model.Track;
import hei.poja.io.service.AccountService;
import hei.poja.io.service.CourseAssignmentService;
import hei.poja.io.service.CourseService;
import hei.poja.io.service.GroupService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

class AccountAndGradeFlowIT extends FacadeIT {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private AccountService accountService;
    @Autowired private CourseService courseService;
    @Autowired private GroupService groupService;
    @Autowired private CourseAssignmentService courseAssignmentService;

    @Test
    void teacher_can_grade_only_assigned_course_and_grade_history_is_kept() {
        accountService.createAdmin("admin@hei.school", "adminPwd");
        var teacher = accountService.createTeacher("prof@hei.school", "profPwd", "Ada", "Lovelace");
        var otherTeacher = accountService.createTeacher("prof2@hei.school", "prof2Pwd", "Alan", "Turing");
        var student =
                accountService.createStudent("etu@hei.school", "etuPwd", "Grace", "Hopper", Track.EL, 2024);

        var course = courseService.create("ALG101", "Algorithmique", 5, Track.EL);
        var groupA = groupService.create("EL1-A", Track.EL, 2024);
        groupService.changeGroup(student.id(), groupA.id());
        courseAssignmentService.create(course.id(), teacher.id(), groupA.id(), 2024, 1);

        var examResponse =
                postAs(
                        "prof@hei.school",
                        "profPwd",
                        "/exams",
                        Map.of(
                                "courseId", course.id().toString(),
                                "title", "Examen final",
                                "dateExam", Instant.now().toString(),
                                "coefficient", "1",
                                "academicYear", 2024,
                                "semester", 1));
        assertThat(examResponse.getStatusCode()).isEqualTo(CREATED);
        String examId = (String) examResponse.getBody().get("id");

        var unknownExam =
                postAs(
                        "prof@hei.school",
                        "profPwd",
                        "/students/" + student.id() + "/grades",
                        gradeBody(UUID.randomUUID().toString(), "10", null));
        assertThat(unknownExam.getStatusCode()).isEqualTo(NOT_FOUND);

        var firstGrade =
                postAs(
                        "prof@hei.school", "profPwd", "/students/" + student.id() + "/grades", gradeBody(examId, "12", null));
        assertThat(firstGrade.getStatusCode()).isEqualTo(CREATED);
        assertThat(firstGrade.getBody().get("value")).isEqualTo(12.0);

        var forbidden =
                postAs(
                        "prof2@hei.school",
                        "prof2Pwd",
                        "/students/" + student.id() + "/grades",
                        gradeBody(examId, "15", "tentative non autorisee"));
        assertThat(forbidden.getStatusCode()).isEqualTo(FORBIDDEN);

        var missingReason =
                postAs("prof@hei.school", "profPwd", "/students/" + student.id() + "/grades", gradeBody(examId, "14", null));
        assertThat(missingReason.getStatusCode().value()).isEqualTo(400);

        var corrected =
                postAs(
                        "prof@hei.school",
                        "profPwd",
                        "/students/" + student.id() + "/grades",
                        gradeBody(examId, "14", "erreur de saisie initiale"));
        assertThat(corrected.getStatusCode()).isEqualTo(CREATED);
        assertThat(corrected.getBody().get("value")).isEqualTo(14.0);

        var ownGrades = getAs("etu@hei.school", "etuPwd", "/students/" + student.id() + "/grades", List.class);
        assertThat(ownGrades.getStatusCode()).isEqualTo(OK);
        assertThat(ownGrades.getBody()).hasSize(1);

        var groupB = groupService.create("EL1-B", Track.EL, 2024);
        var changeGroup =
                postAs(
                        "admin@hei.school",
                        "adminPwd",
                        "/students/" + student.id() + "/group",
                        Map.of("newGroupId", groupB.id().toString()));
        assertThat(changeGroup.getStatusCode()).isEqualTo(CREATED);

        var groupHistory =
                getAs("etu@hei.school", "etuPwd", "/students/" + student.id() + "/group-history", List.class);
        assertThat(groupHistory.getBody()).hasSize(2);

        var gradesAfterGroupChange =
                getAs("etu@hei.school", "etuPwd", "/students/" + student.id() + "/grades", List.class);
        assertThat(gradesAfterGroupChange.getBody()).hasSize(1);

        var otherStudent =
                accountService.createStudent("etu2@hei.school", "etu2Pwd", "Linus", "Torvalds", Track.EL, 2024);
        var forbiddenHistory =
                getAs("etu2@hei.school", "etu2Pwd", "/students/" + student.id() + "/group-history", List.class);
        assertThat(forbiddenHistory.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    private Map<String, Object> gradeBody(String examId, String value, String reason) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("examId", examId);
        body.put("value", value);
        body.put("reason", reason);
        return body;
    }

    private ResponseEntity<Map> postAs(String email, String password, String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(email, password);
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
    }

    private <T> ResponseEntity<T> getAs(String email, String password, String path, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(email, password);
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }
}