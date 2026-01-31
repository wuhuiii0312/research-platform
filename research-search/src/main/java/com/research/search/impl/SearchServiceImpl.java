package com.research.search.service.impl;

import cn.hutool.core.util.StrUtil;
import com.research.common.core.domain.CommonResult;
import com.research.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Override
    public CommonResult<?> searchDocuments(String keyword, String projectId, String type,
                                           Integer pageNum, Integer pageSize) {
        try {
            SearchRequest searchRequest = new SearchRequest("research_documents");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 关键词搜索
            if (StrUtil.isNotBlank(keyword)) {
                boolQuery.must(QueryBuilders.multiMatchQuery(keyword,
                        "title", "content", "description", "keywords"));
            }

            // 项目筛选
            if (StrUtil.isNotBlank(projectId)) {
                boolQuery.filter(QueryBuilders.termQuery("project_id", projectId));
            }

            // 类型筛选
            if (StrUtil.isNotBlank(type)) {
                boolQuery.filter(QueryBuilders.termQuery("type", type));
            }

            // 高亮配置
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title").field("content");
            highlightBuilder.preTags("<em>").postTags("</em>");

            // 分页和排序
            sourceBuilder.query(boolQuery);
            sourceBuilder.highlighter(highlightBuilder);
            sourceBuilder.from((pageNum - 1) * pageSize);
            sourceBuilder.size(pageSize);
            sourceBuilder.sort("create_time", SortOrder.DESC);

            searchRequest.source(sourceBuilder);

            // 执行搜索
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

            // 处理结果
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();

                // 添加高亮
                if (hit.getHighlightFields() != null && !hit.getHighlightFields().isEmpty()) {
                    source.put("highlight", hit.getHighlightFields());
                }

                resultList.add(source);
            }

            // 构建返回结果
            Map<String, Object> result = Map.of(
                    "total", response.getHits().getTotalHits().value,
                    "items", resultList,
                    "pageNum", pageNum,
                    "pageSize", pageSize
            );

            return CommonResult.success(result);

        } catch (IOException e) {
            log.error("搜索文档失败: {}", e.getMessage());
            return CommonResult.failed("搜索失败");
        }
    }

    @Override
    public CommonResult<?> searchTasks(String keyword, Long projectId, String status,
                                       Long assigneeId, Integer pageNum, Integer pageSize) {
        try {
            SearchRequest searchRequest = new SearchRequest("research_tasks");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            if (StrUtil.isNotBlank(keyword)) {
                boolQuery.must(QueryBuilders.multiMatchQuery(keyword,
                        "task_name", "task_description"));
            }

            if (projectId != null) {
                boolQuery.filter(QueryBuilders.termQuery("project_id", projectId));
            }

            if (StrUtil.isNotBlank(status)) {
                boolQuery.filter(QueryBuilders.termQuery("status", status));
            }

            if (assigneeId != null) {
                boolQuery.filter(QueryBuilders.termQuery("assignee_id", assigneeId));
            }

            sourceBuilder.query(boolQuery);
            sourceBuilder.from((pageNum - 1) * pageSize);
            sourceBuilder.size(pageSize);
            sourceBuilder.sort("due_date", SortOrder.ASC);

            searchRequest.source(sourceBuilder);

            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

            // 处理结果...
            return CommonResult.success(response.getHits());

        } catch (IOException e) {
            log.error("搜索任务失败: {}", e.getMessage());
            return CommonResult.failed("搜索失败");
        }
    }

    @Override
    public CommonResult<?> searchUsers(String keyword, String role, Integer pageNum, Integer pageSize) {
        try {
            SearchRequest searchRequest = new SearchRequest("research_users");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            if (StrUtil.isNotBlank(keyword)) {
                boolQuery.must(QueryBuilders.multiMatchQuery(keyword,
                        "username", "real_name", "email", "department"));
            }

            if (StrUtil.isNotBlank(role)) {
                boolQuery.filter(QueryBuilders.termQuery("role", role));
            }

            sourceBuilder.query(boolQuery);
            sourceBuilder.from((pageNum - 1) * pageSize);
            sourceBuilder.size(pageSize);

            searchRequest.source(sourceBuilder);

            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

            // 处理结果...
            return CommonResult.success(response.getHits());

        } catch (IOException e) {
            log.error("搜索用户失败: {}", e.getMessage());
            return CommonResult.failed("搜索失败");
        }
    }

    @Override
    public CommonResult<?> getHotKeywords() {
        // 可以从Redis或Elasticsearch获取热门关键词
        List<String> hotKeywords = List.of(
                "深度学习", "人工智能", "机器学习", "图像识别",
                "数据分析", "科研项目", "论文发表", "专利申请"
        );

        return CommonResult.success(hotKeywords);
    }

    @Override
    public CommonResult<?> rebuildIndex() {
        try {
            // 重新构建所有索引
            rebuildDocumentIndex();
            rebuildTaskIndex();
            rebuildUserIndex();

            return CommonResult.success("索引重建成功");
        } catch (Exception e) {
            log.error("重建索引失败: {}", e.getMessage());
            return CommonResult.failed("索引重建失败");
        }
    }

    private void rebuildDocumentIndex() {
        // TODO: 从MySQL和MongoDB同步文档数据到Elasticsearch
        log.info("开始重建文档索引...");
    }

    private void rebuildTaskIndex() {
        // TODO: 从MySQL同步任务数据到Elasticsearch
        log.info("开始重建任务索引...");
    }

    private void rebuildUserIndex() {
        // TODO: 从MySQL同步用户数据到Elasticsearch
        log.info("开始重建用户索引...");
    }
}