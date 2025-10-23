package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文档服务接口
 */
public interface DocumentService extends IService<Document> {

    /**
     * 上传文档
     */
    Document uploadDocument(MultipartFile file, Long uploaderId, String uploaderName, Long selectionId, Long topicId);

    /**
     * 下载文档
     */
    byte[] downloadDocument(Long documentId);

    /**
     * 删除文档
     */
    boolean deleteDocument(Long documentId, Long userId);

    /**
     * 审核文档
     */
    boolean reviewDocument(Long documentId, String status, String comment, Long reviewerId, String reviewerName);

    /**
     * 根据选题ID获取文档列表
     */
    List<Document> getDocumentsBySelectionId(Long selectionId);

    /**
     * 根据课题ID获取文档列表
     */
    List<Document> getDocumentsByTopicId(Long topicId);

    /**
     * 根据上传者ID获取文档列表
     */
    List<Document> getDocumentsByUploaderId(Long uploaderId);

    /**
     * 获取文档统计信息
     */
    Map<String, Object> getDocumentStats();

    /**
     * 增加下载次数
     */
    void incrementDownloadCount(Long documentId);
}
