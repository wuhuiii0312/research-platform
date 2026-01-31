package com.research.document.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.exception.BusinessException;
import com.research.document.entity.Document;
import com.research.document.entity.mongo.DocumentContent;
import com.research.document.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Override
    public CommonResult<?> uploadDocument(MultipartFile file, Document document) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        try {
            // 1. 保存文件到GridFS
            String filename = generateFileName(file.getOriginalFilename());
            ObjectId fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    filename,
                    file.getContentType()
            );

            // 2. 保存文档元数据到MySQL（通过Feign调用）
            document.setFileUrl(fileId.toString());
            document.setFileSize(file.getSize());
            document.setMongoId(fileId.toString());

            // TODO: 调用自己的保存方法
            // documentMapper.insert(document);

            // 3. 如果是文本文件，提取内容保存到MongoDB
            if (isTextFile(file.getContentType())) {
                saveDocumentContent(document.getId(), file);
            }

            log.info("上传文档成功: docId={}, fileName={}", document.getId(), filename);
            return CommonResult.success(document);

        } catch (IOException e) {
            log.error("上传文档失败: {}", e.getMessage());
            throw new BusinessException("上传文件失败");
        }
    }

    @Override
    public CommonResult<?> downloadDocument(Long id) {
        Document document = getDocumentById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        try {
            // 从GridFS获取文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    new Query(Criteria.where("_id").is(new ObjectId(document.getMongoId())))
            );

            if (gridFSFile == null) {
                throw new BusinessException("文件不存在");
            }

            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            GridFsResource resource = new GridFsResource(gridFSFile, downloadStream);

            // 更新下载次数
            updateDownloadCount(id);

            log.info("下载文档成功: docId={}", id);
            return CommonResult.success(resource);

        } catch (Exception e) {
            log.error("下载文档失败: {}", e.getMessage());
            throw new BusinessException("下载文件失败");
        }
    }

    @Override
    public CommonResult<?> getDocumentContent(Long id) {
        // 从MongoDB查询文档内容
        DocumentContent content = mongoTemplate.findOne(
                new Query(Criteria.where("doc_id").is(id)),
                DocumentContent.class
        );

        if (content == null) {
            throw new BusinessException("文档内容不存在");
        }

        return CommonResult.success(content);
    }

    @Override
    public CommonResult<?> updateDocumentContent(Long id, String newContent, String changeLog) {
        // 1. 获取当前文档内容
        DocumentContent currentContent = mongoTemplate.findOne(
                new Query(Criteria.where("doc_id").is(id)),
                DocumentContent.class
        );

        if (currentContent == null) {
            throw new BusinessException("文档不存在");
        }

        // 2. 创建新版本
        DocumentContent newVersion = new DocumentContent();
        newVersion.setDocId(id);
        newVersion.setContent(newContent);
        newVersion.setVersion(currentContent.getVersion() + 1);
        newVersion.setChangeLog(changeLog);
        newVersion.setCreatedBy(getCurrentUserId());
        newVersion.setCreatedAt(LocalDateTime.now());

        // 3. 保存新版本
        mongoTemplate.save(newVersion);

        log.info("更新文档内容成功: docId={}, version={}", id, newVersion.getVersion());
        return CommonResult.success(newVersion);
    }

    @Override
    public CommonResult<?> searchDocuments(String keyword, String projectId,
                                           Integer pageNum, Integer pageSize) {
        // 使用Elasticsearch进行全文搜索
        // TODO: 实现Elasticsearch搜索

        return CommonResult.success("搜索功能");
    }

    // 私有方法

    private String generateFileName(String originalFilename) {
        String ext = FileUtil.extName(originalFilename);
        return IdUtil.simpleUUID() + "." + ext;
    }

    private boolean isTextFile(String contentType) {
        return contentType != null && (
                contentType.startsWith("text/") ||
                        contentType.equals("application/pdf") ||
                        contentType.equals("application/msword") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }

    private void saveDocumentContent(Long docId, MultipartFile file) throws IOException {
        String content = extractTextFromFile(file);

        if (StrUtil.isNotBlank(content)) {
            DocumentContent docContent = new DocumentContent();
            docContent.setDocId(docId);
            docContent.setContent(content);
            docContent.setVersion(1);
            docContent.setChangeLog("初始版本");
            docContent.setCreatedBy(getCurrentUserId());
            docContent.setCreatedAt(LocalDateTime.now());

            mongoTemplate.save(docContent);
        }
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        // TODO: 实现文件内容提取
        // 可以使用Apache Tika或自定义解析器
        return "提取的文本内容";
    }

    private Document getDocumentById(Long id) {
        // TODO: 从MySQL查询
        return new Document();
    }

    private void updateDownloadCount(Long id) {
        // TODO: 更新下载次数
    }

    private Long getCurrentUserId() {
        // TODO: 获取当前用户ID
        return 1L;
    }
}