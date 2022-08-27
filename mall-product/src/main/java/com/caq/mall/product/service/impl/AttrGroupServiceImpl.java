package com.caq.mall.product.service.impl;

import com.caq.mall.product.entity.AttrEntity;
import com.caq.mall.product.service.AttrService;
import com.caq.mall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caq.common.utils.PageUtils;
import com.caq.common.utils.Query;

import com.caq.mall.product.dao.AttrGroupDao;
import com.caq.mall.product.entity.AttrGroupEntity;
import com.caq.mall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        //多条件查询
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            //如果是默认的是查全部的一级分类
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 获取分类下的所有分组及属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        /** 1.获取分类下的所有分组,封装成集合
         *  分类和组的关系在pms_group表中，所以（where catelog_id = ？）即可查出分类对应的组
         *  由于这是mp，它会得出所有的这种关系，并把结果封装成集合
         */
        List<AttrGroupEntity> list = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        /** 2.获得分组下的属性
         *  要第三张关联表,直接调用关联表的service即查询分组对应的属性id
         *  获得属性id在去调用属性表的service即可查询属性名
         *  以上两步前面已经写好逻辑了直接调用即可attrService.getRelationAttr（groupId）
         */
        List<AttrGroupWithAttrsVo> collect = list.stream().map((item) -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
            if (attrs != null) {
                attrGroupWithAttrsVo.setAttrs(attrs);
            }
            return attrGroupWithAttrsVo;
        }).filter((attrvo) -> {
            return attrvo.getAttrs() != null && attrvo.getAttrs().size() > 0;
        }).collect(Collectors.toList());
        return collect;
    }
}