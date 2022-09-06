package com.project.sidefit.domain.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.project.sidefit.domain.entity.Image;
import com.project.sidefit.domain.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.environment}")
    private String environment;

    @Value("${spring.fileDir}")
    private String rootDir;

    private String fileDir;

    private final AmazonS3Client amazonS3Client;

    private final ImageRepository imageRepository;

    @PostConstruct
    private void init() {
        if (environment.equals("local")) {
            this.fileDir = System.getProperty("user.dir") + this.rootDir;
        } else {
            this.fileDir = this.rootDir;
        }
    }

    public void deleteFile(String imageUrl, String dirName) {
        imageRepository.deleteByImageUrl(imageUrl);
//        DeleteObjectRequest request = new DeleteObjectRequest(bucket, imageUrl);
//        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, imageUrl));
        int pos = imageUrl.lastIndexOf("/");

        String key = dirName + imageUrl.substring(pos);

        log.info(key);

        DeleteObjectRequest request = new DeleteObjectRequest(bucket, key);
        amazonS3Client.deleteObject(request);

//        amazonS3Client.deleteObject(bucket, key);
    }

    public String uploadFiles(MultipartFile multipartFile, String dirName) throws IOException {

        File uploadFile = convert(multipartFile)  // 파일 변환할 수 없으면 에러
                .orElseThrow(() -> new IllegalArgumentException("error: MultipartFile -> File convert fail"));
        return upload(uploadFile, multipartFile.getOriginalFilename(), dirName);
    }

    public String upload(File uploadFile, String originalFilename, String dirName) {
        String fileName = dirName + "/" + uploadFile.getName();   // S3에 저장된 파일 이름
        String uploadImageUrl = putS3(uploadFile, fileName); // s3로 업로드


        Image image = new Image(originalFilename, uploadImageUrl);
        imageRepository.save(image);

        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    // S3로 업로드
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile));
//                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // 로컬에 저장된 이미지 지우기
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("File delete success");
            return;
        }
        log.info("File delete fail");
    }

    // 로컬에 파일 업로드 하기
    private Optional<File> convert(MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            return Optional.empty();
        }

        // 파일 이름
        String originalFilename = multipartFile.getOriginalFilename();

        // S3에 저장될 파일이름
        String storeFileName = createStoreFileName(originalFilename);

        File convertFile = new File(fileDir + storeFileName);
        multipartFile.transferTo(convertFile);

        return Optional.of(convertFile);
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    /*private String extractFileName(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(0, pos);
    }*/
}
