package com.project.sidefit.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageApiController {

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
