package com.spring_boots.spring_boots.s3Bucket.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.spring_boots.spring_boots.s3Bucket.config.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
public class S3BucketService {

    private final S3Config s3Config;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    private String getFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }

    // 파일 기본 경로 업로드
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = makeHashedFileName(file);
        String fileUrl = "https://" +  bucketName+ "s3.amazonaws.com/" + fileName;
        ObjectMetadata metadata = createFileMetadata(file);
        try {
            s3Config.amazonS3Client().putObject(bucketName, fileName, file.getInputStream(), metadata);
            return fileUrl;
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    // 파일 사용자 지정 경로 업로드
    public String uploadFile(MultipartFile file, String path) throws IOException {
        String fileName = (path.endsWith("/") ? path : path + "/") + makeHashedFileName(file);
        String fileUrl = "https://" +  bucketName+ "s3.amazonaws.com/" + fileName;
        ObjectMetadata metadata =  createFileMetadata(file);
        try {
            s3Config.amazonS3Client().putObject(bucketName, fileName, file.getInputStream(), metadata);
            return fileUrl;
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    // 고유한 파일 이름 생성 기능
    public String makeHashedFileName (MultipartFile file) {
        LocalDateTime now = LocalDateTime.now(); // 현재 시간 구하기
        String fileName = file.getOriginalFilename(); // 원본 파일 이름 가져오기
        int hashed = (fileName + now.format(DateTimeFormatter.ofPattern("HHmmss"))).hashCode(); // 원본파일 이름 + 현재 시간 조합하여 해쉬 생성
        return now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + hashed; // 해쉬 값 + 현재 시간 조합으로 고유한 파일 이름 생성
    }

    // S3에 업로드할 파일의 메타데이터 생성 기능
    public ObjectMetadata createFileMetadata (MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType()); // 파일 타입 설정
        metadata.setContentLength(file.getSize()); // 파일 크기 설정
        return metadata;
    }

    public void deleteFile(String fileName) {
        try {
            s3Config.amazonS3Client().deleteObject(bucketName, fileName);
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 실패: " + e.getMessage());
        }
    }


}
