package com.caq.mall.product.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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