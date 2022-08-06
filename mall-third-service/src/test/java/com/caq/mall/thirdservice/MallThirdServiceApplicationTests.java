package com.caq.mall.thirdservice;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class MallThirdServiceApplicationTests {
    @Resource
    OSSClient ossClient;

    @Test
    void contextLoads() throws FileNotFoundException {

        // 上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\Jack\\Desktop\\LeetCode_Sharing.png");
        ossClient.putObject("pyy-mall", "2022/testPhoto3.png", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传完成...");
    }
}

