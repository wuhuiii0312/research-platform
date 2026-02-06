package com.research.document.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.exception.BusinessException;
import com.research.common.core.util.SecurityUtils;
import com.research.document.entity.Document;
import com.research.document.entity.mongo.DocumentContent;
import com.research.document.mapper.DocumentMapper;
import com.research.document.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档上传与列表控制器（对接前端 /api/document/**，由网关转发并去掉 /api 前缀）
 */
@Slf4j
@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    /** 支持 POST /document/upload 与 POST /document，避免网关或前端路径截断导致 404 */
    @PostMapping(value = { "/upload", "" })
    public CommonResult<?> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam("projectId") Long projectId,
                                  @RequestParam(value = "isPublic", required = false, defaultValue = "0") Integer isPublic) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (projectId == null) {
            throw new BusinessException("项目ID不能为空");
        }
        // 组装文档元数据（契合开题报告权限设计：公开/仅成员可见）
        Document doc = new Document();
        doc.setProjectId(projectId);
        doc.setName(file.getOriginalFilename()); // 文档名称
        doc.setPermissionType(isPublic == 1 ? "VISITOR" : "MEMBER"); // 权限类型：访客可见/仅成员可见
        doc.setDelFlag(0); // 逻辑删除：默认正常

        log.info("收到文档上传请求: projectId={}, name={}, isPublic={}", projectId, doc.getName(), isPublic);
        CommonResult<?> result = documentService.uploadDocument(file, doc);
        // 上传成功后同步刷新缓存（可选，提升列表查询效率）
        if (result.getCode() == 200) {
            log.info("文档上传成功: {}", doc.getName());
        }
        return result;
    }

    /**
     * 文档列表查询（默认过滤 test_data_flag=0）
     * - 访客端（isPublic=1）：返回所有公开项目的公开文档
     * - 其他角色：仅展示当前用户参与项目下的文档
     */
    @GetMapping("/list")
    public CommonResult<List<Document>> list(@RequestParam(required = false) Long projectId,
                                             @RequestParam(required = false) Integer isPublic) {
        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<Document>()
                .eq(Document::getDelFlag, 0)
                .eq(Document::getTestDataFlag, 0)
                .orderByDesc(Document::getCreateTime);
        
        Long currentUserId = SecurityUtils.getUserId();
        boolean isVisitor = SecurityUtils.isGlobalVisitor();
        
        // 访客端且 isPublic=1：返回所有公开项目的公开文档
        if (isVisitor && isPublic != null && isPublic == 1) {
            // 查询所有公开项目（is_public=1）的公开文档（is_public=1）
            queryWrapper.inSql(Document::getProjectId,
                    "SELECT id FROM project WHERE is_public = 1 AND del_flag = 0")
                    .eq(Document::getIsPublic, 1);
        } else {
            // 其他角色：仅展示当前用户参与项目下的文档
            if (currentUserId != null && currentUserId > 0) {
                queryWrapper.inSql(Document::getProjectId,
                        "SELECT project_id FROM project_member WHERE user_id = " + currentUserId + " AND status = 1");
            }
            if (projectId != null) {
                queryWrapper.eq(Document::getProjectId, projectId);
            }
            if (isPublic != null && isPublic == 1) {
                queryWrapper.eq(Document::getIsPublic, 1);
            }
        }
        
        List<Document> documentList = documentMapper.selectList(queryWrapper);
        log.info("查询文档列表，projectId={}，isPublic={}，isVisitor={}，共{}条", 
                projectId, isPublic, isVisitor, documentList.size());
        return CommonResult.success(documentList);
    }

    /** 逻辑删除（使用 query 传参，避免网关/代理对 path variable 处理异常导致 404） */
    @DeleteMapping("")
    public CommonResult<?> delete(@RequestParam("id") Long id) {
        if (id == null) throw new BusinessException("文档ID不能为空");
        int rows = documentMapper.update(null,
                new LambdaUpdateWrapper<Document>()
                        .eq(Document::getId, id)
                        .set(Document::getDelFlag, 1));
        if (rows == 0) throw new BusinessException("文档不存在");
        return CommonResult.success("删除成功");
    }

    /** 按项目ID批量删除文档（项目解散时调用） */
    @PostMapping("/delete/byProjectId")
    public CommonResult<?> deleteDocumentsByProjectId(@RequestBody java.util.Map<String, Object> params) {
        Long projectId = ((Number) params.get("projectId")).longValue();
        return documentService.deleteDocumentsByProjectId(projectId);
    }

    /** 版本列表（过滤 test_data_flag=1） */
    @GetMapping("/version/list")
    public CommonResult<?> getVersionList(@RequestParam Long docId) {
        return documentService.getVersionList(docId);
    }

    /** 版本历史：MySQL document_version 表 */
    @GetMapping("/version/{documentId}")
    public CommonResult<?> getVersionListByPath(@PathVariable Long documentId) {
        return documentService.getVersionList(documentId);
    }

    /** 回滚到指定版本 */
    @PostMapping("/rollback")
    public CommonResult<?> rollback(@RequestBody Map<String, Object> params) {
        Long docId = params.get("documentId") != null ? Long.valueOf(params.get("documentId").toString()) : null;
        String targetVersionNo = params.get("targetVersionNo") != null ? params.get("targetVersionNo").toString() : null;
        if (docId == null || targetVersionNo == null) throw new BusinessException("documentId与targetVersionNo不能为空");
        return documentService.rollback(docId, targetVersionNo);
    }

    @GetMapping("/content/{id}")
    public CommonResult<?> getContent(@PathVariable Long id) {
        Query q = new Query(Criteria.where("doc_id").is(id)).with(Sort.by(Sort.Direction.DESC, "version")).limit(1);
        DocumentContent dc = mongoTemplate.findOne(q, DocumentContent.class);
        if (dc == null) return CommonResult.success("");
        return CommonResult.success(dc.getHtmlContent() != null ? dc.getHtmlContent() : dc.getContent());
    }

    /** 预览：返回文件流，前端按格式渲染 */
    @GetMapping("/preview/{documentId}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> preview(
            @PathVariable Long documentId,
            @RequestParam(required = false) String versionNo) {
        org.springframework.core.io.Resource resource = documentService.previewDocument(documentId, versionNo);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    /** 下载（支持 versionNo 指定版本，直接返回文件流而不是 JSON） */
    @GetMapping("/download/{documentId}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> download(
            @PathVariable Long documentId,
            @RequestParam(required = false) String versionNo) {
        CommonResult<?> result = documentService.downloadDocument(documentId, versionNo);
        org.springframework.core.io.Resource resource = (org.springframework.core.io.Resource) result.getData();
        if (resource == null) {
            throw new BusinessException("文件不存在");
        }
        Document doc = documentMapper.selectById(documentId);
        String filename = doc != null && doc.getName() != null ? doc.getName() : "file";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encoded + "\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(resource);
    }

    /** 批注列表（过滤测试数据） */
    @GetMapping("/annotation/list/{documentId}")
    public CommonResult<?> listAnnotations(@PathVariable Long documentId) {
        return documentService.listAnnotations(documentId);
    }

    @PostMapping("/annotation/add")
    public CommonResult<?> addAnnotation(@RequestBody com.research.document.entity.DocumentAnnotation annotation) {
        return documentService.addAnnotation(annotation);
    }

    @PostMapping("/annotation/delete/{annotationId}")
    public CommonResult<?> deleteAnnotation(@PathVariable Long annotationId) {
        return documentService.deleteAnnotation(annotationId);
    }
}