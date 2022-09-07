package com.caq.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.caq.common.utils.PageUtils;
import com.caq.common.utils.R;
import com.caq.mall.product.entity.ProductAttrValueEntity;
import com.caq.mall.product.service.ProductAttrValueService;
import com.caq.mall.product.vo.AttrRespVo;
import com.caq.mall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.caq.mall.product.service.AttrService;


/**
 * 商品属性
 *
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-27 21:05:30
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;



    /**
     *  获取spu规格
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId") Long spuId){

        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrListforspu(spuId);

        return R.ok().put("data",entities);
    }

    /**
     * 显示规格参数
     * 一个方法当两个来用！
     * 规格参数和销售参数的区别在于type的值，type为 1是规格参数，type为0是销售参数
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("attrType") String type,
                          @PathVariable("catelogId") Integer catelogId) {
        PageUtils page = attrService.queryBaseAttrPage(params, type, catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 数据回显
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrRespVo respVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo vo) {
        attrService.saveAttr(vo);
        return R.ok();
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);

        return R.ok();
    }

    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){

        productAttrValueService.updateSpuAttr(spuId,entities);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
