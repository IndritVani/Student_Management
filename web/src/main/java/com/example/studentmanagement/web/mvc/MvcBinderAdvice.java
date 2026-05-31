package com.example.studentmanagement.web.mvc;

import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;

/**
 * Form-binding rules shared by the MVC controllers. The key job is turning empty form fields
 * into {@code null} for optional values (date of birth, GPA) instead of failing type conversion.
 * Scoped to the {@code web.mvc} package so it never touches the JSON API.
 */
@ControllerAdvice(basePackages = "com.example.studentmanagement.web.mvc")
public class MvcBinderAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Trim strings and treat blank as null.
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        // Empty numeric fields -> null (rather than a conversion error).
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
        binder.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, true));
        // Empty <input type="date"> -> null; otherwise ISO yyyy-MM-dd.
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue((text == null || text.isBlank()) ? null : LocalDate.parse(text.trim()));
            }

            @Override
            public String getAsText() {
                return getValue() == null ? "" : getValue().toString();
            }
        });
    }
}
