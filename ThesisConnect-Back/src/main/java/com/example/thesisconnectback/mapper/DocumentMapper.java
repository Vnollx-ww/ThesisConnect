package com.example.thesisconnectback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.thesisconnectback.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 文档Mapper接口
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 根据选题ID获取文档列表
     */
    @Select("SELECT * FROM sys_document WHERE selection_id = #{selectionId} AND deleted = 0 ORDER BY create_time DESC")
    List<Document> findBySelectionId(@Param("selectionId") Long selectionId);

    /**
     * 根据课题ID获取文档列表
     */
    @Select("SELECT * FROM sys_document WHERE topic_id = #{topicId} AND deleted = 0 ORDER BY create_time DESC")
    List<Document> findByTopicId(@Param("topicId") Long topicId);

    /**
     * 根据上传者ID获取文档列表
     */
    @Select("SELECT * FROM sys_document WHERE uploader_id = #{uploaderId} AND deleted = 0 ORDER BY create_time DESC")
    List<Document> findByUploaderId(@Param("uploaderId") Long uploaderId);

    /**
     * 获取文档统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalDocuments, " +
            "COUNT(CASE WHEN status = 'pending' THEN 1 END) as pendingDocuments, " +
            "COUNT(CASE WHEN status = 'approved' THEN 1 END) as approvedDocuments, " +
            "COUNT(CASE WHEN status = 'rejected' THEN 1 END) as rejectedDocuments, " +
            "SUM(size) as totalSize " +
            "FROM sys_document WHERE deleted = 0")
    Map<String, Object> getDocumentStats();

    /**
     * 增加下载次数
     */
    @Select("UPDATE sys_document SET download_count = download_count + 1 WHERE id = #{id}")
    void incrementDownloadCount(@Param("id") Long id);
}