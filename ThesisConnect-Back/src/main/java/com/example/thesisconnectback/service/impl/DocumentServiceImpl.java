package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.Document;
import com.example.thesisconnectback.mapper.DocumentMapper;
import com.example.thesisconnectback.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文档服务实现类
 */
@Slf4j
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Override
    public Document uploadDocument(MultipartFile file, Long uploaderId, String uploaderName, Long selectionId, Long topicId) {
        try {
            // 检查文件
            if (file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;

            // 创建上传目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(uploadPath, fileName);
            Files.copy(file.getInputStream(), filePath);

            // 创建文档记录
            Document document = new Document();
            document.setName(fileName);
            document.setOriginalName(originalFilename);
            document.setType(getDocumentType(extension));
            document.setSize(file.getSize());
            document.setPath(filePath.toString());
            document.setExtension(extension);
            document.setUploaderId(uploaderId);
            document.setUploaderName(uploaderName);
            document.setSelectionId(selectionId);
            document.setTopicId(topicId);
            document.setStatus("pending");
            document.setDownloadCount(0);
            document.setCreateTime(LocalDateTime.now());
            document.setUpdateTime(LocalDateTime.now());

            this.save(document);
            return document;

        } catch (IOException e) {
            log.error("文件上传失败：", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public byte[] downloadDocument(Long documentId) {
        Document document = this.getById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        try {
            // 增加下载次数
            this.incrementDownloadCount(documentId);
            
            Path filePath = Paths.get(document.getPath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("文件下载失败：", e);
            throw new RuntimeException("文件下载失败");
        }
    }

    @Override
    public boolean deleteDocument(Long documentId, Long userId) {
        Document document = this.getById(documentId);
        if (document == null) {
            return false;
        }

        // 检查权限：只有上传者可以删除
        if (!document.getUploaderId().equals(userId)) {
            throw new RuntimeException("权限不足");
        }

        try {
            // 删除物理文件
            Path filePath = Paths.get(document.getPath());
            Files.deleteIfExists(filePath);
            
            // 删除数据库记录
            return this.removeById(documentId);
        } catch (IOException e) {
            log.error("文件删除失败：", e);
            throw new RuntimeException("文件删除失败");
        }
    }

    @Override
    public boolean reviewDocument(Long documentId, String status, String comment, Long reviewerId, String reviewerName) {
        Document document = this.getById(documentId);
        if (document == null) {
            return false;
        }

        document.setStatus(status);
        document.setReviewComment(comment);
        document.setReviewerId(reviewerId);
        document.setReviewerName(reviewerName);
        document.setReviewTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());

        return this.updateById(document);
    }

    @Override
    public List<Document> getDocumentsBySelectionId(Long selectionId) {
        return baseMapper.findBySelectionId(selectionId);
    }

    @Override
    public List<Document> getDocumentsByTopicId(Long topicId) {
        return baseMapper.findByTopicId(topicId);
    }

    @Override
    public List<Document> getDocumentsByUploaderId(Long uploaderId) {
        return baseMapper.findByUploaderId(uploaderId);
    }

    @Override
    public Map<String, Object> getDocumentStats() {
        return baseMapper.getDocumentStats();
    }

    @Override
    public void incrementDownloadCount(Long documentId) {
        baseMapper.incrementDownloadCount(documentId);
    }

    /**
     * 根据文件扩展名获取文档类型
     */
    private String getDocumentType(String extension) {
        switch (extension.toLowerCase()) {
            case ".doc":
            case ".docx":
                return "Word文档";
            case ".pdf":
                return "PDF文档";
            case ".ppt":
            case ".pptx":
                return "PPT文档";
            case ".xls":
            case ".xlsx":
                return "Excel文档";
            case ".txt":
                return "文本文件";
            case ".zip":
            case ".rar":
                return "压缩文件";
            default:
                return "其他文件";
        }
    }
}
