package com.caq.mall.ware.dao;

import com.caq.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-28 11:07:37
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
