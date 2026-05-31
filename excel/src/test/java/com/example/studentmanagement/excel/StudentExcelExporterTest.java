package com.example.studentmanagement.excel;

import com.example.studentmanagement.model.Course;
import com.example.studentmanagement.model.Student;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentExcelExporterTest {

    private final StudentExcelExporter exporter = new StudentExcelExporter();

    @Test
    void export_writesHeaderAndOneRowPerStudent() throws Exception {
        List<Student> students = List.of(
                Student.builder().studentNumber("STU-2025-0001")
                        .firstName("Ada").lastName("Lovelace").email("ada@example.com")
                        .dateOfBirth(LocalDate.of(2003, 5, 12))
                        .course(Course.COMPUTER_SCIENCE).enrollmentYear(2025).gpa(3.9).build(),
                Student.builder().studentNumber("STU-2025-0002")
                        .firstName("Alan").lastName("Turing").email("alan@example.com")
                        .course(Course.SOFTWARE_ENGINEERING).enrollmentYear(2025).build());

        byte[] bytes = exporter.export(students);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet(StudentExcelExporter.SHEET_NAME);
            assertThat(sheet).isNotNull();

            // header + 2 data rows -> last row index 2
            assertThat(sheet.getLastRowNum()).isEqualTo(2);

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Student Number");
            assertThat(header.getPhysicalNumberOfCells()).isEqualTo(StudentExcelExporter.HEADERS.length);

            Row first = sheet.getRow(1);
            assertThat(first.getCell(0).getStringCellValue()).isEqualTo("STU-2025-0001");
            assertThat(first.getCell(5).getStringCellValue()).isEqualTo("Computer Science");
        }
    }

    @Test
    void export_emptyList_writesHeaderOnly() throws Exception {
        byte[] bytes = exporter.export(List.of());

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet(StudentExcelExporter.SHEET_NAME);
            assertThat(sheet.getLastRowNum()).isEqualTo(0);
        }
    }
}
