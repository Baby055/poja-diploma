package hei.poja.io.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import hei.poja.io.conf.FacadeIT;
import hei.poja.io.exception.ConflictException;
import hei.poja.io.model.Track;
import hei.poja.io.service.AccountService;
import hei.poja.io.service.CourseAssignmentService;
import hei.poja.io.service.CourseService;
import hei.poja.io.service.GradeService;
import hei.poja.io.service.GroupService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

class AccountAndGradeFlowIT extends FacadeIT {

    private static final String ADMIN_EMAIL = "admin-flow@hei.school";
    private static final String ADMIN_PWD = "adminPwd";
    private static final String TEACHER_EMAIL = "prof-flow@hei.school";
    private static final String TEACHER_PWD = "profPwd";
    private static final String OTHER_TEACHER_EMAIL = "prof2-flow@hei.school";
    private static final String OTHER_TEACHER_PWD = "prof2Pwd";
    private static final String STUDENT_EMAIL = "etu-flow@hei.school";
    private static final String STUDENT_PWD = "etuPwd";
    private static final String OTHER_STUDENT_EMAIL = "etu2-flow@hei.school";
    private static final String OTHER_STUDENT_PWD = "etu2Pwd";
    private static final int ACADEMIC_YEAR = 2024;

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private AccountService accountService;
    @Autowired private CourseService courseService;
    @Autowired private GroupService groupService;
    @Autowired private CourseAssignmentService courseAssignmentService;
    @Autowired private GradeService gradeService;

    private UUID cachedStudentId;
    private UUID cachedTeacherId;
    private UUID cachedCourseId;

    private void setupAccountsAndCourse() {
        createAdminSafely(ADMIN_EMAIL, ADMIN_PWD);
        var teacher = createTeacherSafely(TEACHER_EMAIL, TEACHER_PWD, "Ada", "Lovelace");
        createTeacherSafely(OTHER_TEACHER_EMAIL, OTHER_TEACHER_PWD, "Alan", "Turing");
        var student = createStudentSafely(STUDENT_EMAIL, STUDENT_PWD, "Grace", "Hopper", Track.EL, ACADEMIC_YEAR);
        var course = courseService.create("ALG101-flow", "Algorithmique", 5, Track.EL);

        cachedStudentId = student.id();
        cachedTeacherId = teacher.id();
        cachedCourseId = course.id();
    }

    private UUID studentId() { return cachedStudentId; }
    private UUID teacherId() { return cachedTeacherId; }
    private UUID courseId() { return cachedCourseId; }

    private void createAdminSafely(String email, String pwd) {
        try { accountService.createAdmin(email, pwd); } catch (ConflictException ignored) {}
    }

    private hei.poja.io.model.Teacher createTeacherSafely(String email, String pwd, String first, String last) {
        try { return accountService.createTeacher(email, pwd, first, last); } catch (ConflictException e) {
            return accountService.createTeacher(email + "-retry", pwd, first, last);
        }
    }

    private hei.poja.io.model.Student createStudentSafely(String email, String pwd, String first, String last, Track track, int year) {
        try { return accountService.createStudent(email, pwd, first, last, track, year); } catch (ConflictException e) {
            return accountService.createStudent(email + "-retry", pwd, first, last, track, year);
        }
    }

    @Nested
    @DisplayName("Admin creates accounts via REST")
    class AccountCreationFlow {

        @Test
        @DisplayName("Admin can create student, teacher, and admin accounts")
        void admin_can_create_accounts_via_rest() {
            setupAccountsAndCourse();

            var studentResp = postAs(ADMIN_EMAIL, ADMIN_PWD, "/students",
                    new CreateStudentRequest("student-rest@hei.flow", "pwd", "Grace", "Hopper", Track.EL, ACADEMIC_YEAR));
            assertThat(studentResp.getStatusCode()).as("Admin creating student").isEqualTo(CREATED);

            var teacherResp = postAs(ADMIN_EMAIL, ADMIN_PWD, "/teachers",
                    new CreateTeacherRequest("teacher-rest@hei.flow", "pwd", "Ada", "Lovelace"));
            assertThat(teacherResp.getStatusCode()).as("Admin creating teacher").isEqualTo(CREATED);

            var adminResp = postAs(ADMIN_EMAIL, ADMIN_PWD, "/admins",
                    new CreateAdminRequest("admin-rest@hei.flow", "pwd"));
            assertThat(adminResp.getStatusCode()).as("Admin creating another admin").isEqualTo(CREATED);
        }

        @Test
        @DisplayName("Duplicate email returns CONFLICT")
        void duplicate_account_returns_conflict() {
            setupAccountsAndCourse();

            var first = postAs(ADMIN_EMAIL, ADMIN_PWD, "/students",
                    new CreateStudentRequest("dup-flow@hei.school", "pwd", "A", "B", Track.EL, ACADEMIC_YEAR));
            assertThat(first.getStatusCode()).as("First creation").isEqualTo(CREATED);

            var duplicate = postAs(ADMIN_EMAIL, ADMIN_PWD, "/students",
                    new CreateStudentRequest("dup-flow@hei.school", "pwd", "A", "B", Track.EL, ACADEMIC_YEAR));
            assertThat(duplicate.getStatusCode()).as("Duplicate creation").isEqualTo(CONFLICT);
        }

        @Test
        @DisplayName("Unauthenticated request returns UNAUTHORIZED")
        void unauthenticated_request_returns_401() {
            var resp = restTemplate.exchange("/students", HttpMethod.POST,
                    new HttpEntity<>(Map.of(
                            "email", "x-flow@hei.school",
                            "password", "pwd",
                            "firstName", "A",
                            "lastName", "B",
                            "track", "EL",
                            "enrollmentYear", ACADEMIC_YEAR)),
                    Map.class);
            assertThat(resp.getStatusCode()).as("Unauthenticated access").isEqualTo(UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("Exam creation and grading flow")
    class ExamAndGradingFlow {

        @Test
        @DisplayName("Teacher creates exam, grades student, forbidden for unassigned teacher, correction requires reason")
        void teacher_grades_student_with_full_validation() {
            setupAccountsAndCourse();

            var examResponse = postAs(TEACHER_EMAIL, TEACHER_PWD, "/exams",
                    new ExamRequest(courseId(), "Examen final", Instant.now(), BigDecimal.ONE, ACADEMIC_YEAR, 1));
            assertThat(examResponse.getStatusCode()).as("Create exam").isEqualTo(CREATED);
            String examId = String.valueOf(examResponse.getBody().get("id"));

            var unknownExam = postAs(TEACHER_EMAIL, TEACHER_PWD,
                    "/students/" + studentId() + "/grades",
                    new GradeRequest(UUID.randomUUID(), new BigDecimal("10"), null));
            assertThat(unknownExam.getStatusCode()).as("Grade with unknown exam").isEqualTo(NOT_FOUND);

            var firstGrade = postAs(TEACHER_EMAIL, TEACHER_PWD,
                    "/students/" + studentId() + "/grades",
                    new GradeRequest(UUID.fromString(examId), new BigDecimal("12"), null));
            assertThat(firstGrade.getStatusCode()).as("First grade").isEqualTo(CREATED);
            assertThat(firstGrade.getBody().get("value")).as("Grade value").isEqualTo(12.0);

            var forbidden = postAs(OTHER_TEACHER_EMAIL, OTHER_TEACHER_PWD,
                    "/students/" + studentId() + "/grades",
                    new GradeRequest(UUID.fromString(examId), new BigDecimal("15"), "tentative non autorisee"));
            assertThat(forbidden.getStatusCode()).as("Unassigned teacher grades").isEqualTo(FORBIDDEN);

            var missingReason = postAs(TEACHER_EMAIL, TEACHER_PWD,
                    "/students/" + studentId() + "/grades",
                    new GradeRequest(UUID.fromString(examId), new BigDecimal("14"), null));
            assertThat(missingReason.getStatusCode().value()).as("Correction without reason").isEqualTo(400);

            var corrected = postAs(TEACHER_EMAIL, TEACHER_PWD,
                    "/students/" + studentId() + "/grades",
                    new GradeRequest(UUID.fromString(examId), new BigDecimal("14"), "erreur de saisie initiale"));
            assertThat(corrected.getStatusCode()).as("Corrected grade").isEqualTo(CREATED);
            assertThat(corrected.getBody().get("value")).as("Corrected grade value").isEqualTo(14.0);

            var ownGrades = getAs(STUDENT_EMAIL, STUDENT_PWD,
                    "/students/" + studentId() + "/grades", List.class);
            assertThat(ownGrades.getStatusCode()).as("View own grades").isEqualTo(OK);
            assertThat(ownGrades.getBody()).as("Grades count").hasSize(1);
        }

        @Test
        @DisplayName("Admin can also grade a student (admin bypass)")
        void admin_can_also_grade() {
            setupAccountsAndCourse();

            var examResponse = postAs(TEACHER_EMAIL, TEACHER_PWD, "/exams",
                    new ExamRequest(courseId(), "Examen final", Instant.now(), BigDecimal.ONE, ACADEMIC_YEAR, 1));
            assertThat(examResponse.getStatusCode()).isEqualTo(CREATED);
            String examId = String.valueOf(examResponse.getBody().get("id"));

            var adminGrade = postAs(ADMIN_EMAIL, ADMIN_PWD,
                    "/students/" + studentId() + "/grades",
                    new GradeRequest(UUID.fromString(examId), new BigDecimal("16"), "note admin"));
            assertThat(adminGrade.getStatusCode()).as("Admin grades student").isEqualTo(CREATED);
            assertThat(adminGrade.getBody().get("value")).as("Admin grade value").isEqualTo(16.0);
        }

        @Test
        @DisplayName("Teacher can list exams for a course")
        void teacher_can_list_exams_for_course() {
            setupAccountsAndCourse();

            postAs(TEACHER_EMAIL, TEACHER_PWD, "/exams",
                    new ExamRequest(courseId(), "Examen 1", Instant.now(), BigDecimal.ONE, ACADEMIC_YEAR, 1));
            postAs(TEACHER_EMAIL, TEACHER_PWD, "/exams",
                    new ExamRequest(courseId(), "Examen 2", Instant.now(), new BigDecimal("2"), ACADEMIC_YEAR, 1));

            var exams = getAs(TEACHER_EMAIL, TEACHER_PWD,
                    "/exams?courseId=" + courseId(), List.class);
            assertThat(exams.getStatusCode()).as("List exams").isEqualTo(OK);
            assertThat(exams.getBody()).as("Exam count").hasSize(2);
        }

        @Test
        @DisplayName("Grade history is recorded after correction")
        void grade_history_is_recorded_after_correction() {
            setupAccountsAndCourse();

            var examResponse = postAs(TEACHER_EMAIL, TEACHER_PWD, "/exams",
                    new ExamRequest(courseId(), "Examen final", Instant.now(), BigDecimal.ONE, ACADEMIC_YEAR, 1));
            String examId = String.valueOf(examResponse.getBody().get("id"));

            postAs(TEACHER_EMAIL, TEACHER_PWD,
                    "/students/" + studentId() + "/grades",
                    new GradeRequest(UUID.fromString(examId), new BigDecimal("12"), null));

            postAs(TEACHER_EMAIL, TEACHER_PWD,
                    "/students/" + studentId() + "/grades",
                    new GradeRequest(UUID.fromString(examId), new BigDecimal("14"), "correction"));

            var gradeId = gradeService.getGradesForStudent(studentId(), teacherId()).get(0).id();
            var history = gradeService.getHistory(gradeId);
            assertThat(history).as("Grade history entries").hasSize(2);
            assertThat(history.get(0).reason()).as("Latest history reason").isEqualTo("correction");
            assertThat(history.get(1).reason()).as("Initial history reason").isEqualTo("Saisie initiale");
        }
    }

    @Nested
    @DisplayName("Group management")
    class GroupManagementFlow {

        @Test
        @DisplayName("Admin changes student group, history tracked, grades persist")
        void admin_changes_group_with_history_tracking() {
            setupAccountsAndCourse();

            var groupA = groupService.create("EL1-A-flow", Track.EL, ACADEMIC_YEAR);
            groupService.changeGroup(studentId(), groupA.id());
            courseAssignmentService.create(courseId(), teacherId(), groupA.id(), ACADEMIC_YEAR, 1);

            var examResponse = postAs(TEACHER_EMAIL, TEACHER_PWD, "/exams",
                    new ExamRequest(courseId(), "Examen final", Instant.now(), BigDecimal.ONE, ACADEMIC_YEAR, 1));
            String examId = String.valueOf(examResponse.getBody().get("id"));

            postAs(TEACHER_EMAIL, TEACHER_PWD,
                    "/students/" + studentId() + "/grades",
                    new GradeRequest(UUID.fromString(examId), new BigDecimal("12"), null));

            var groupB = groupService.create("EL1-B-flow", Track.EL, ACADEMIC_YEAR);
            var changeGroup = postAs(ADMIN_EMAIL, ADMIN_PWD,
                    "/students/" + studentId() + "/group",
                    Map.of("newGroupId", groupB.id().toString()));
            assertThat(changeGroup.getStatusCode()).as("Change group").isEqualTo(CREATED);

            var groupHistory = getAs(STUDENT_EMAIL, STUDENT_PWD,
                    "/students/" + studentId() + "/group-history", List.class);
            assertThat(groupHistory.getBody()).as("Group history has 2 entries").hasSize(2);

            var gradesAfterGroupChange = getAs(STUDENT_EMAIL, STUDENT_PWD,
                    "/students/" + studentId() + "/grades", List.class);
            assertThat(gradesAfterGroupChange.getBody()).as("Grades persist after group change").hasSize(1);
        }
    }

    @Nested
    @DisplayName("Access control")
    class AccessControlFlow {

        @Test
        @DisplayName("Student cannot view another student's grades")
        void student_cannot_view_other_student_grades() {
            setupAccountsAndCourse();

            var other = createStudentSafely(OTHER_STUDENT_EMAIL, OTHER_STUDENT_PWD, "Linus", "Torvalds", Track.EL, ACADEMIC_YEAR);

            var forbidden = getAs(OTHER_STUDENT_EMAIL, OTHER_STUDENT_PWD,
                    "/students/" + studentId() + "/grades", List.class);
            assertThat(forbidden.getStatusCode()).as("View other student grades").isEqualTo(FORBIDDEN);
        }

        @Test
        @DisplayName("Student cannot view another student's group history")
        void student_cannot_view_other_student_group_history() {
            setupAccountsAndCourse();

            createStudentSafely(OTHER_STUDENT_EMAIL, OTHER_STUDENT_PWD, "Linus", "Torvalds", Track.EL, ACADEMIC_YEAR);

            var forbiddenHistory = getAs(OTHER_STUDENT_EMAIL, OTHER_STUDENT_PWD,
                    "/students/" + studentId() + "/group-history", List.class);
            assertThat(forbiddenHistory.getStatusCode()).as("View other student group history").isEqualTo(FORBIDDEN);
        }

        @Test
        @DisplayName("Student can request their own transcript")
        void student_can_request_transcript() {
            setupAccountsAndCourse();

            var resp = postAs(STUDENT_EMAIL, STUDENT_PWD,
                    "/students/" + studentId() + "/transcript", null);
            assertThat(resp.getStatusCode()).as("Request transcript").isEqualTo(ACCEPTED);
        }

        @Test
        @DisplayName("Student cannot request another student's transcript")
        void student_cannot_request_other_transcript() {
            setupAccountsAndCourse();

            createStudentSafely(OTHER_STUDENT_EMAIL, OTHER_STUDENT_PWD, "Linus", "Torvalds", Track.EL, ACADEMIC_YEAR);

            var resp = postAs(OTHER_STUDENT_EMAIL, OTHER_STUDENT_PWD,
                    "/students/" + studentId() + "/transcript", null);
            assertThat(resp.getStatusCode()).as("Request other transcript").isEqualTo(FORBIDDEN);
        }
    }

    // -- Request records --

    record CreateStudentRequest(String email, String password, String firstName, String lastName, Track track, int enrollmentYear) {}
    record CreateTeacherRequest(String email, String password, String firstName, String lastName) {}
    record CreateAdminRequest(String email, String password) {}
    record ExamRequest(UUID courseId, String title, Instant dateExam, BigDecimal coefficient, int academicYear, int semester) {}
    record GradeRequest(UUID examId, BigDecimal value, String reason) {}

    // -- HTTP helpers --

    private ResponseEntity<Map<String, Object>> postAs(String email, String password, String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(email, password);
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), mapType());
    }

    private <T> ResponseEntity<T> getAs(String email, String password, String path, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(email, password);
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    @SuppressWarnings("unchecked")
    private static Class<Map<String, Object>> mapType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }
}
