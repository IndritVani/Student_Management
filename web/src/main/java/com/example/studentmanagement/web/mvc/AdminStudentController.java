package com.example.studentmanagement.web.mvc;

import com.example.studentmanagement.dto.StudentAdminRequest;
import com.example.studentmanagement.dto.StudentDto;
import com.example.studentmanagement.service.CourseService;
import com.example.studentmanagement.service.StudentService;
import com.example.studentmanagement.service.exception.DuplicateEmailException;
import com.example.studentmanagement.service.exception.StudentNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin CRUD pages for students. Protected by Spring Security ({@code /admin/**}). */
@Controller
@RequestMapping("/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final StudentService studentService;
    private final CourseService courseService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("studentRequest", new StudentAdminRequest());
        prepareForm(model, "/admin/students/new", false, null, null);
        return "admin/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("studentRequest") StudentAdminRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "/admin/students/new", false, null, null);
            return "admin/form";
        }
        try {
            StudentDto created = studentService.create(request);
            redirectAttributes.addFlashAttribute("message",
                    "Student created: " + created.getStudentNumber());
            return "redirect:/admin/students";
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate", "That email is already registered.");
            prepareForm(model, "/admin/students/new", false, null, null);
            return "admin/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        StudentDto student = studentService.findById(id);
        model.addAttribute("studentRequest", toAdminRequest(student));
        prepareForm(model, "/admin/students/" + id + "/edit", true, id, student.getStudentNumber());
        return "admin/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("studentRequest") StudentAdminRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "/admin/students/" + id + "/edit", true, id, null);
            return "admin/form";
        }
        try {
            studentService.update(id, request);
            redirectAttributes.addFlashAttribute("message", "Student updated.");
            return "redirect:/admin/students";
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate", "That email is already registered.");
            prepareForm(model, "/admin/students/" + id + "/edit", true, id, null);
            return "admin/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        studentService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Student deleted.");
        return "redirect:/admin/students";
    }

    @ExceptionHandler(StudentNotFoundException.class)
    public String handleNotFound(StudentNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/students";
    }

    private void prepareForm(Model model, String formAction, boolean editing, Long id, String studentNumber) {
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("formAction", formAction);
        model.addAttribute("editing", editing);
        model.addAttribute("studentId", id);
        model.addAttribute("studentNumber", studentNumber);
    }

    private StudentAdminRequest toAdminRequest(StudentDto dto) {
        return StudentAdminRequest.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .dateOfBirth(dto.getDateOfBirth())
                .course(dto.getCourse())
                .enrollmentYear(dto.getEnrollmentYear())
                .gpa(dto.getGpa())
                .build();
    }
}
