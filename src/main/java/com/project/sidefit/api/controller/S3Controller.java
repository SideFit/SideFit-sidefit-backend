package com.project.sidefit.api.controller;

import com.project.sidefit.domain.entity.FileEntity;
import com.project.sidefit.domain.service.FileService;
import com.project.sidefit.domain.service.S3Service;
import com.project.sidefit.domain.service.dto.FileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;
    private final FileService fileService;

    @GetMapping("/s3/upload")
    public String goToUpload() {
        return "s3/upload";
    }

    @PostMapping("/s3/upload")
    public String uploadFile(FileDto fileDto) throws IOException {
        String url = s3Service.uploadFile(fileDto.getFile());
        fileDto.setUrl(url);
        fileService.save(fileDto);

        return "redirect:/s3/list";
    }

    @GetMapping("/s3/list")
    public String listPage(Model model) {
        List<FileEntity> fileList =fileService.getFiles();
        model.addAttribute("fileList", fileList);

        return "s3/list";
    }
}
