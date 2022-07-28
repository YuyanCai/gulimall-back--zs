package com.caq.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caq.common.utils.PageUtils;
import com.caq.mall.product.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu属性值
 *
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-27 21:05:30
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

