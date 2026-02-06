package com.research.document.service;

import com.research.common.core.domain.CommonResult;
import com.research.document.entity.Document;
import com.research.document.entity.DocumentAnnotation;
import com.research.document.entity.DocumentVersion;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档服务接口（含测试数据过滤 test_data_flag=0）
 */
public interface DocumentService {

    CommonResult<?> uploadDocument(MultipartFile file, Document document);

    CommonResult<?> downloadDocument(Long id);

    CommonResult<?> downloadDocument(Long documentId, String versionNo);

    /** 预览：返回可读流，权限+测试数据过滤 */
    Resource previewDocument(Long documentId, String versionNo);

    CommonResult<?> getDocumentContent(Long id);

    CommonResult<?> updateDocumentContent(Long id, String newContent, String changeLog);

    CommonResult<?> searchDocuments(String keyword, String projectId,
                                    Integer pageNum, Integer pageSize);

    /** 版本列表（过滤 test_data_flag=1） */
    CommonResult<List<DocumentVersion>> getVersionList(Long documentId);

    /** 回滚到指定版本 */
    CommonResult<?> rollback(Long documentId, String targetVersionNo);

    /** 批注：添加 */
    CommonResult<?> addAnnotation(DocumentAnnotation annotation);

    /** 批注：列表（过滤测试数据） */
    CommonResult<List<DocumentAnnotation>> listAnnotations(Long documentId);

    /** 批注：删除（负责人可删） */
    CommonResult<?> deleteAnnotation(Long annotationId);

    /** 按项目ID批量删除文档（项目解散时调用） */
    CommonResult<?> deleteDocumentsByProjectId(Long projectId);
}
