package com.waypoint.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PhotoResponse {
    private Long id;
    private Long routeId;
    private String filePath;
    private String fileUrl;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;
}
