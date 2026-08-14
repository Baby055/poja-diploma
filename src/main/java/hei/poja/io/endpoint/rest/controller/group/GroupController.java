package hei.poja.io.endpoint.rest.controller.group;

import hei.poja.io.model.Group;
import hei.poja.io.model.Track;
import hei.poja.io.service.GroupService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
@AllArgsConstructor
public class GroupController {
    private final GroupService groupService;

    public record GroupRequest(String ref, Track track, int academicYear) {}

    @GetMapping
    public List<Group> getGroups() {
        return groupService.findAll();
    }

    @GetMapping("/{id}")
    public Group getGroup(@PathVariable UUID id) {
        return groupService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Group createGroup(@RequestBody GroupRequest request) {
        return groupService.create(request.ref(), request.track(), request.academicYear());
    }

    @PutMapping("/{id}")
    public Group updateGroup(@PathVariable UUID id, @RequestBody GroupRequest request) {
        return groupService.update(id, request.ref(), request.track(), request.academicYear());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable UUID id) {
        groupService.deleteById(id);
    }
}
