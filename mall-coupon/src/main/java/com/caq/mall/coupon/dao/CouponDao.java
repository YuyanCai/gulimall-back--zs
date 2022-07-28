package com.caq.mall.coupon.dao;

import com.caq.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-27 23:38:40
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
