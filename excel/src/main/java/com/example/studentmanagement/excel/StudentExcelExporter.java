package com.example.studentmanagement.excel;

import com.example.studentmanagement.model.Course;
import com.example.studentmanagement.model.Student;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Renders a list of {@link Student} into a {@code .xlsx} workbook (Apache POI).
 */
@Component
public class StudentExcelExporter {

    static final String SHEET_NAME = "Students";

    static final String[] HEADERS = {
            "Student Number", "First Name", "Last Name", "Email",
            "Date of Birth", "Course", "Enrollment Year", "GPA"
    };

    /**
     * @param students students to export (must not be {@code null})
     * @return the {@code .xlsx} file content as bytes
     */
    public byte[] export(List<Student> students) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET_NAME);
            writeHeader(workbook, sheet);
            writeRows(sheet, students);

            for (int col = 0; col < HEADERS.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            // ByteArrayOutputStream never actually throws IOException, but the API declares it.
            throw new UncheckedIOException("Failed to generate students .xlsx export", e);
        }
    }

    private void writeHeader(Workbook workbook, Sheet sheet) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row header = sheet.createRow(0);
        for (int col = 0; col < HEADERS.length; col++) {
            Cell cell = header.createCell(col);
            cell.setCellValue(HEADERS[col]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeRows(Sheet sheet, List<Student> students) {
        int rowIdx = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(nullToEmpty(student.getStudentNumber()));
            row.createCell(1).setCellValue(nullToEmpty(student.getFirstName()));
            row.createCell(2).setCellValue(nullToEmpty(student.getLastName()));
            row.createCell(3).setCellValue(nullToEmpty(student.getEmail()));
            row.createCell(4).setCellValue(formatDate(student.getDateOfBirth()));
            row.createCell(5).setCellValue(formatCourse(student.getCourse()));
            row.createCell(6).setCellValue(student.getEnrollmentYear() == null
                    ? "" : String.valueOf(student.getEnrollmentYear()));
            row.createCell(7).setCellValue(student.getGpa() == null
                    ? "" : String.valueOf(student.getGpa()));
        }
    }

    private String formatCourse(Course course) {
        return course == null ? "" : course.getDisplayName();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
