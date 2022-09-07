package com.caq.mall.ware.feign;

import com.caq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-gateway")
public interface ProductFeignService {

    /**
     *  feignService有两种写法
     *      1.给远程调用的微服务发请求
         *      @FeignClient("mall-product") 指定微服务
         *      /product/skuinfo/info/{skuId}
     *      2.给网关发请求
     *          @FeignClient("mall-gateway")
     *          /api/product/skuinfo/info/{skuId}
     *
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/api/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

}
