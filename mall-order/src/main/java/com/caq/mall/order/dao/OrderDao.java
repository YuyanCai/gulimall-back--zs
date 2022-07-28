package com.caq.mall.order.dao;

import com.caq.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-28 11:02:03
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
