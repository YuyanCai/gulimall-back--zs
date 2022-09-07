package com.caq.mall.product.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caq.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caq.common.utils.PageUtils;
import com.caq.common.utils.Query;

import com.caq.mall.product.dao.CategoryDao;
import com.caq.mall.product.entity.CategoryEntity;
import com.caq.mall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {

        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);

        List<CategoryEntity> level1Menus = categoryEntityList.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map(menu -> {
            menu.setChildren(getChildrens(menu, categoryEntityList));
            return menu;
        }).sorted((menu1, menu2) -> {
            return menu1.getSort() - menu2.getSort();
        }).collect(Collectors.toList());
        return level1Menus;
    }

    /**
     * 找到cateLogId的完整路径
     *
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId,paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(
                new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        //将数据库的多次查询变为一次,查询所有的分类
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        //1、查出所有分类
        //1、1）查出所有一级分类,从所有的分类中查出一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream()
                //Collectors.toMap意思是将集合转换为map，map的key和value要分别进行映射
                .collect(Collectors.toMap(
                        k -> k.getCatId().toString(),
                        v -> {
            //1、每一个的一级分类,查到这个一级分类的二级分类
                            // 具体是怎么查的呢？
                            // 其实就是拿一级分类集合中的所有元素，去查询id = 一级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        return parentCid;
    }

    //获得父分类的cid(分类id)
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream()
                .filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
        // return this.baseMapper.selectList(
        //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1.收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> allList) {
        List<CategoryEntity> collect = allList.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, allList));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return collect;
    }
    //        2、组装成父子的树形结构
//        获取所有一级分类
//        stream.filter一般适用于list集合,主要作用就是模拟sql查询，从集合中查询想要的数据。
//        filter里面的参数user是指集合里面的每一项

}