# API examples

## Add a student

```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"registerNumber":"22CS001","name":"Arun Kumar","classSection":"CSE-A"}'
```

## Add a hall

```bash
curl -X POST http://localhost:8080/api/halls \
  -H "Content-Type: application/json" \
  -d '{"name":"Hall A","rows":5,"columns":6}'
```

## Add an exam

```bash
curl -X POST http://localhost:8080/api/exams \
  -H "Content-Type: application/json" \
  -d '{"name":"Java Programming","examDate":"2026-09-29","startTime":"09:00","endTime":"12:00"}'
```

## Generate

```bash
curl -X POST http://localhost:8080/api/seating/generate \
  -H "Content-Type: application/json" \
  -d '{"examId":1,"hallIds":[1]}'
```

## View

```bash
curl http://localhost:8080/api/seating/exam/1
```
