package com.caq.mall.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfig {
//    官方建议把requestOptions创建成单实例
    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient() {

        RestClientBuilder builder = null;
        // 可以指定多个es
        builder = RestClient.builder(new HttpHost("192.168.1.8", 9200, "http"));

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}
