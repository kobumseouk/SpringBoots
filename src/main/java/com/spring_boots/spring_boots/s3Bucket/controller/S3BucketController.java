package com.spring_boots.spring_boots.s3Bucket.controller;

import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/s3/bucket")
@RequiredArgsConstructor
public class S3BucketController {
    private final S3BucketService s3BucketService;
    @PostMapping()
    public ResponseEntity<?> uploadfile(@RequestParam("file")MultipartFile file, @RequestParam("path") String path) {
        try {
            String fileUrl = s3BucketService.uploadFile(file);
            return new ResponseEntity<>(fileUrl, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("{/filename}")
    public ResponseEntity<String> deleteBucket(@PathVariable("filename") String filename) {
        try {
            s3BucketService.deleteFile(filename);
            return new ResponseEntity<>("Hello World!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
