package com.research.document.config;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;

/**
 * MongoDB GridFS 配置：提供 GridFSBucket Bean，供文档上传/下载使用。
 * Spring Boot 不会自动创建 GridFSBucket，需手动定义。
 */
@Configuration
public class MongoGridFsConfig {

    @Bean
    public GridFSBucket gridFSBucket(MongoDatabaseFactory mongoDatabaseFactory) {
        MongoDatabase database = mongoDatabaseFactory.getMongoDatabase();
        return GridFSBuckets.create(database);
    }
}
