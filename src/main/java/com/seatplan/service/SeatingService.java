package com.seatplan.service;

import com.seatplan.dto.GenerateSeatingRequest;
import com.seatplan.dto.SeatAssignmentDto;
import com.seatplan.dto.SeatingPlanDto;
import com.seatplan.exception.BusinessException;
import com.seatplan.exception.ResourceNotFoundException;
import com.seatplan.model.ExamSession;
import com.seatplan.model.Hall;
import com.seatplan.model.SeatAssignment;
import com.seatplan.model.Student;
import com.seatplan.repository.ExamRepository;
import com.seatplan.repository.HallRepository;
import com.seatplan.repository.SeatAssignmentRepository;
import com.seatplan.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatingService {

    private final StudentRepository studentRepository;
    private final HallRepository hallRepository;
    private final ExamRepository examRepository;
    private final SeatAssignmentRepository assignmentRepository;
    private final int maxNodes;
    private final int maxAttempts;

    public SeatingService(StudentRepository studentRepository,
                          HallRepository hallRepository,
                          ExamRepository examRepository,
                          SeatAssignmentRepository assignmentRepository,
                          @Value("${seatplan.generator.max-nodes:250000}") int maxNodes,
                          @Value("${seatplan.generator.max-attempts:20}") int maxAttempts) {
        this.studentRepository = studentRepository;
        this.hallRepository = hallRepository;
        this.examRepository = examRepository;
        this.assignmentRepository = assignmentRepository;
        this.maxNodes = maxNodes;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public SeatingPlanDto generate(GenerateSeatingRequest request) {
        ExamSession exam = examRepository.findById(request.examId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + request.examId()));

        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) {
            throw new BusinessException("Add students before generating a seating plan.");
        }

        List<Long> distinctHallIds = request.hallIds().stream().distinct().toList();
        List<Hall> halls = distinctHallIds.stream()
                .map(id -> hallRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Hall not found: " + id)))
                .toList();

        int capacity = halls.stream().mapToInt(Hall::getCapacity).sum();
        if (capacity < students.size()) {
            throw new BusinessException("Insufficient hall capacity. Students: "
                    + students.size() + ", selected capacity: " + capacity + ".");
        }

        Map<String, List<Student>> studentsBySection = students.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getClassSection().trim(),
                        LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)
                ));

        List<SeatPosition> positions = allocatePositionsAcrossHalls(halls, students.size());

        Map<SeatKey, SeatPosition> positionMap = positions.stream()
                .collect(Collectors.toMap(SeatPosition::key, p -> p));

        List<SeatKey> allKeys = positions.stream().map(SeatPosition::key).toList();
        Map<SeatKey, List<SeatKey>> neighbors = buildNeighbors(positions, positionMap);

        Map<SeatKey, String> solved = null;
        Random random = new Random();

        for (int attempt = 1; attempt <= maxAttempts && solved == null; attempt++) {
            Map<String, Integer> remaining = studentsBySection.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size(),
                            (a, b) -> a, LinkedHashMap::new));
            Map<SeatKey, String> placed = new HashMap<>();
            int[] nodes = {0};
            if (solve(remaining, placed, new HashSet<>(allKeys), neighbors, random, nodes)) {
                solved = placed;
            }
        }

        if (solved == null) {
            throw new BusinessException(
                    "No valid seating arrangement could be found for the selected halls and class/section distribution. "
                    + "Try adding another hall, changing the selected halls, or balancing sections."
            );
        }

        // Old plan is removed only after a valid solution has been found.
        assignmentRepository.deleteByExamId(exam.getId());

        Map<String, Deque<Student>> studentQueues = studentsBySection.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayDeque<>(e.getValue())));

        List<SeatAssignment> assignments = new ArrayList<>();
        for (SeatPosition position : positions) {
            String section = solved.get(position.key());
            Student student = studentQueues.get(section).removeFirst();
            int seatNumber = (position.row() - 1) * position.columns() + position.column();
            Hall hall = halls.stream()
                    .filter(h -> h.getId().equals(position.hallId()))
                    .findFirst()
                    .orElseThrow();
            assignments.add(new SeatAssignment(exam, hall, student, seatNumber,
                    position.row(), position.column()));
        }

        assignmentRepository.saveAll(assignments);
        List<SeatAssignmentDto> dto = assignments.stream().map(SeatAssignmentDto::from).toList();
        return SeatingPlanDto.of(exam, dto);
    }

    @Transactional(readOnly = true)
    public SeatingPlanDto getPlan(Long examId) {
        ExamSession exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));
        List<SeatAssignmentDto> assignments = assignmentRepository.findByExamId(examId).stream()
                .sorted(Comparator.comparing((SeatAssignment a) -> a.getHall().getName())
                        .thenComparing(SeatAssignment::getSeatNumber))
                .map(SeatAssignmentDto::from)
                .toList();
        return SeatingPlanDto.of(exam, assignments);
    }

    @Transactional
    public void deletePlan(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found: " + examId);
        }
        assignmentRepository.deleteByExamId(examId);
    }

    /**
     * Uses proportional capacity allocation so selecting multiple halls actually
     * distributes the students across those halls instead of filling only the first hall.
     */
    private List<SeatPosition> allocatePositionsAcrossHalls(List<Hall> halls, int studentCount) {
        Map<Long, Integer> allocation = new LinkedHashMap<>();
        Map<Long, Double> fractional = new HashMap<>();
        int assigned = 0;
        int totalCapacity = halls.stream().mapToInt(Hall::getCapacity).sum();

        for (Hall hall : halls) {
            double exact = (double) studentCount * hall.getCapacity() / totalCapacity;
            int count = Math.min(hall.getCapacity(), (int) Math.floor(exact));
            allocation.put(hall.getId(), count);
            fractional.put(hall.getId(), exact - Math.floor(exact));
            assigned += count;
        }

        // Give the remaining students to halls with the largest fractional share.
        List<Hall> byFraction = new ArrayList<>(halls);
        byFraction.sort((a, b) -> Double.compare(fractional.get(b.getId()), fractional.get(a.getId())));
        int index = 0;
        while (assigned < studentCount) {
            Hall hall = byFraction.get(index % byFraction.size());
            if (allocation.get(hall.getId()) < hall.getCapacity()) {
                allocation.put(hall.getId(), allocation.get(hall.getId()) + 1);
                assigned++;
            }
            index++;
        }

        List<SeatPosition> positions = new ArrayList<>();
        for (Hall hall : halls) {
            int needed = allocation.get(hall.getId());
            int added = 0;
            for (int row = 1; row <= hall.getRows() && added < needed; row++) {
                for (int col = 1; col <= hall.getColumns() && added < needed; col++) {
                    positions.add(new SeatPosition(hall.getId(), hall.getName(), hall.getRows(),
                            hall.getColumns(), row, col));
                    added++;
                }
            }
        }
        return positions;
    }

    private boolean solve(Map<String, Integer> remaining,
                          Map<SeatKey, String> placed,
                          Set<SeatKey> unassigned,
                          Map<SeatKey, List<SeatKey>> neighbors,
                          Random random,
                          int[] nodes) {

        if (unassigned.isEmpty()) {
            return true;
        }

        if (++nodes[0] > maxNodes) {
            return false;
        }

        SeatKey next = chooseNextSeat(unassigned, placed, neighbors, random);

        List<String> candidates = new ArrayList<>();

        // All sections with remaining students are candidates.
        // Same-section adjacency is no longer a hard failure.
        for (Map.Entry<String, Integer> entry : remaining.entrySet()) {
            if (entry.getValue() > 0) {
                candidates.add(entry.getKey());
            }
        }

        /*
         * Prefer sections that create the FEWEST same-section
         * neighbouring seats.
         *
         * This makes different sections stay close to each other
         * whenever possible, while still allowing an arrangement
         * when the section distribution is unbalanced.
         */
        candidates.sort((a, b) -> {
            int sameA = countSameSectionNeighbors(a, next, placed, neighbors);
            int sameB = countSameSectionNeighbors(b, next, placed, neighbors);

            if (sameA != sameB) {
                return Integer.compare(sameA, sameB);
            }

            // If the conflict score is equal, use the section
            // having more remaining students first.
            return Integer.compare(
                    remaining.get(b),
                    remaining.get(a)
            );
        });

        // Randomize only candidates with the same score so that
        // repeated generations don't always produce exactly
        // the same arrangement.
        List<String> orderedCandidates = new ArrayList<>();

        int i = 0;
        while (i < candidates.size()) {
            int score = countSameSectionNeighbors(
                    candidates.get(i),
                    next,
                    placed,
                    neighbors
            );

            int j = i + 1;

            while (j < candidates.size()
                    && countSameSectionNeighbors(
                    candidates.get(j),
                    next,
                    placed,
                    neighbors
            ) == score) {
                j++;
            }

            List<String> group = new ArrayList<>(candidates.subList(i, j));
            Collections.shuffle(group, random);
            orderedCandidates.addAll(group);

            i = j;
        }

        for (String section : orderedCandidates) {

            placed.put(next, section);
            remaining.put(section, remaining.get(section) - 1);
            unassigned.remove(next);

            if (solve(
                    remaining,
                    placed,
                    unassigned,
                    neighbors,
                    random,
                    nodes
            )) {
                return true;
            }

            // Backtrack
            unassigned.add(next);
            remaining.put(section, remaining.get(section) + 1);
            placed.remove(next);
        }

        return false;
    }
    private int countSameSectionNeighbors(
            String section,
            SeatKey seat,
            Map<SeatKey, String> placed,
            Map<SeatKey, List<SeatKey>> neighbors) {

        int count = 0;

        for (SeatKey neighbor : neighbors.getOrDefault(seat, List.of())) {
            if (section.equals(placed.get(neighbor))) {
                count++;
            }
        }

        return count;
    }

    private SeatKey chooseNextSeat(Set<SeatKey> unassigned,
                                   Map<SeatKey, String> placed,
                                   Map<SeatKey, List<SeatKey>> neighbors,
                                   Random random) {
        int bestAssigned = -1;
        int bestDegree = -1;
        List<SeatKey> ties = new ArrayList<>();

        for (SeatKey key : unassigned) {
            int assigned = 0;
            for (SeatKey n : neighbors.getOrDefault(key, List.of())) {
                if (placed.containsKey(n)) assigned++;
            }
            int degree = neighbors.getOrDefault(key, List.of()).size();

            if (assigned > bestAssigned || (assigned == bestAssigned && degree > bestDegree)) {
                bestAssigned = assigned;
                bestDegree = degree;
                ties.clear();
                ties.add(key);
            } else if (assigned == bestAssigned && degree == bestDegree) {
                ties.add(key);
            }
        }
        return ties.get(random.nextInt(ties.size()));
    }

    private Map<SeatKey, List<SeatKey>> buildNeighbors(List<SeatPosition> positions,
                                                        Map<SeatKey, SeatPosition> positionMap) {
        Map<SeatKey, List<SeatKey>> result = new HashMap<>();
        for (SeatPosition p : positions) {
            List<SeatKey> ns = new ArrayList<>();
            int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] d : directions) {
                SeatKey key = new SeatKey(p.hallId(), p.row() + d[0], p.column() + d[1]);
                if (positionMap.containsKey(key)) ns.add(key);
            }
            result.put(p.key(), ns);
        }
        return result;
    }

    private record SeatKey(Long hallId, int row, int column) {}

    private record SeatPosition(Long hallId, String hallName, int rows, int columns, int row, int column) {
        SeatKey key() {
            return new SeatKey(hallId, row, column);
        }
    }
}
