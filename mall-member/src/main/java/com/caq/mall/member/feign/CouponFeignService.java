package com.caq.mall.member.feign;

import com.caq.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-coupon")
public interface CouponFeignService {

    /**
     * 那么这个意思就是说，当我们调用这个接口的方法时，他就会去注册中心中找远程服务mall-coupon所在位置然后再去
     * 调用/coupon/coupon/member/list这个请求对应的方法
     * @return
     */
    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();


}
