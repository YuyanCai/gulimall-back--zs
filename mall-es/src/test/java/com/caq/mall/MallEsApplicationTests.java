package com.caq.mall;

import com.alibaba.fastjson.JSON;
import com.caq.mall.config.EsConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.naming.directory.SearchResult;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallEsApplicationTests {

    @Resource
    private RestHighLevelClient client;

    // 1.ES中保存数据
    @Test
    public void contextLoads() throws IOException {
        // 设置索引
        IndexRequest indexRequest = new IndexRequest ("users");
        indexRequest.id("1");

        User user = new User();
        user.setUserName("张三");
        user.setAge(20);
        user.setGender("男");

        String jsonString = JSON.toJSONString(user);
        //设置要保存的内容，指定数据和类型
        indexRequest.source(jsonString,XContentType.JSON);
        //执行创建索引和保存数据
        IndexResponse index = client.index(indexRequest, EsConfig.COMMON_OPTIONS);

        System.out.println(index);
    }

    /**
     * es中获取数据
     * @throws IOException
     */
    @Test
    public void contextLoads1() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("users");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.query(QueryBuilders.matchQuery("name","张三"));
        System.out.println(sourceBuilder.toString());
        searchRequest.source(sourceBuilder);

        // 2 执行检索
        SearchResponse response = client.search(searchRequest, EsConfig.COMMON_OPTIONS);
        // 3 分析响应结果
        System.out.println(response.toString());


    }




    @Data
    class User {
        private String userName;
        private String gender;
        private Integer age;
    }

}
