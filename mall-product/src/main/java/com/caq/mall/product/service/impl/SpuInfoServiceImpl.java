package com.caq.mall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.caq.common.constant.ProductConstant;
import com.caq.common.to.SkuHasStockVo;
import com.caq.common.to.SkuReductionTo;
import com.caq.common.to.SpuBoundTo;
import com.caq.common.to.es.SkuEsModel;
import com.caq.common.utils.R;
import com.caq.mall.product.entity.*;
import com.caq.mall.product.feign.CouponFeignService;
import com.caq.mall.product.feign.SearchFeignService;
import com.caq.mall.product.feign.WareFeignService;
import com.caq.mall.product.service.*;
import com.caq.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caq.common.utils.PageUtils;
import com.caq.common.utils.Query;

import com.caq.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //
        /**
         * 1.??????spu????????????   pms_spu_info
         * ?????????????????????????????????vo??????????????????????????????????????????????????????????????????vo?????????????????????????????????
         */
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.save(infoEntity);

        /**
         * 2.??????spu???????????????  pms_spu_info_desc
         * ????????????????????????????????????????????????service
         * String.join()???????????????????????????????????????","????????????????????????????????????
         */
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        /**
         *  3.??????spu????????????   pms_spu_images
         *  ???vo???????????????????????????
         *  ????????????service???????????????????????????????????????
         *  ??????id???url???????????????????????????
         */
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(), images);


        /**
         * 4.??????spu???????????????  pms_product_attr_value
         * ???vo?????????????????????????????????
         * ?????????????????????????????????????????????????????????
         */
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map((attr) -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());
            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveBatch(collect);

        //5.??????spu???????????????  mall_sms -> sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r0 = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r0.getCode() != 0) {
            log.error("????????????spu??????????????????");
        }
        couponFeignService.saveSpuBounds(spuBoundTo);

        //6.????????????spu???????????????sku?????????
        //6.1sku???????????????;pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                String defalutImg = "";
                for (Images image : item.getImages()) {
                    //????????????????????????????????????
                    if (image.getDefaultImg() == 1) {
                        defalutImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                //??????vo??????????????????
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defalutImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                //6.2sku????????????;pms_sku_images
                //?????????????????????????????????
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imageEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();

                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());

                    return skuImagesEntity;
                }).filter(entity -> {
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(imageEntities);

                //6.3sku???????????????;pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //6.4sku?????????????????????(?????????);
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
//                skuReductionTo.getFullPrice()????????????bigdecimal????????????????????????int???????????????
//                ?????????????????????compareTo??????????????????????????????????????????0,???????????????0????????????????????????????????????
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("????????????spu????????????????????????");
                    }
                }

            });
        }


    }

    @Override
    public void saveBaseInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            //??????sql: status=1 and (id=1 or spu_name like xxx)
            queryWrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * ????????????
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        //1.??????spu?????????sku??????
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);

        //2.??????spu???????????????????????????
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListforSpu(spuId);

        //3.???SkuEsModel.Attrs????????????
        //3.1??????spu????????????????????????????????????id??????
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        //3.2????????????????????????????????????
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);

        //3.3??????????????????set??????
        Set<Long> idSet = searchAttrIds.stream().collect(Collectors.toSet());

        //3.4???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????Attrs??????????????????????????????attrsList
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        //????????????skuId?????????
        List<Long> skuIdList = skuInfoEntities.stream()
                .map(SkuInfoEntity::getSkuId)
                .collect(Collectors.toList());

        //TODO 1?????????????????????????????????????????????????????????
        Map<Long, Boolean> stockMap = null;
        try {
            R skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
            //
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            stockMap = skuHasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        } catch (Exception e) {
            log.error("?????????????????????????????????{}",e);
        }

        //2???????????????sku?????????
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> collect = skuInfoEntities.stream().map(sku -> {
            //?????????????????????
            SkuEsModel esModel = new SkuEsModel();
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            //??????????????????
            if (finalStockMap == null) {
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            //TODO 2??????????????????0
            esModel.setHotScore(0L);

            //TODO 3???????????????????????????????????????
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandId(brandEntity.getBrandId());
            esModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            esModel.setCatalogId(categoryEntity.getCatId());
            esModel.setCatalogName(categoryEntity.getName());

            //??????????????????
            esModel.setAttrs(attrsList);

            BeanUtils.copyProperties(sku,esModel);

            return esModel;
        }).collect(Collectors.toList());

        //TODO 5??????????????????es???????????????mall-search
        R r = searchFeignService.productStatusUp(collect);

        if (r.getCode() == 0) {
            //??????????????????
            //TODO 6???????????????spu?????????
            this.baseMapper.updaSpuStatus(spuId, ProductConstant.ProductStatusEnum.SPU_UP.getCode());
        } else {
            //??????????????????
            //TODO 7?????????????????????????????????:????????????
        }
    }

}