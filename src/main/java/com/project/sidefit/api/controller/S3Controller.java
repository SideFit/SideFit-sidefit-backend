package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.FileDto;
import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.domain.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
//@Controller
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    /*
    @GetMapping("/image")
    public String uploadImage(@ModelAttribute FileDto fileDto) {
        return "image-upload";
    }

    @PostMapping("/image")
    @ResponseBody
    public Response updateUserImage(@ModelAttribute FileDto fileDto) {
        try {
            s3Service.uploadFiles(fileDto.getMultipartFile(), "image");
        } catch (IOException e) {
            Response.failure(-1000, "이미지 업로드 실패");
        }

        return Response.success();
    }*/

    @PostMapping("/image")
    public Response updateUserImage(@RequestBody FileDto fileDto) {
        try {
            s3Service.uploadFiles(fileDto.getMultipartFile(), "static");
        } catch (IOException e) {
            Response.failure(-1000, "이미지 업로드 실패");
        }

        return Response.success();
    }


}
