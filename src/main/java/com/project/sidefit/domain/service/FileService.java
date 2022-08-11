package com.project.sidefit.domain.service;

import com.project.sidefit.domain.entity.FileEntity;
import com.project.sidefit.domain.repository.FileRepository;
import com.project.sidefit.domain.service.dto.FileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    public void save(FileDto fileDto) {
        FileEntity fileEntity = new FileEntity(fileDto.getTitle(), fileDto.getUrl());
        fileRepository.save(fileEntity);
    }

    public List<FileEntity> getFiles() {
        return fileRepository.findAll();
    }
}
