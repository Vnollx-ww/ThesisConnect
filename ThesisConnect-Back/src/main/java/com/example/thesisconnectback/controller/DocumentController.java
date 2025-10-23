package com.example.thesisconnectback.controller;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.Document;
import com.example.thesisconnectback.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 文档管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@CrossOrigin
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public Result<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long selectionId,
            @RequestParam(required = false) Long topicId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            
            Document document = documentService.uploadDocument(file, userId, username, selectionId, topicId);
            return Result.success("文档上传成功", document);
        } catch (Exception e) {
            log.error("文档上传失败：", e);
            return Result.error("文档上传失败：" + e.getMessage());
        }
    }

    /**
     * 下载文档
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            Document document = documentService.getById(id);
            if (document == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = documentService.downloadDocument(id);
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            log.error("文档下载失败：", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            boolean success = documentService.deleteDocument(id, userId);
            if (success) {
                return Result.success("文档删除成功");
            } else {
                return Result.error("文档删除失败");
            }
        } catch (Exception e) {
            log.error("文档删除失败：", e);
            return Result.error("文档删除失败：" + e.getMessage());
        }
    }

    /**
     * 审核文档
     */
    @PostMapping("/{id}/review")
    public Result<Void> reviewDocument(
            @PathVariable Long id,
            @RequestBody Map<String, String> reviewForm,
            HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");
            if (!"teacher".equals(role) && !"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Long reviewerId = (Long) request.getAttribute("userId");
            String reviewerName = (String) request.getAttribute("username");
            String status = reviewForm.get("status");
            String comment = reviewForm.get("comment");

            boolean success = documentService.reviewDocument(id, status, comment, reviewerId, reviewerName);
            if (success) {
                return Result.success("审核完成");
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            log.error("文档审核失败：", e);
            return Result.error("文档审核失败：" + e.getMessage());
        }
    }

    /**
     * 根据选题ID获取文档列表
     */
    @GetMapping("/selection/{selectionId}")
    public Result<List<Document>> getDocumentsBySelection(@PathVariable Long selectionId) {
        try {
            List<Document> documents = documentService.getDocumentsBySelectionId(selectionId);
            return Result.success(documents);
        } catch (Exception e) {
            log.error("获取文档列表失败：", e);
            return Result.error("获取文档列表失败");
        }
    }

    /**
     * 根据课题ID获取文档列表
     */
    @GetMapping("/topic/{topicId}")
    public Result<List<Document>> getDocumentsByTopic(@PathVariable Long topicId) {
        try {
            List<Document> documents = documentService.getDocumentsByTopicId(topicId);
            return Result.success(documents);
        } catch (Exception e) {
            log.error("获取文档列表失败：", e);
            return Result.error("获取文档列表失败");
        }
    }

    /**
     * 根据上传者ID获取文档列表
     */
    @GetMapping("/uploader/{uploaderId}")
    public Result<List<Document>> getDocumentsByUploader(@PathVariable Long uploaderId, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");
            
            if (!"admin".equals(role) && !userId.equals(uploaderId)) {
                return Result.forbidden("权限不足");
            }

            List<Document> documents = documentService.getDocumentsByUploaderId(uploaderId);
            return Result.success(documents);
        } catch (Exception e) {
            log.error("获取文档列表失败：", e);
            return Result.error("获取文档列表失败");
        }
    }

    /**
     * 获取文档统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDocumentStats(HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以查看统计信息）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Map<String, Object> stats = documentService.getDocumentStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取文档统计信息失败：", e);
            return Result.error("获取文档统计信息失败");
        }
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public Result<Document> getDocumentById(@PathVariable Long id) {
        try {
            Document document = documentService.getById(id);
            if (document != null) {
                return Result.success(document);
            } else {
                return Result.notFound("文档不存在");
            }
        } catch (Exception e) {
            log.error("获取文档详情失败：", e);
            return Result.error("获取文档详情失败");
        }
    }
}
