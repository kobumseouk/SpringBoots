package com.spring_boots.spring_boots.s3Bucket.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3BucketService {
    private final AmazonS3Client amazonS3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.bucket.url}")
    private String bucketUrl;

    // 아래의 내용을 잘 이용하여 업로드, 삭제를 구현합니다.
    // s3Client.putObject(bucketName, filename, file.getInputStream(), metadata);
    // s3Client.deleteObject(bucketName, filename);

    /**
     * 파일 업로드 시 같은 이름의 파일이 있다면 덮어씁니다.<br/>
     * 같은 이름의 파일이 없다면 새로 생성합니다.
     *
     * @param file Controller에서 받은 MultipartFile 형태의 파일
     * @return {@code bucketUrl/originName-UUID} 형태의 URL 문자열
     * @throws IOException 파일의 InputStream을 받지 못함
     */
    public String upload(MultipartFile file) throws IOException {
        return upload(file, "");
    }

    /**
     * 파일 업로드 시 같은 이름의 파일이 있다면 덮어씁니다.<br/>
     * 같은 이름의 파일이 없다면 새로 생성합니다.
     *
     * @param file Controller에서 받은 MultipartFile 형태의 파일
     * @param path 파일을 저장할 경로
     * @return {@code bucketUrl/{path}/originName-UUID} 형태의 URL 문자열
     * @throws IOException 파일의 InputStream을 받지 못함
     */
    public String upload(MultipartFile file, String path) throws IOException {
        String filename;
        if (path == null || path.isEmpty()) {
            filename = getHashedFilename(file);
        } else {
            filename = (path.endsWith("/") ? path : path + "/") + getHashedFilename(file);
        }
        String fileUrl = bucketUrl + filename;
        ObjectMetadata metadata = createObjectMetadata(file);

        amazonS3Client.putObject(bucketName, filename, file.getInputStream(), metadata);
        return fileUrl;
    }

    /**
     * 파일 업로드 시 같은 이름의 파일이 있다면 덮어씁니다.<br/>
     * 같은 이름의 파일이 없다면 새로 생성합니다.
     *
     * @param files Controller에서 받은 MultipartFile 형태의 파일
     * @return {@code bucketUrl/{path}/originName-UUID} 형태의 URL List 객체
     * @throws IOException 파일의 InputStream을 받지 못함
     */
    public List<String> upload(MultipartFile[] files) throws IOException {
        return upload(files, "");
    }

    /**
     * 파일 업로드 시 같은 이름의 파일이 있다면 덮어씁니다.<br/>
     * 같은 이름의 파일이 없다면 새로 생성합니다.
     *
     * @param files Controller에서 받은 MultipartFile 형태의 파일
     * @param path 파일을 저장할 경로
     * @return {@code bucketUrl/{path}/originName-UUID} 형태의 URL 문자열 List 객체
     * @throws IOException 파일의 InputStream을 받지 못함
     */
    public List<String> upload(MultipartFile[] files, String path) throws IOException {
        return Arrays.stream(files)
                .map(file -> {
                    try {
                        return upload(file, path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * @param filename 삭제할 파일의 경로 혹은 이름
     * @return true - 파일 삭제 성공<br/>
     *         false - 파일 이름이 null이거나 빈 문자열
     * @throws RuntimeException 파일 삭제 중 오류가 발생할 경우 발생
     */
    public boolean remove(String filename) {
        if (filename == null || filename.isEmpty()) return false;
        if (filename.startsWith("http")) {
            filename = filename.substring(bucketUrl.length());
        }
        try {
            amazonS3Client.deleteObject(bucketName, filename);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @param filenames 삭제할 파일의 경로 혹은 이름 배열
     * @return true나 false가 담긴 boolean 배열<br/>
     *         e.g. {@code [ true, true, false, true, false ]}
     * @throws RuntimeException 파일 삭제 중 오류가 발생할 경우 발생
     */
    public List<Boolean> remove(String[] filenames) {
        return Arrays.stream(filenames)
                .map(file -> {
                    try {
                        return remove(file);
                    } catch (RuntimeException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private String getHashedFilename(MultipartFile file) {
        UUID uuid4 = UUID.randomUUID();
        String filename = file.getOriginalFilename();
        return String.format("%s_%s", filename, uuid4);
    }

    private ObjectMetadata createObjectMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        return metadata;
    }

}
