package com.waypoint.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String username;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
