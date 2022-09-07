package com.caq.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caq.common.to.SkuHasStockVo;
import com.caq.common.utils.PageUtils;
import com.caq.mall.ware.entity.WareSkuEntity;
import com.caq.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-28 11:07:37
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);
}

