package hei.poja.io.endpoint.rest.controller.account;

import hei.poja.io.model.Student;
import hei.poja.io.model.Teacher;
import hei.poja.io.model.Track;
import hei.poja.io.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AccountController {
    private final AccountService accountService;

    public record CreateStudentRequest(
            String email,
            String password,
            String firstName,
            String lastName,
            Track track,
            int enrollmentYear){}

    public record CreateTeacherRequest(
            String email,
            String password,
            String firstName,
            String lastName) {}

    public record CreateAdminRequest(String email, String password) {}

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public Student createStudent(@RequestBody CreateStudentRequest request) {
        return accountService.createStudent(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.track(),
                request.enrollmentYear()
        );
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    public Teacher createTeacher(@RequestBody CreateTeacherRequest request) {
        return accountService.createTeacher(
                request.email(), request.password(), request.firstName(), request.lastName());
    }

    @PostMapping("/admins")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAdmin(@RequestBody CreateAdminRequest request) {
        accountService.createAdmin(request.email(), request.password());
    }
}
