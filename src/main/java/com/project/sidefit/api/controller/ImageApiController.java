package com.project.sidefit.api.controller;

import com.project.sidefit.api.dto.response.Response;
import com.project.sidefit.domain.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

//@Controller
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageApiController {

    private final S3Service s3Service;

    /*@GetMapping("/image")
    public String uploadImage(@ModelAttribute FileDto fileDto) {
        return "image-upload";
    }

    @PostMapping("/image")
    @ResponseBody
    public Response updateUserImage(@ModelAttribute FileDto fileDto) {
        try {
            String imageUrl = s3Service.uploadFiles(fileDto.getMultipartFile(), "image");
            return Response.success(imageUrl);
        } catch (IOException e) {
            return Response.failure(-1000, "이미지 업로드 실패");
        }
    }*/

    @PostMapping("/image")
    public Response uploadImage(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return Response.failure(-1000, "이미지 파일 필요");
        }

        try {
            s3Service.uploadFiles(multipartFile, "image");
        } catch (IOException e) {
            log.error("error",e);
            return Response.failure(-1000, "이미지 업로드 실패");
        }

        return Response.success();
    }

    @DeleteMapping("/image")
    public Response deleteImage(String imageUrl) {
        s3Service.deleteFile(imageUrl, "image");
        return Response.success();
    }

    @GetMapping("/image/download")
    public ResponseEntity<byte[]> download(String imageUrl) throws IOException {
        return s3Service.downloadFile(imageUrl, "image");
    }
}
