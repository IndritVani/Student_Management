# Student Management System — Build Specification (v2)

> Working spec for the course project. Code is written against this document.
> **[ASSUMPTION]** = my chosen default — confirm or change it.
> **Jira** is out of scope here (handled separately).
>
> **v2 changes:** added a web frontend (public registration + admin CRUD), `Course` enum (7 predefined),
> auto-generated student numbers, simple admin login; switched to **Java 21** and **H2-only**.

---

## 1. Goal

A small but production-shaped application to manage students, taken through a full
CI/CD pipeline (Jenkins) to a live deployment (OpenShift), with a usable web UI.

Two user-facing areas:

- **Public registration** — anyone opens the site, fills a form, picks one of **7 predefined courses**, and registers.
- **Admin area** — sees the full list of students with all details, and can **add / edit / delete** them.

Plus a REST API (satisfies the "Controller/API" deliverable, powers Swagger, and is what the unit tests exercise).

---

## 2. Tech stack & versions

| Concern        | Choice                                  | Notes |
|----------------|-----------------------------------------|-------|
| Language       | Java **21**                             | LTS; runs on UBI9 OpenJDK 21 images. |
| Build          | Maven (multi-module)                    | Required. |
| Framework      | Spring Boot **3.3.x**                    | Web (MVC), Data JPA, Validation, Thymeleaf, Security. |
| Frontend       | **Thymeleaf + Bootstrap 5** **[ASSUMPTION]** | Server-rendered, lives in `web` module. One deployable jar. |
| Boilerplate    | Lombok                                  | Required. |
| Mapping        | MapStruct 1.6.x                         | Required. DTO ↔ Entity. |
| Excel          | Apache POI 5.x (`poi-ooxml`)            | Required. `.xlsx`. |
| Persistence    | Spring Data JPA                         | |
| Database       | **H2 (in-memory)** everywhere           | Zero setup. Note: data is ephemeral on restart. |
| Auth           | Spring Security, single admin login **[ASSUMPTION]** | Protects `/admin/**`. Can be removed if not wanted. |
| API docs       | springdoc-openapi (Swagger UI)          | Reachable URL on OpenShift. |
| Tests          | JUnit 5 + Mockito                       | Required (JUnit). |
| CI/CD          | Jenkins (declarative pipeline)          | Required. |
| Deploy         | OpenShift (Dockerfile build) **[ASSUMPTION]** | Required. Single app, no external DB. |
| Base package   | `com.example.studentmanagement` **[ASSUMPTION]** | Same root across all modules. |

---

## 3. Architecture

```
Browser
 ├─ public:  /register            ─┐
 └─ admin:   /admin/students ...   ─┤
                                    ▼
              web (MVC + REST controllers)
                       │
                       ▼
              service (business logic) ─▶ repository (JPA) ─▶ H2
                       │
                       ├─▶ mapper (MapStruct): Entity ↔ DTO
                       └─▶ excel (Apache POI): List<Student> ─▶ .xlsx
```

- **MVC controllers** render Thymeleaf pages and call the service directly.
- **REST controllers** (`/api/**`) expose the same operations as JSON (Swagger + tests).
- `/admin/**` is protected by a login; `/register` and `/api/courses` are public.

### Module dependency direction

```
model      (no internal deps)   ← entities + Course enum
dto        (no internal deps)
mapper     ─▶ model, dto
repository ─▶ model
excel      ─▶ model
service    ─▶ repository, mapper, dto, model
web        ─▶ service, dto, excel    ← bootable Spring Boot app, owns UI + security
```

Only **`web`** uses `spring-boot-maven-plugin` to build the executable jar. Others are plain library jars.

---

## 4. Maven multi-module layout

```
student-management/
├── pom.xml                 (parent / aggregator, packaging=pom)
├── model/
│   └── .../model/Student.java
│   └── .../model/Course.java          (enum, 7 values)
├── dto/
│   └── .../dto/StudentDto.java
│   └── .../dto/StudentRegistrationRequest.java   (public form)
│   └── .../dto/StudentAdminRequest.java          (admin add/edit)
│   └── .../dto/CourseDto.java
├── mapper/
│   └── .../mapper/StudentMapper.java
├── repository/
│   └── .../repository/StudentRepository.java
├── excel/
│   └── .../excel/StudentExcelExporter.java
├── service/
│   └── .../service/StudentService.java + StudentServiceImpl.java
│   └── .../service/StudentNumberGenerator.java
│   └── .../service/exception/StudentNotFoundException.java
├── web/
│   ├── .../StudentManagementApplication.java
│   ├── .../web/api/StudentApiController.java      (REST /api/students)
│   ├── .../web/api/CourseApiController.java        (REST /api/courses)
│   ├── .../web/mvc/RegistrationController.java     (public pages)
│   ├── .../web/mvc/AdminStudentController.java     (admin pages)
│   ├── .../web/GlobalExceptionHandler.java
│   ├── .../web/config/SecurityConfig.java
│   └── src/main/resources/
│       ├── application.yml
│       ├── templates/            (Thymeleaf: see §10)
│       └── static/               (css/js if any)
├── Dockerfile
├── Jenkinsfile
└── openshift/  (deployment.yaml, service.yaml, route.yaml)  [if using Option B]
```

> Keep every module under `com.example.studentmanagement.*` so the `web` app can component-scan
> beans, entities, and repositories across modules.

---

## 5. Domain model (`model` module)

### `Course` enum — the 7 predefined courses

```java
public enum Course {
    COMPUTER_SCIENCE("Computer Science"),
    SOFTWARE_ENGINEERING("Software Engineering"),
    INFORMATION_TECHNOLOGY("Information Technology"),
    DATA_SCIENCE("Data Science"),
    CYBERSECURITY("Cybersecurity"),
    BUSINESS_INFORMATICS("Business Informatics"),
    ELECTRICAL_ENGINEERING("Electrical Engineering");

    private final String displayName;
    Course(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
```

> These 7 are placeholders — swap in your real course names.

### `Student` entity

| Field           | Type        | Constraints / notes                          |
|-----------------|-------------|----------------------------------------------|
| `id`            | `Long`      | PK, auto-generated                           |
| `studentNumber` | `String`    | unique, **auto-generated** (e.g. `STU-2025-0007`) |
| `firstName`     | `String`    | not blank                                    |
| `lastName`      | `String`    | not blank                                    |
| `email`         | `String`    | not blank, unique, email format              |
| `dateOfBirth`   | `LocalDate` | optional                                     |
| `course`        | `Course`    | required; `@Enumerated(EnumType.STRING)`     |
| `enrollmentYear`| `Integer`   | defaults to current year                     |
| `gpa`           | `Double`    | optional (admin-set), 0.0–4.0                |

- JPA: `@Entity`, `@Id @GeneratedValue`, `@Column(unique=true)` on `email` and `studentNumber`.
- Lombok `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` (avoid `@Data` on entities).

---

## 6. DTOs (`dto` module)

- `StudentDto` — full read model returned to UI/API (includes `id`, `studentNumber`, course display name).
- `StudentRegistrationRequest` — **public form**: `firstName`, `lastName`, `email`, `dateOfBirth`, `course`. (No id, no studentNumber, no gpa.) Validation: `@NotBlank`, `@Email`, `@NotNull` course.
- `StudentAdminRequest` — **admin add/edit**: everything editable (name, email, course, dob, enrollmentYear, gpa).
- `CourseDto` — `{ name: "COMPUTER_SCIENCE", displayName: "Computer Science" }` for the dropdown.

Lombok on DTOs is fine.

---

## 7. Mapper (`mapper` module) — MapStruct

```java
@Mapper(componentModel = "spring")
public interface StudentMapper {
    StudentDto toDto(Student entity);
    List<StudentDto> toDtoList(List<Student> entities);
    Student toEntity(StudentRegistrationRequest request);   // studentNumber set by service
    Student toEntity(StudentAdminRequest request);
    void updateEntityFromAdminRequest(StudentAdminRequest request, @MappingTarget Student entity);
}
```

### ⚠️ Lombok + MapStruct annotation-processor ordering (classic gotcha)

In modules compiling both, configure the compiler plugin so Lombok runs **before** MapStruct and include the binding:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <path><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><version>${lombok.version}</version></path>
      <path><groupId>org.mapstruct</groupId><artifactId>mapstruct-processor</artifactId><version>${mapstruct.version}</version></path>
      <path><groupId>org.projectlombok</groupId><artifactId>lombok-mapstruct-binding</artifactId><version>0.2.0</version></path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

Without `lombok-mapstruct-binding`, MapStruct often can't see Lombok getters/setters and generates empty mappers.

---

## 8. Repository (`repository` module)

```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);
    boolean existsByStudentNumber(String studentNumber);
    long countByEnrollmentYear(int year);   // helps the number generator
}
```

---

## 9. Service (`service` module)

- `register(StudentRegistrationRequest)` — validate unique email, generate student number, default enrollmentYear = current year, save, return DTO. **Public.**
- `create(StudentAdminRequest)` — admin add.
- `findAll()` / `findById(id)` (throws `StudentNotFoundException`).
- `update(id, StudentAdminRequest)`.
- `delete(id)`.
- `findAllEntities()` — for Excel export.
- `StudentNumberGenerator` — builds `STU-{year}-{4-digit sequence}` from `countByEnrollmentYear` + 1.

Business rule: reject duplicate email at registration with a clear validation message surfaced on the form.

---

## 10. Frontend — Thymeleaf pages (`web` module)

`src/main/resources/templates/`:

| Template                  | Route (controller)            | Purpose |
|---------------------------|-------------------------------|---------|
| `register.html`           | `GET /register`               | Public form; course `<select>` populated from `/api/courses` (or model attr). |
| `register-success.html`   | (after `POST /register`)      | Confirmation + the generated student number. |
| `login.html`              | `GET /login`                  | Admin login (Spring Security form). |
| `admin/list.html`         | `GET /admin/students`         | Table of all students + Edit/Delete + "Add" + "Export Excel" buttons. |
| `admin/form.html`         | `GET /admin/students/new`, `GET /admin/students/{id}/edit` | Shared add/edit form. |
| `fragments/layout.html`   | —                             | Bootstrap nav + common head (Thymeleaf fragments). |

Controllers:

- `RegistrationController` — `GET /register`, `POST /register` (binds `StudentRegistrationRequest`, re-renders form with errors on validation failure).
- `AdminStudentController` — `GET /admin/students`, `GET/POST /admin/students/new`, `GET/POST /admin/students/{id}/edit`, `POST /admin/students/{id}/delete`.

> HTML forms only support GET/POST, so deletes use `POST /admin/students/{id}/delete` (no need for hidden-method config).

Bootstrap 5 via CDN (or a static copy if the cluster blocks CDNs — see deploy notes).

> When we build these templates I'll follow the frontend-design guidance for layout/styling.

---

## 11. REST API (`web` module)

| Method | Path                    | Body                        | Returns            | Code | Access |
|--------|-------------------------|-----------------------------|--------------------|------|--------|
| GET    | `/api/courses`          | —                           | `List<CourseDto>`  | 200  | public |
| POST   | `/api/students`         | `StudentAdminRequest`       | `StudentDto`       | 201  | admin  |
| GET    | `/api/students`         | —                           | `List<StudentDto>` | 200  | admin  |
| GET    | `/api/students/{id}`    | —                           | `StudentDto`       | 200  | admin  |
| PUT    | `/api/students/{id}`    | `StudentAdminRequest`       | `StudentDto`       | 200  | admin  |
| DELETE | `/api/students/{id}`    | —                           | —                  | 204  | admin  |
| GET    | `/api/students/export`  | —                           | `.xlsx` file       | 200  | admin  |

- `GlobalExceptionHandler` (`@RestControllerAdvice`): not-found → 404, validation → 400.
- Swagger UI at `/swagger-ui.html`.

---

## 12. Security (`web` module) — `SecurityConfig`

Simple, single admin account (username/password from config/env), form login.

```
permitAll:   /, /register, /api/courses, /login, /css/**, /js/**, /swagger-ui/**, /v3/api-docs/**, /h2-console/**
authenticated (ROLE_ADMIN): /admin/**, and the mutating /api/students/** endpoints
form login page: /login
```

Admin credentials via `application.yml` (`app.admin.username`, `app.admin.password`) overridable by env vars on OpenShift. Use a `BCryptPasswordEncoder`.

> If you'd rather have **no auth**, I remove this module's config and make `/admin/**` public — one change.

---

## 13. Configuration (`web/src/main/resources/application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:students;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  h2:
    console:
      enabled: true       # /h2-console for inspection
  thymeleaf:
    cache: false          # convenient during dev

app:
  admin:
    username: ${ADMIN_USERNAME:admin}
    password: ${ADMIN_PASSWORD:admin123}   # override on OpenShift
```

> In-memory H2 means data resets on restart. If persistence across restarts is needed,
> switch to `jdbc:h2:file:/data/students` and mount a PVC at `/data` on OpenShift (I'll add that if you want it).

Optional: a `CommandLineRunner` to seed a few sample students so the admin list isn't empty on first boot.

---

## 14. Testing (JUnit 5)

- **Service tests** (Mockito): registration (unique-email check, number generation), CRUD, `StudentNotFoundException`.
- **Mapper test**: real generated mapper, field mapping incl. course display name.
- **Excel test**: export a list, reopen bytes with POI, assert header + row count.
- **Web slice test** (`@WebMvcTest`, optional): status codes for the API; security rules (401/302 on `/admin/**` without auth).

Tests run during `mvn clean install` (Surefire) — what Jenkins executes.

---

## 15. Parent POM essentials

- `<packaging>pom</packaging>` + `<modules>` listing all 7.
- Spring Boot BOM (parent or imported), version pins for MapStruct, POI, Lombok.
- Properties: `java.version=21`, `mapstruct.version`, `lombok.version`.
- Compiler-plugin processor config (§7) in modules using Lombok+MapStruct (or parent `pluginManagement`).

---

## 16. Dockerfile (multi-stage, Java 21)

```dockerfile
# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY ../../Downloads .
RUN mvn -B clean package -DskipTests

# ---- runtime stage ----
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest
WORKDIR /app
COPY --from=build /app/web/target/web-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> Match `web/target/web-*.jar` to the `web` module's artifactId/finalName.

---

## 17. Jenkins pipeline

### `Jenkinsfile`

```groovy
pipeline {
  agent any
  tools { maven 'Maven3'; jdk 'JDK21' }   // names must match Jenkins Global Tool Config
  stages {
    stage('Checkout') { steps { checkout scm } }
    stage('Build')    { steps { sh 'mvn -B clean install' } }   // compiles + runs tests
    stage('Archive')  { steps { archiveArtifacts artifacts: 'web/target/*.jar', fingerprint: true } }
  }
  post {
    always  { junit '**/target/surefire-reports/*.xml' }
    success { echo 'Build succeeded — artifact archived.' }
    failure { echo 'Build failed.' }
  }
}
```

### Setup steps (you do these once in Jenkins UI)

1. **Manage Jenkins → Tools**: add JDK `JDK21` and Maven `Maven3`.
2. **Plugins**: ensure *Pipeline*, *Git*, *JUnit* are installed.
3. **New Item → Pipeline** named `student-management`.
4. **Pipeline → Pipeline script from SCM → Git** → your GitHub URL → branch `main` → Script Path `Jenkinsfile`.
5. Private repo → add a GitHub credential and select it.
6. **Save → Build Now.** Green run + archived `web/target/*.jar` + JUnit report = your screenshot.

---

## 18. OpenShift deployment (single app, H2)

> Prereq: `oc login ...`, then `oc new-project student-mgmt` (or use existing). **You run these.**

### Option A — OpenShift builds from the Dockerfile (simplest)

```bash
oc new-app . --name=student-management --strategy=docker
oc set env deployment/student-management ADMIN_USERNAME=admin ADMIN_PASSWORD='choose-a-strong-one'
oc expose service/student-management
oc get route student-management -o jsonpath='{.spec.host}{"\n"}'
```

### Option B — apply manifests in `openshift/`

```bash
oc apply -f openshift/
oc get route student-management
```

### Verify

- App home / `https://<route-host>/register` → registration page works.
- `https://<route-host>/admin/students` → prompts login, then shows the list. **Screenshot this.**
- `https://<route-host>/swagger-ui.html` → API docs (also good "accessible URL" proof).

> If the cluster blocks the Bootstrap CDN, bundle Bootstrap into `static/` so the UI styles offline.
> Optional CD: add a Jenkins `Deploy` stage running `oc start-build student-management --from-dir=. --follow` with an `oc` token credential.

---

## 19. Deliverables checklist (from the assignment)

- [ ] Code on GitHub (multi-module Maven)
- [ ] Screenshot: Jira board *(you handle Jira)*
- [ ] Screenshot: Jenkins pipeline, successful build
- [ ] Screenshot: OpenShift deployment + live URL (registration + admin pages)
- [ ] PDF docs: project description, architecture, library usage, pipeline description

---

## 20. Open questions / confirm

1. Frontend: **Thymeleaf + Bootstrap** ok, or do you want a React SPA? / confirmation: current is OK
2. Admin area behind a **simple login**, or **no auth**? / confirmation: simple login is fine
3. The 7 course names — keep my placeholders or give me the real ones?/ confirmation: keep the placeholders
4. Package/group id `com.example.studentmanagement` ok? / confirmation: ok
5. OpenShift: which cluster (Developer Sandbox / CRC / school)? / confirmation: we'll deal with this later
6. Want data to **survive restarts** (file-based H2 + PVC), or is ephemeral fine for the demo? / confirmation: to survive restarts

---

## 21. Suggested build order

1. Parent POM + empty modules wired (`mvn clean install` passes).
2. `model` (Student + Course) → `dto` → `mapper` (verify generated mapper — §7 gotcha).
3. `repository` → `service` (+ number generator + unit tests).
4. `excel` (+ test).
5. `web`: REST controllers → security → Thymeleaf pages (register, admin list/form, login). Run locally.
6. `Dockerfile` — build & run container locally.
7. `Jenkinsfile` — green pipeline.
8. OpenShift — deploy, expose route, verify both pages.
9. PDF documentation.
