package com.research.document.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.exception.BusinessException;
import com.research.common.core.util.SecurityUtils;
import com.research.document.entity.Document;
import com.research.document.entity.DocumentAnnotation;
import com.research.document.entity.DocumentVersion;
import com.research.document.entity.mongo.DocumentContent;
import com.research.document.mapper.DocumentAnnotationMapper;
import com.research.document.mapper.DocumentMapper;
import com.research.document.mapper.DocumentVersionMapper;
import com.research.document.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentVersionMapper documentVersionMapper;

    @Autowired
    private DocumentAnnotationMapper documentAnnotationMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public CommonResult<?> uploadDocument(MultipartFile file, Document document) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        try {
            // 1. 保存文件到 GridFS（testDataFlag 通过元数据或业务表体现，此处仅存文件）
            String filename = generateFileName(file.getOriginalFilename());
            ObjectId fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    filename,
                    file.getContentType()
            );

            Long creatorId = SecurityUtils.getUserId();
            if (creatorId == null) creatorId = 1L;
            document.setCreatorId(creatorId);
            document.setDelFlag(0);
            document.setTestDataFlag(0);
            if (document.getIsPublic() == null) document.setIsPublic(0);
            document.setFileUrl(fileId.toString());
            document.setFileSize(file.getSize());
            document.setMongoId(fileId.toString());

            documentMapper.insert(document);

            // 2. 新增 document_version 记录（1.0）
            DocumentVersion version = new DocumentVersion();
            version.setDocumentId(document.getId());
            version.setVersionNo("1.0");
            version.setMongoFileId(fileId.toString());
            version.setUploadTime(LocalDateTime.now());
            version.setChangeDesc("初始版本");
            version.setUploaderId(creatorId);
            version.setTestDataFlag(0);
            documentVersionMapper.insert(version);

            // 3. 文本文件提取内容到 MongoDB
            if (isTextFile(file.getContentType())) {
                saveDocumentContent(document.getId(), file);
            }

            // 4. 同步文档到项目详情（仅非测试数据，通过 HTTP 调用项目服务）
            if (document.getTestDataFlag() != null && document.getTestDataFlag() == 0) {
                syncDocumentToProjectDetail(document.getProjectId(), document.getId());
            }

            log.info("上传文档成功: docId={}, fileName={}", document.getId(), filename);

            // 通知文档上传人：上传成功
            notifyDocUploadSuccess(document);
            return CommonResult.success(document);

        } catch (IOException e) {
            log.error("上传文档失败: {}", e.getMessage());
            throw new BusinessException("上传文件失败");
        }
    }

    @Override
    public CommonResult<?> downloadDocument(Long id) {
        return downloadDocument(id, null);
    }

    @Override
    public CommonResult<?> downloadDocument(Long documentId, String versionNo) {
        Document doc = getDocumentById(documentId);
        if (doc == null) throw new BusinessException("文档不存在或无权访问");
        String mongoId = doc.getMongoId();
        if (StrUtil.isNotBlank(versionNo)) {
            DocumentVersion ver = getVersionByDocAndNo(documentId, versionNo);
            if (ver != null && StrUtil.isNotBlank(ver.getMongoFileId())) mongoId = ver.getMongoFileId();
        }
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(mongoId))));
            if (gridFSFile == null) throw new BusinessException("文件不存在");
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            GridFsResource resource = new GridFsResource(gridFSFile, downloadStream);
            log.info("下载文档成功: docId={}", documentId);
            return CommonResult.success(resource);
        } catch (Exception e) {
            log.error("下载文档失败: {}", e.getMessage());
            throw new BusinessException("下载文件失败");
        }
    }

    @Override
    public Resource previewDocument(Long documentId, String versionNo) {
        Document doc = getDocumentById(documentId);
        if (doc == null) throw new BusinessException("文档不存在或无权访问");
        String mongoId = doc.getMongoId();
        if (StrUtil.isNotBlank(versionNo)) {
            DocumentVersion ver = getVersionByDocAndNo(documentId, versionNo);
            if (ver != null && StrUtil.isNotBlank(ver.getMongoFileId())) mongoId = ver.getMongoFileId();
        }
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(mongoId))));
            if (gridFSFile == null) throw new BusinessException("文件不存在");
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            return new GridFsResource(gridFSFile, downloadStream);
        } catch (Exception e) {
            log.error("预览文档失败: {}", e.getMessage());
            throw new BusinessException("预览失败");
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
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        // 1. 纯文本文件
        if (contentType.startsWith("text/") || originalName.endsWith(".txt")) {
            return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }

        // 2. Word 文档（.docx/.doc）- 使用 Apache POI 提取纯文本
        try (InputStream is = file.getInputStream()) {
            if (originalName.endsWith(".docx")) {
                try (org.apache.poi.xwpf.usermodel.XWPFDocument docx = new org.apache.poi.xwpf.usermodel.XWPFDocument(is);
                     org.apache.poi.xwpf.extractor.XWPFWordExtractor extractor =
                             new org.apache.poi.xwpf.extractor.XWPFWordExtractor(docx)) {
                    return extractor.getText();
                }
            }
            if (originalName.endsWith(".doc")) {
                try (org.apache.poi.hwpf.HWPFDocument doc = new org.apache.poi.hwpf.HWPFDocument(is);
                     org.apache.poi.hwpf.extractor.WordExtractor extractor =
                             new org.apache.poi.hwpf.extractor.WordExtractor(doc)) {
                    return extractor.getText();
                }
            }
        } catch (Exception e) {
            log.warn("提取 Word 文档文本失败: name={}, error={}", originalName, e.getMessage());
        }

        // 3. PDF 文档（简单文本提取）
        if (originalName.endsWith(".pdf") || "application/pdf".equals(contentType)) {
            try (InputStream is = file.getInputStream();
                 org.apache.pdfbox.pdmodel.PDDocument pdf = org.apache.pdfbox.pdmodel.PDDocument.load(is)) {
                org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                return stripper.getText(pdf);
            } catch (Exception e) {
                log.warn("提取 PDF 文本失败: name={}, error={}", originalName, e.getMessage());
            }
        }

        // 4. 其它二进制文件暂不支持在线预览
        return "该文件类型暂不支持在线预览，请点击右上角“下载”后在本地打开。";
    }

    private Document getDocumentById(Long id) {
        return documentMapper.selectOne(new LambdaQueryWrapper<Document>()
                .eq(Document::getId, id)
                .eq(Document::getDelFlag, 0)
                .eq(Document::getTestDataFlag, 0));
    }

    private DocumentVersion getVersionByDocAndNo(Long documentId, String versionNo) {
        return documentVersionMapper.selectOne(new LambdaQueryWrapper<DocumentVersion>()
                .eq(DocumentVersion::getDocumentId, documentId)
                .eq(DocumentVersion::getVersionNo, versionNo)
                .eq(DocumentVersion::getTestDataFlag, 0));
    }

    private Long getCurrentUserId() {
        Long id = SecurityUtils.getUserId();
        return id != null ? id : 1L;
    }

    @Override
    public CommonResult<List<DocumentVersion>> getVersionList(Long documentId) {
        List<DocumentVersion> list = documentVersionMapper.selectList(
                new LambdaQueryWrapper<DocumentVersion>()
                        .eq(DocumentVersion::getDocumentId, documentId)
                        .eq(DocumentVersion::getTestDataFlag, 0)
                        .orderByDesc(DocumentVersion::getUploadTime));
        return CommonResult.success(list);
    }

    @Override
    public CommonResult<?> rollback(Long documentId, String targetVersionNo) {
        DocumentVersion target = getVersionByDocAndNo(documentId, targetVersionNo);
        if (target == null) throw new BusinessException("目标版本不存在");
        Document doc = getDocumentById(documentId);
        if (doc == null) throw new BusinessException("文档不存在");
        String nextNo = String.format("%.1f", Double.parseDouble(targetVersionNo) + 1.0);
        DocumentVersion newVer = new DocumentVersion();
        newVer.setDocumentId(documentId);
        newVer.setVersionNo(nextNo);
        newVer.setMongoFileId(target.getMongoFileId());
        newVer.setUploadTime(LocalDateTime.now());
        newVer.setChangeDesc("回滚至v" + targetVersionNo);
        newVer.setUploaderId(getCurrentUserId());
        newVer.setTestDataFlag(0);
        documentVersionMapper.insert(newVer);
        return CommonResult.success("回滚成功");
    }

    @Override
    public CommonResult<?> addAnnotation(DocumentAnnotation annotation) {
        annotation.setCreateTime(LocalDateTime.now());
        annotation.setDelFlag(0);
        annotation.setTestDataFlag(0);
        annotation.setCreatorId(annotation.getCreatorId() != null ? annotation.getCreatorId() : getCurrentUserId());
        documentAnnotationMapper.insert(annotation);
        return CommonResult.success(annotation);
    }

    @Override
    public CommonResult<List<DocumentAnnotation>> listAnnotations(Long documentId) {
        List<DocumentAnnotation> list = documentAnnotationMapper.selectList(
                new LambdaQueryWrapper<DocumentAnnotation>()
                        .eq(DocumentAnnotation::getDocumentId, documentId)
                        .eq(DocumentAnnotation::getDelFlag, 0)
                        .eq(DocumentAnnotation::getTestDataFlag, 0)
                        .orderByAsc(DocumentAnnotation::getCreateTime));
        return CommonResult.success(list);
    }

    @Override
    public CommonResult<?> deleteAnnotation(Long annotationId) {
        DocumentAnnotation a = documentAnnotationMapper.selectById(annotationId);
        if (a == null) throw new BusinessException("批注不存在");
        a.setDelFlag(1);
        documentAnnotationMapper.updateById(a);
        return CommonResult.success("删除成功");
    }

    @Override
    public CommonResult<?> deleteDocumentsByProjectId(Long projectId) {
        if (projectId == null) {
            throw new BusinessException("项目ID不能为空");
        }
        
        // 查询项目下的所有文档
        List<Document> documents = documentMapper.selectList(new LambdaQueryWrapper<Document>()
                .eq(Document::getProjectId, projectId)
                .eq(Document::getDelFlag, 0));
        
        if (documents == null || documents.isEmpty()) {
            log.info("项目下没有文档需要删除: projectId={}", projectId);
            return CommonResult.success("删除成功");
        }
        
        // 批量逻辑删除
        int count = 0;
        for (Document doc : documents) {
            int rows = documentMapper.update(null,
                    new LambdaUpdateWrapper<Document>()
                            .eq(Document::getId, doc.getId())
                            .set(Document::getDelFlag, 1));
            if (rows > 0) {
                count++;
            }
        }
        
        log.info("按项目ID批量删除文档成功: projectId={}, 删除数量={}", projectId, count);
        return CommonResult.success("删除成功，共删除 " + count + " 个文档");
    }

    /**
     * 同步文档到项目详情（调用项目服务接口，使用 Nacos 服务发现）
     */
    private void syncDocumentToProjectDetail(Long projectId, Long documentId) {
        if (restTemplate == null) {
            log.warn("RestTemplate 未配置，跳过文档同步到项目详情");
            return;
        }
        try {
            // 使用服务名调用（Nacos 服务发现，@LoadBalanced 会自动解析）
            String url = "http://research-project/project/detail/sync/document?projectId={projectId}&documentId={documentId}";
            restTemplate.postForObject(url, null, CommonResult.class, projectId, documentId);
            log.debug("同步文档到项目详情成功: projectId={}, documentId={}", projectId, documentId);
        } catch (Exception e) {
            log.warn("同步文档到项目详情失败: projectId={}, documentId={}, error={}", projectId, documentId, e.getMessage());
        }
    }

    /**
     * 通知上传人：文档上传成功
     */
    private void notifyDocUploadSuccess(Document document) {
        if (restTemplate == null || document == null || document.getCreatorId() == null) {
            return;
        }
        try {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("targetUserIds", java.util.Collections.singletonList(document.getCreatorId()));
            body.put("title", "文档上传成功");
            body.put("content", "您的文档【" + document.getName() + "】已上传成功");
            body.put("bizType", "DOCUMENT");
            body.put("bizId", document.getId());
            body.put("projectId", document.getProjectId());
            body.put("priority", "NORMAL");
            body.put("actionType", "VIEW_DETAIL");
            body.put("notificationType", "DOC_UPLOAD_SUCCESS");
            body.put("relatedId", String.valueOf(document.getId()));
            body.put("relatedType", "DOCUMENT");
            restTemplate.postForObject(
                    "http://research-notification/internal/notification/send",
                    body,
                    CommonResult.class
            );
        } catch (Exception e) {
            log.warn("发送文档上传成功通知失败: docId={}, error={}", document.getId(), e.getMessage());
        }
    }
}