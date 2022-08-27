package com.caq.mall.product;

//import com.aliyun.oss.*;
//import com.aliyun.oss.model.PutObjectRequest;

import com.caq.mall.product.entity.BrandEntity;
import com.caq.mall.product.service.BrandService;
import com.caq.mall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;


//    @Resource
//    private OSSClient ossClient;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("苹果");
        brandService.save(brandEntity);
    }

    @Test
    void show() {
        List<BrandEntity> list = brandService.list();
        for (BrandEntity brandEntity : list) {
            System.out.println(brandEntity);
        }
    }


//    @Test
//    public void testUploads() throws FileNotFoundException {
//
//
//        // 上传文件流。
//        InputStream inputStream = new FileInputStream("C:\\Users\\Jack\\Desktop\\LeetCode_Sharing.png");
//        ossClient.putObject("pyy-mall", "2022/testPhoto2.png", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//        System.out.println("上传完成...");
//
//    }

}
