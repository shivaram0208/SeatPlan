package com.seatplan.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GenerateSeatingRequest(
        @NotNull Long examId,
        @NotEmpty List<Long> hallIds
) {}
