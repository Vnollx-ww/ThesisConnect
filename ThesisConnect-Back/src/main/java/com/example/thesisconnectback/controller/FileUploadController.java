package com.example.thesisconnectback.controller;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileUploadController {

    @Autowired
    private OssService ossService;

    // MinIO配置信息（应与MinioConfig保持一致，或者注入配置）
    // 为了简单，这里硬编码或者从配置获取。
    // 但是MinioConfig中的值是硬编码的。
    private static final String MINIO_ENDPOINT = "http://111.230.105.54:9000";
    private static final String BUCKET_NAME = "topic";

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 上传文件
            ossService.uploadFile(fileName, file);
            
            // 构造完整URL
            String fileUrl = MINIO_ENDPOINT + "/" + BUCKET_NAME + "/" + fileName;
            
            return Result.success("上传成功", fileUrl);
        } catch (Exception e) {
            log.error("文件上传失败：", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}
