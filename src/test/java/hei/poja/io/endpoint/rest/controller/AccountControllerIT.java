package hei.poja.io.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import hei.poja.io.conf.FacadeIT;
import hei.poja.io.endpoint.rest.controller.AccountAndGradeFlowIT.CreateAdminRequest;
import hei.poja.io.endpoint.rest.controller.AccountAndGradeFlowIT.CreateStudentRequest;
import hei.poja.io.endpoint.rest.controller.AccountAndGradeFlowIT.CreateTeacherRequest;
import hei.poja.io.exception.ConflictException;
import hei.poja.io.model.Track;
import hei.poja.io.service.AccountService;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

class AccountControllerIT extends FacadeIT {

    private static final String ADMIN_EMAIL = "admin-acct@hei.school";
    private static final String ADMIN_PWD = "adminPwd";
    private static final int ACADEMIC_YEAR = 2024;

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private AccountService accountService;

    @Nested
    @DisplayName("Account creation via REST")
    class AccountCreation {

        @Test
        @DisplayName("Admin can create student accounts")
        void admin_can_create_student() {
            ensureAdmin();

            var resp = postAs(ADMIN_EMAIL, ADMIN_PWD, "/students",
                    new CreateStudentRequest("new-student@acct.hei", "pwd", "A", "B", Track.EL, ACADEMIC_YEAR));
            assertThat(resp.getStatusCode()).as("Create student").isEqualTo(CREATED);
        }

        @Test
        @DisplayName("Admin can create teacher accounts")
        void admin_can_create_teacher() {
            ensureAdmin();

            var resp = postAs(ADMIN_EMAIL, ADMIN_PWD, "/teachers",
                    new CreateTeacherRequest("new-teacher@acct.hei", "pwd", "Ada", "Lovelace"));
            assertThat(resp.getStatusCode()).as("Create teacher").isEqualTo(CREATED);
        }

        @Test
        @DisplayName("Admin can create admin accounts")
        void admin_can_create_admin() {
            ensureAdmin();

            var resp = postAs(ADMIN_EMAIL, ADMIN_PWD, "/admins",
                    new CreateAdminRequest("new-admin@acct.hei", "pwd"));
            assertThat(resp.getStatusCode()).as("Create admin").isEqualTo(CREATED);
        }
    }

    @Nested
    @DisplayName("Duplicate and invalid requests")
    class Validation {

        @Test
        @DisplayName("Duplicate email returns CONFLICT")
        void duplicate_account_returns_conflict() {
            ensureAdmin();

            postAs(ADMIN_EMAIL, ADMIN_PWD, "/students",
                    new CreateStudentRequest("dup-acct@hei.school", "pwd", "A", "B", Track.EL, ACADEMIC_YEAR));

            var duplicate = postAs(ADMIN_EMAIL, ADMIN_PWD, "/students",
                    new CreateStudentRequest("dup-acct@hei.school", "pwd", "A", "B", Track.EL, ACADEMIC_YEAR));
            assertThat(duplicate.getStatusCode()).as("Duplicate creation").isEqualTo(CONFLICT);
        }

        @Test
        @DisplayName("Unauthenticated request returns UNAUTHORIZED")
        void unauthenticated_request_returns_401() {
            var resp = restTemplate.exchange("/students", HttpMethod.POST,
                    new HttpEntity<>(Map.of(
                            "email", "anon-acct@hei.school",
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
    @DisplayName("Access control")
    class AccessControl {

        @Test
        @DisplayName("Non-admin cannot create accounts")
        void non_admin_cannot_create_accounts() {
            ensureAdmin();
            var student = createStudentSafely("simple-acct@hei.school", "pwd", "A", "B", Track.EL, ACADEMIC_YEAR);

            var attempt = postAs(student.user().email(), "pwd", "/students",
                    new CreateStudentRequest("x-acct@hei.school", "pwd", "A", "B", Track.EL, ACADEMIC_YEAR));
            assertThat(attempt.getStatusCode()).as("Student tries to create account").isEqualTo(FORBIDDEN);
        }
    }

    private void ensureAdmin() {
        try { accountService.createAdmin(ADMIN_EMAIL, ADMIN_PWD); } catch (ConflictException ignored) {}
    }

    private hei.poja.io.model.Student createStudentSafely(String email, String pwd, String first, String last, Track track, int year) {
        try { return accountService.createStudent(email, pwd, first, last, track, year); } catch (ConflictException e) {
            return accountService.createStudent(email + "-retry", pwd, first, last, track, year);
        }
    }

    // -- HTTP helpers --

    private ResponseEntity<Map<String, Object>> postAs(String email, String password, String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(email, password);
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), mapType());
    }

    @SuppressWarnings("unchecked")
    private static Class<Map<String, Object>> mapType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }
}
