# Student Management System

A small, production-shaped Spring Boot application to manage students, built as a
multi-module Maven project and taken through CI/CD (Jenkins) to OpenShift.

* **Public registration** — anyone can open the site, fill a form, pick one of 7 predefined
  courses, and register. They receive an auto-generated student number.
* **Admin area** — login-protected list of all students with add / edit / delete and Excel export.
* **REST API** — the same operations as JSON, documented with Swagger UI.

## Tech stack

Java 21 · Spring Boot 3.3 (Web MVC, Data JPA, Validation, Thymeleaf, Security) ·
Lombok · MapStruct · Apache POI · H2 · springdoc-openapi · JUnit 5 + Mockito ·
Jenkins · OpenShift.

## Modules

```
model       JPA entities + Course enum (7 values)
dto         request/response DTOs
mapper      MapStruct Entity <-> DTO
repository  Spring Data JPA
excel       Apache POI .xlsx export
service     business logic (registration, CRUD, number generator)
web         bootable app: MVC pages + REST + security + UI   (the only executable jar)
```

Dependency direction: `web → service → {repository, mapper, dto, model}`, plus `web → excel`.

## Build & test

The build needs JDK 21. Maven is provided via the wrapper:

```bash
./mvnw clean install          # Linux/macOS
mvnw.cmd clean install        # Windows
```

This compiles all modules and runs the unit tests (Surefire).

## Run locally

```bash
./mvnw -pl web spring-boot:run
# or, after a build:
java -jar web/target/web-1.0.0.jar
```

Then open:

| URL                                   | What |
|---------------------------------------|------|
| http://localhost:8080/register        | Public registration |
| http://localhost:8080/admin/students  | Admin list (login required) |
| http://localhost:8080/login           | Admin login |
| http://localhost:8080/swagger-ui.html | API docs |
| http://localhost:8080/h2-console      | DB console (JDBC URL `jdbc:h2:mem:students`) |

Default admin credentials: **admin / admin123** (override with `ADMIN_USERNAME` / `ADMIN_PASSWORD`).
A few sample students are seeded on first boot.

## Configuration

| Env var          | Default                                          | Purpose |
|------------------|--------------------------------------------------|---------|
| `DB_URL`         | `jdbc:h2:mem:students;DB_CLOSE_DELAY=-1`         | In-memory locally; file-based on OpenShift for persistence |
| `ADMIN_USERNAME` | `admin`                                          | Admin login |
| `ADMIN_PASSWORD` | `admin123`                                       | Admin login |

## REST API

| Method | Path                   | Access | Returns |
|--------|------------------------|--------|---------|
| GET    | `/api/courses`         | public | course list |
| POST   | `/api/students`        | admin  | created student (201) |
| GET    | `/api/students`        | admin  | all students |
| GET    | `/api/students/{id}`   | admin  | one student |
| PUT    | `/api/students/{id}`   | admin  | updated student |
| DELETE | `/api/students/{id}`   | admin  | 204 |
| GET    | `/api/students/export` | admin  | `.xlsx` file |

## CI/CD & deployment

* `Jenkinsfile` — declarative pipeline: checkout → `mvn clean install` → archive jar + JUnit report.
* `Dockerfile` — multi-stage (Maven build → UBI9 OpenJDK 21 runtime).
* `openshift/` — manifests + instructions (see `openshift/README.md`). Uses file-based H2 on a PVC.
