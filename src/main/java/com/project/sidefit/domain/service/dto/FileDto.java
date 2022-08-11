package com.project.sidefit.domain.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {

    private String title;
    private String url;
    private MultipartFile file;

    public void setUrl(String url) {
        this.url = url;
    }
}
