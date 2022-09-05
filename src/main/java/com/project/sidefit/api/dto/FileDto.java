package com.project.sidefit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileDto {

    private String filename;
    private MultipartFile multipartFile;
}
