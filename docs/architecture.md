# System Design and Architecture

## Layered architecture

```text
+------------------------------------------------------+
| Browser UI: HTML / CSS / JavaScript                  |
+--------------------------+---------------------------+
                           |
                           v
+------------------------------------------------------+
| REST Controllers                                      |
| StudentController / HallController / ExamController |
| SeatingController / DashboardController              |
+--------------------------+---------------------------+
                           |
                           v
+------------------------------------------------------+
| Services                                              |
| StudentService / HallService / ExamService           |
| SeatingService                                       |
+--------------------------+---------------------------+
                           |
                           v
+------------------------------------------------------+
| Spring Data JPA Repositories                          |
+--------------------------+---------------------------+
                           |
                           v
+------------------------------------------------------+
| H2 (default) / MySQL                                  |
+------------------------------------------------------+
```

## Entities

- Student: register number, name, class/section.
- Hall: name, rows, columns.
- ExamSession: subject/exam, date and time.
- SeatAssignment: maps one student to one seat in one hall for one exam.

## Constraints

- Register number is unique.
- Hall name is unique.
- Exam name is unique.
- A student can have at most one seat per exam.
- A seat can have at most one student per exam/hall.
- Generation validates total capacity before solving.
- Generated arrangements are deleted/replaced only after a valid arrangement is found.

## Algorithm complexity

Let `S` be the number of selected seats and `C` the number of distinct sections. The solver uses bounded backtracking. The theoretical worst case is exponential, but the implementation:
- searches over section counts rather than individual students,
- uses a fail-first seat heuristic,
- rejects illegal adjacent sections immediately,
- limits search nodes,
- retries with randomized tie ordering.

For normal academic seating sizes this makes the search practical while still giving an explicit failure instead of violating the requirement.
