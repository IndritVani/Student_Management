package com.example.studentmanagement.web.mvc;

import com.example.studentmanagement.dto.StudentDto;
import com.example.studentmanagement.dto.StudentRegistrationRequest;
import com.example.studentmanagement.service.CourseService;
import com.example.studentmanagement.service.StudentService;
import com.example.studentmanagement.service.exception.DuplicateEmailException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/** Public registration pages. */
@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final StudentService studentService;
    private final CourseService courseService;

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("registrationRequest", new StudentRegistrationRequest());
        model.addAttribute("courses", courseService.findAll());
        return "register";
    }

    @PostMapping("/register")
    public String submit(@Valid @ModelAttribute("registrationRequest") StudentRegistrationRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", courseService.findAll());
            return "register";
        }

        try {
            StudentDto created = studentService.register(request);
            model.addAttribute("student", created);
            return "register-success";
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate", "That email is already registered.");
            model.addAttribute("courses", courseService.findAll());
            return "register";
        }
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/register";
    }
}
