package ch.uzh.ifi.access.controller;

import ch.uzh.ifi.access.model.Course;
import ch.uzh.ifi.access.model.Submission;
import ch.uzh.ifi.access.model.dto.StudentDTO;
import ch.uzh.ifi.access.model.dto.SubmissionDTO;
import ch.uzh.ifi.access.model.dto.UserDTO;
import ch.uzh.ifi.access.model.projections.*;
import ch.uzh.ifi.access.service.AuthService;
import ch.uzh.ifi.access.service.CourseService;
import ch.uzh.ifi.access.service.EvaluationService;
import lombok.AllArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/courses")
public class CourseController {

    private AuthService authService;

    private CourseService courseService;

    private EvaluationService evaluationService;

    @PostMapping
    @PreAuthorize("hasRole('supervisor')")
    public String createCourse(@RequestBody Map<String, String> body, Authentication authentication) {
        Course newCourse = courseService.createCourseFromRepository(body.get("repository"));
        authService.createCourseRoles(newCourse.getUrl());
        authService.registerCourseSupervisors(newCourse.getUrl(), List.of(authentication.getName()));
        evaluationService.createEvaluators(newCourse);
        return newCourse.getUrl();
    }

    @PostMapping("/{course}/pull")
    @PreAuthorize("hasRole(#course+'-supervisor')")
    public void updateCourse(@PathVariable String course) {
        Course updatedCourse = courseService.updateCourseFromRepository(course);
        evaluationService.createEvaluators(updatedCourse);
    }

    @GetMapping
    public List<CourseOverview> getCourses() {
        return courseService.getCourses();
    }

    @GetMapping("/{course}")
    @PreAuthorize("hasRole(#course)")
    public CourseWorkspace getCourseWorkspace(@PathVariable String course) {
        return courseService.getCourse(course);
    }

    @GetMapping("/{course}/assignments")
    public List<AssignmentOverview> getAssignments(@PathVariable String course) {
        return courseService.getAssignments(course);
    }

    @GetMapping("/{course}/assignments/{assignment}")
    public AssignmentWorkspace getAssignment(@PathVariable String course, @PathVariable String assignment) {
        return courseService.getAssignment(course, assignment);
    }

    @GetMapping("/{course}/assignments/{assignment}/tasks")
    public List<TaskOverview> getTasks(@PathVariable String course, @PathVariable String assignment) {
        return courseService.getTasks(course, assignment);
    }

    @GetMapping("/{course}/assignments/{assignment}/tasks/{task}")
    public TaskWorkspace getTask(@PathVariable String course, @PathVariable String assignment, @PathVariable String task) {
        return courseService.getTask(course, assignment, task);
    }

    @GetMapping("/{course}/assignments/{assignment}/tasks/{task}/users/{user}")
    @PreAuthorize("hasRole(#course+'-assistant') or (#user == authentication.name)")
    public TaskWorkspace getTask(@PathVariable String course, @PathVariable String assignment,
                                 @PathVariable String task, @PathVariable String user) {
        return courseService.getTask(course, assignment, task, user);
    }

    @GetMapping("/{course}/assignments/{assignment}/tasks/{task}/users/{user}/submissions/{submission}")
    @PreAuthorize("hasRole(#course + '-assistant') or @courseService.isSubmissionOwner(#submission, #user)")
    public TaskWorkspace getTask(@PathVariable String course, @PathVariable String assignment,
                                 @PathVariable String task, @PathVariable String user, @PathVariable Long submission) {
        return courseService.getTask(course, assignment, task, user, submission);
    }

    @PostMapping("/{course}/submit")
    @PreAuthorize("hasRole(#course + '-assistant') or not #submission.type.graded or @courseService.isSubmissionAllowed(#submission.taskId)")
    public Submission evaluateSubmission(@PathVariable String course, @RequestBody SubmissionDTO submission) {
        Submission newSubmission = courseService.createSubmission(submission);
        return evaluationService.evaluateSubmission(newSubmission);
    }

    @PostMapping("/{course}/enroll")
    @PreAuthorize("hasRole(#course + '-supervisor')")
    public void addStudents(@PathVariable String course, @RequestBody List<String> newStudents) {
        authService.registerCourseStudents(course, newStudents);
    }

    @GetMapping("/{course}/students")
    @PreAuthorize("hasRole(#course + '-assistant')")
    public List<StudentDTO> getStudents(@PathVariable String course) {
        return authService.getStudentsByCourse(course).stream()
                .map(student -> courseService.getStudent(course, student)).toList();
    }

    @PostMapping("/{course}/students")
    @PreAuthorize("hasRole(#course + '-assistant')")
    public void updateStudent(@PathVariable String course, @RequestBody UserDTO updates) {
        courseService.updateStudent(updates);
    }

    @GetMapping("/{course}/assistants")
    @PreAuthorize("hasRole(#course + '-supervisor')")
    public List<UserRepresentation> getAssistants(@PathVariable String course) {
        return authService.getAssistantsByCourse(course);
    }

    @PostMapping("/{course}/assistants")
    @PreAuthorize("hasRole(#course + '-supervisor')")
    public void addAssistants(@PathVariable String course, @RequestBody List<String> newAssistants) {
        authService.registerCourseAssistants(course, newAssistants);
    }
}