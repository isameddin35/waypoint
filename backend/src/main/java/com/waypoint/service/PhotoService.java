package com.waypoint.service;

import com.waypoint.dto.PhotoResponse;
import com.waypoint.entity.Photo;
import com.waypoint.entity.Route;
import com.waypoint.entity.User;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.PhotoRepository;
import com.waypoint.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final RouteRepository routeRepository;
    private final EntityMapper entityMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Transactional
    public PhotoResponse uploadPhoto(Long routeId, MultipartFile file, Long userId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Photo photo = Photo.builder()
                    .route(route)
                    .filePath(fileName)
                    .uploadedBy(User.builder().id(userId).build())
                    .build();

            photo = photoRepository.save(photo);
            return entityMapper.toPhotoResponse(photo);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    public List<PhotoResponse> getPhotosByRoute(Long routeId) {
        return photoRepository.findByRouteId(routeId).stream()
                .map(entityMapper::toPhotoResponse)
                .toList();
    }
}
