package com.waypoint.controller;

import com.waypoint.dto.PhotoResponse;
import com.waypoint.security.UserPrincipal;
import com.waypoint.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping(value = "/routes/{routeId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponse> uploadPhoto(
            @PathVariable Long routeId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.uploadPhoto(routeId, file, principal.getUserId()));
    }

    @GetMapping("/routes/{routeId}/photos")
    public ResponseEntity<List<PhotoResponse>> getPhotos(@PathVariable Long routeId) {
        return ResponseEntity.ok(photoService.getPhotosByRoute(routeId));
    }
}
