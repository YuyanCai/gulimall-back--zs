package com.caq.mall.product;

import com.caq.mall.product.entity.BrandEntity;
import com.caq.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("苹果");
        brandService.save(brandEntity);
    }

    @Test
    void show(){
        List<BrandEntity> list = brandService.list();
        for (BrandEntity brandEntity : list) {
            System.out.println(brandEntity);
        }
    }

}
