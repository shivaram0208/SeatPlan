# Test Plan

## Functional tests

1. Add, edit and delete a student.
2. Add, edit and delete a hall.
3. Add, edit and delete an exam.
4. Attempt duplicate register number — API should return HTTP 400.
5. Attempt duplicate hall name — HTTP 400.
6. Attempt exam with end time before start time — HTTP 400.
7. Attempt generation with insufficient capacity — HTTP 400.
8. Generate a valid plan and verify every student appears once.
9. Verify no left/right/front/back neighbors share a class/section.
10. Regenerate and verify the old plan is replaced.
11. Open View Plan and print.

## Edge cases

- One-seat hall.
- Multiple halls.
- More students than one hall but enough combined capacity.
- Unequal class sizes.
- Very unbalanced class sizes.
- Impossible distribution — system returns a clear business error.
