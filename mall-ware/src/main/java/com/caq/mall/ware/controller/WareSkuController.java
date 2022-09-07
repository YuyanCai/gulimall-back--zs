package com.caq.mall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.caq.common.to.SkuHasStockVo;
import com.caq.mall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.caq.mall.ware.entity.WareSkuEntity;
import com.caq.mall.ware.service.WareSkuService;
import com.caq.common.utils.PageUtils;
import com.caq.common.utils.R;



/**
 * 商品库存
 *
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-28 11:07:37
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 查询sku是否有库存
     * @return
     */
    @PostMapping(value = "/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds) {

        //skuId stock
        List<SkuHasStockVo> vos = wareSkuService.getSkuHasStock(skuIds);

        return R.ok().setData(vos);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
