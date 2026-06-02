package com.waypoint.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReviewRequest {
    @NotNull @Min(1) @Max(5)
    private Integer rating;

    private String comment;
}
