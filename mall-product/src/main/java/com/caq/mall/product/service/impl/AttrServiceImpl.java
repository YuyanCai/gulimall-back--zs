package com.caq.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.caq.common.constant.ProductConstant;
import com.caq.mall.product.dao.AttrAttrgroupRelationDao;
import com.caq.mall.product.entity.AttrAttrgroupRelationEntity;
import com.caq.mall.product.entity.AttrGroupEntity;
import com.caq.mall.product.entity.CategoryEntity;
import com.caq.mall.product.service.AttrAttrgroupRelationService;
import com.caq.mall.product.service.AttrGroupService;
import com.caq.mall.product.service.CategoryService;
import com.caq.mall.product.vo.AttrGroupRelationVo;
import com.caq.mall.product.vo.AttrRespVo;
import com.caq.mall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caq.common.utils.PageUtils;
import com.caq.common.utils.Query;

import com.caq.mall.product.dao.AttrDao;
import com.caq.mall.product.entity.AttrEntity;
import com.caq.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;

import javax.annotation.Resource;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private AttrAttrgroupRelationService relationService;

    @Resource
    private AttrAttrgroupRelationDao relation;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        //1.将前端接收数据的对象vo赋值给attrEntity对象，从而更新数据库
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            //2.保存关联关系
            //因为属性组和属性是通过关联关系表连接的
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationService.save(relationEntity);
        }

    }

    //分页查询规格参数
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, String type, Integer catelogId) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            //如果不是一级分类，那么查询的时候加上where catelog_id = ?
            //IgnoreCase忽略大小写
            wrapper.eq("catelog_id", catelogId);
        }

        //多条件模糊查询
        //搜索框里的key不但可以对catelog_id进行模糊查询，对attr_name也模糊查询
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("attr_id", key).or().like("attr_name", key);
        }

        //多条件分页查询
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper);

        PageUtils pageUtils = new PageUtils(page);


        List<AttrEntity> list = page.getRecords();
//        .map()这个方法是对被筛选过后的流进行映射，一般是对属性进行赋值。
        List<AttrRespVo> resultList = list.stream().map(item -> {
            AttrRespVo attrRespvo = new AttrRespVo();
            BeanUtils.copyProperties(item, attrRespvo);
            //设置分类和分组的名字
            if ("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity attrId = relationService.
                        getOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                                .eq("attr_id", item.getAttrId()));
                if (attrId != null && attrId.getAttrGroupId() != null) {
                    //attrgroupRelationEntity.getAttrGroupId()也可以，这里可以直接放进去对象
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrId.getAttrGroupId());
                    attrRespvo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryService.getById(item.getCatelogId());
            if (categoryEntity != null) {
                attrRespvo.setCatelogName(categoryEntity.getName());
            }
            //返回最后的封装结果
            return attrRespvo;
        }).collect(Collectors.toList());

        //返回的结果是一个集合
        pageUtils.setList(resultList);

//        返回分页后的集合对象
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, respVo);

        /**
         * 设置分组信息
         */
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrgroupRelationEntity = relationService.
                    getOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", attrEntity.getAttrId()));
            if (attrgroupRelationEntity != null) {
                respVo.setAttrGroupId(attrgroupRelationEntity.getAttrGroupId());

                Long attrGroupId = attrgroupRelationEntity.getAttrGroupId();
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrGroupId);
                if (attrGroupEntity != null) {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }


        /**
         * 设置分类信息
         */
        Long catelogId = attrEntity.getCatelogId();
        //有了分类的完整路径，接下来就设置分类名字
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        respVo.setCatelogPath(catelogPath);

        //获得分类名字
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        if (categoryEntity != null) {
            respVo.setCatelogName(categoryEntity.getName());
        }

        return respVo;
    }


    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //修改分组关联
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();

            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());

            //统计attr_id的关联属性,如果没有初始分组,则进行添加操作;有则进行修改操作
            Integer count = relation.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {
                relation.update(attrAttrgroupRelationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                relation.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        //分布查询，第一步去关联表中查出所有的组和属性id
        List<AttrAttrgroupRelationEntity> entities = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        //第二收集属性id
        List<Long> attrIds = entities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if (attrIds == null || attrIds.size() == 0) {
            return null;
        }
        List<AttrEntity> list = this.listByIds(attrIds);
        return list;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> entities = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, entity);
            return entity;
        }).collect(Collectors.toList());
        relation.deleteBatchRelation(entities);
    }


    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {

        /**
         *  1.当前分组只能关联自己所属的分类里面的所有属性
         */
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        /**
         *  2 .当前分组只能引用别的分组没有引用的属性
         *  2.1 当前分类下的所有分组
         *  2.2 这些分组关联的属性
         *  2.3 从当前分类的所有属性中移除这些属性
         */

        /**
         * 2.1 当前分类下的所有分组。收集到他们的组id
         * 因为分类和组的关系在pms_group表中，所以用attrGroupService
         */
        List<AttrGroupEntity> group = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        List<Long> collectGroupIds = group.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        /**
         *  2.2 收集到分组的所有属性
         *  （1）拿着上一步收集到的组id到关系表中查找关系表实体类对象，
         *  （2）通过关系表实体类对象获得所有分组下的所有属性id
         */
        List<AttrAttrgroupRelationEntity> groupId = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collectGroupIds));
        List<Long> attrIds = groupId.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        /**
         * 2.3 从当前分类的所有属性中移除这些属性并筛选出基本属性（where attr_type = 1）
         */
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        //如果其他分组也没关联属性，那么就不加这个条件
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }

        /**
         * 分页多条件查询
         * where (`attr_id` = ? or `attr_name` like ?)
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }


        /**
         * page方法需要两个参数
         * 1.IPage对象（通过工具类Query获取并通过.getPage(params)封装页面传来分页参数）
         * 2.wrapper（自己生成）
         */
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }


}