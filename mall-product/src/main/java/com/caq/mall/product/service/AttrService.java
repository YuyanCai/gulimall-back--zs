package com.caq.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caq.common.utils.PageUtils;
import com.caq.mall.product.entity.AttrEntity;
import com.caq.mall.product.vo.AttrGroupRelationVo;
import com.caq.mall.product.vo.AttrRespVo;
import com.caq.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-27 21:05:30
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo vo);

    PageUtils queryBaseAttrPage(Map<String, Object> params, String type, Integer catelogId);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    List<Long> selectSearchAttrs(List<Long> attrIds);
}

