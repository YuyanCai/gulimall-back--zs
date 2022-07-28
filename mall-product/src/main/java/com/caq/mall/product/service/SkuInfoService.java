package com.caq.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caq.common.utils.PageUtils;
import com.caq.mall.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku信息
 *
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-27 21:05:30
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

