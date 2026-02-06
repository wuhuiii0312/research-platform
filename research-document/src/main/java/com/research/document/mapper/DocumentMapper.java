package com.research.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.research.document.entity.Document;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档元数据 Mapper（对应表 document_meta）
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}

