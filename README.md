# SeatPlan — Exam Hall Seating Arrangement Generator

A complete Spring Boot + JPA + REST API + HTML/CSS/JavaScript project for automatically generating exam-hall seating arrangements.

## Problem addressed

Manual exam seating is time-consuming and can accidentally place students from the same class/section next to one another. SeatPlan stores students and halls, then generates a seating plan across selected halls while enforcing the adjacency rule wherever a valid arrangement exists.

### Adjacency rule

Two seats are considered adjacent when they share an edge:
- left / right
- front / back

Diagonal seats are not treated as adjacent. This definition is documented in the project so it is consistent in the UI, algorithm and API.

## Technology stack

- Java 17
- Spring Boot 3.4.5
- Spring Web / REST
- Spring Data JPA
- H2 database for immediate local execution
- MySQL support through a second application profile
- HTML5 + CSS3 + Vanilla JavaScript frontend
- Maven
- JUnit 5 / Spring Boot Test

## Main modules

1. Student CRUD — register number, name, class/section.
2. Hall CRUD — hall name, rows and columns/capacity.
3. Exam CRUD — exam name, date and session times.
4. Seating generator — validates capacity and creates a conflict-free arrangement.
5. Seating viewer — grouped by hall with row/column and seat number.
6. Print — browser print view for invigilators.
7. REST API — all CRUD and generation operations are available through JSON endpoints.
8. Error handling — validation and business errors are returned as JSON.

## Run the project

Requirements:
- JDK 17+
- Maven 3.9+

From the project folder:

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

H2 console:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:file:./data/seatplan
```

User: `sa`
Password: blank

Build an executable JAR:

```bash
mvn clean package
java -jar target/seatplan-1.0.0.jar
```

## MySQL

Create a database named `seatplan`, then run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Set credentials in `src/main/resources/application-mysql.properties`.

## First-use workflow

1. Open the dashboard.
2. Add students with different class/section values, for example `CSE-A`, `CSE-B`, `ECE-A`.
3. Add one or more halls and specify rows and columns.
4. Create an exam.
5. Select the exam and the halls to use.
6. Click **Generate Seating Plan**.
7. View the generated seat table and print it.

The generator deletes the old plan for the selected exam before creating a new one, so regeneration is safe. When multiple halls are selected, the available seats are distributed proportionally across those halls.

## REST API

### Students

```text
GET    /api/students
GET    /api/students/{id}
POST   /api/students
PUT    /api/students/{id}
DELETE /api/students/{id}
```

POST/PUT example:

```json
{
  "registerNumber": "22CS001",
  "name": "Arun Kumar",
  "classSection": "CSE-A"
}
```

### Halls

```text
GET    /api/halls
GET    /api/halls/{id}
POST   /api/halls
PUT    /api/halls/{id}
DELETE /api/halls/{id}
```

Example:

```json
{
  "name": "Hall A",
  "rows": 5,
  "columns": 6
}
```

### Exams

```text
GET    /api/exams
GET    /api/exams/{id}
POST   /api/exams
PUT    /api/exams/{id}
DELETE /api/exams/{id}
```

Example:

```json
{
  "name": "Java Programming",
  "examDate": "2026-09-29",
  "startTime": "09:00",
  "endTime": "12:00"
}
```

### Seating

Generate:

```text
POST /api/seating/generate
```

Body:

```json
{
  "examId": 1,
  "hallIds": [1, 2]
}
```

View:

```text
GET /api/seating/exam/{examId}
```

Delete generated plan:

```text
DELETE /api/seating/exam/{examId}
```

Dashboard counts:

```text
GET /api/dashboard
```

## Algorithm

The generator first checks that total selected-hall capacity is at least the number of registered students. It then groups students by class/section.

Instead of searching over individual students, the constraint solver searches over section counts. It:
1. Builds a seat graph for all selected halls.
2. Chooses the next unfilled seat using a fail-first heuristic (prefer seats with more already-assigned neighbors).
3. Selects a remaining class/section that is not used by an adjacent seat.
4. Backtracks when no section is legal.
5. Retries with randomized tie ordering when necessary.
6. Assigns the actual students from each section to the solved section positions.
7. Persists the final plan.

If no valid arrangement is found within the configured search limit, the API returns a clear error rather than silently creating a plan that violates the rule.

## Architecture

The application uses a layered architecture:

```text
Browser UI
   |
REST Controllers
   |
Services / Business Logic
   |
Spring Data JPA Repositories
   |
H2 / MySQL
```

See:
- `docs/architecture.md`
- `docs/uml.puml`
- `docs/api-examples.md`

## Assessment-rubric mapping

### 1. Technical Implementation — 40 marks
- Spring Boot application
- REST API
- CRUD operations
- JPA database integration
- Automatic seating algorithm
- Frontend integration

### 2. System Design & Architecture — 25 marks
- Layered architecture
- Entity relationship model
- UML class diagram
- REST separation
- H2/MySQL persistence

### 3. Code Quality & Efficiency — 20 marks
- DTOs for generation requests/responses
- Validation
- Global exception handling
- Modular services
- Bounded backtracking and randomized retries
- Unit tests for the core generator

### 4. Presentation & Communication — 15 marks
Use the included UI for demonstration and the included documentation/UML for the viva.

## Demo data

A demo JSON file is included in `docs/demo-data.json`. You can add those records through the UI or API.

## Notes

This project intentionally does not use authentication because the supplied project brief focuses on seating generation, CRUD, REST APIs, database design and architecture. Authentication/role-based access can be added later without changing the seating algorithm.

## Database integrity

Deleting a student, hall or exam also removes its related seat assignments first, preventing foreign-key errors and orphaned records.
