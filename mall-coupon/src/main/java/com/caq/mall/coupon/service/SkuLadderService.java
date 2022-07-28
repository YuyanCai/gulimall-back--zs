package com.caq.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caq.common.utils.PageUtils;
import com.caq.mall.coupon.entity.SkuLadderEntity;

import java.util.Map;

/**
 * 商品阶梯价格
 *
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-27 23:38:40
 */
public interface SkuLadderService extends IService<SkuLadderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

