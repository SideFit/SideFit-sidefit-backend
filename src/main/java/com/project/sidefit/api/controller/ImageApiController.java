package com.project.sidefit.api.controller;

import com.project.sidefit.domain.service.FileService;
import com.project.sidefit.domain.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageApiController {

    private final FileService fileService;
    private final S3Service s3Service;

    @GetMapping("/image")
    public void getImages() {

    }

    @PostMapping("/image")
    public void uploadImage() {

    }

    @DeleteMapping("/image")
    public void deleteImage() {

    }
}
