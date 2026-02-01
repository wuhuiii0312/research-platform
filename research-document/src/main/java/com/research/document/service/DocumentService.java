package com.research.document.service;

import com.research.common.core.domain.CommonResult;
import com.research.document.entity.Document;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档服务接口
 */
public interface DocumentService {

    CommonResult<?> uploadDocument(MultipartFile file, Document document);

    CommonResult<?> downloadDocument(Long id);

    CommonResult<?> getDocumentContent(Long id);

    CommonResult<?> updateDocumentContent(Long id, String newContent, String changeLog);

    CommonResult<?> searchDocuments(String keyword, String projectId,
                                    Integer pageNum, Integer pageSize);
}
