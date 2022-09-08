package com.project.sidefit.domain.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.project.sidefit.domain.entity.Image;
import com.project.sidefit.domain.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    public ResponseEntity<byte[]> downloadFile(String imageUrl, String dirName) throws IOException {
        Image image = imageRepository.findByImageUrl(imageUrl).orElseThrow(IllegalStateException::new);
        String originalFilename = image.getName();
        int pos = imageUrl.lastIndexOf("/");

        String key = dirName + imageUrl.substring(pos);

        S3Object object = amazonS3Client.getObject(bucket, key);
        S3ObjectInputStream s3is = object.getObjectContent();
        ObjectMetadata metadata = object.getObjectMetadata();
        byte[] bytes = IOUtils.toByteArray(s3is);

        String encodedOriginalFilename = UriUtils.encode(originalFilename, StandardCharsets.UTF_8);

        log.info("content type",MediaType.parseMediaType(metadata.getContentType()));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType(metadata.getContentType()));
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", encodedOriginalFilename);

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(bytes);

        /*try {

            // 로컬 디렉토리에 파일 다운로드
            String key = dirName + imageUrl.substring(pos);

            S3Object o = amazonS3Client.getObject(bucket, key);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(file);


            ObjectMetadata objectMetadata = amazonS3Client.getObjectMetadata(bucket, key);



            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }

            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            log.error("error : {}", e);
        } catch (FileNotFoundException e) {
            log.error("error : {}", e);
        } catch (IOException e) {
            log.error("error : {}", e);
        } finally {
            // 로컬 파일 삭제
            removeNewFile(file);
        }

        UrlResource resource = new UrlResource("file:" + path);
        log.info("file:{}", file.getPath());

        String encodedOriginalFilename = UriUtils.encode(originalFilename, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedOriginalFilename + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);*/
    }

    public void deleteFile(String imageUrl, String dirName) {
        imageRepository.deleteByImageUrl(imageUrl);

        int pos = imageUrl.lastIndexOf("/");
        String key = dirName + imageUrl.substring(pos);

        log.info(key);

        DeleteObjectRequest request = new DeleteObjectRequest(bucket, key);
        amazonS3Client.deleteObject(request);
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
