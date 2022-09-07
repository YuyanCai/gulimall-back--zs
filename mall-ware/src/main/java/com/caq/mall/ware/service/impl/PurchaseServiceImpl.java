package com.caq.mall.ware.service.impl;

import com.caq.common.constant.WareConstant;
import com.caq.mall.ware.entity.PurchaseDetailEntity;
import com.caq.mall.ware.service.PurchaseDetailService;
import com.caq.mall.ware.service.WareSkuService;
import com.caq.mall.ware.vo.MergeVo;
import com.caq.mall.ware.vo.PurchaseDoneVo;
import com.caq.mall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caq.common.utils.PageUtils;
import com.caq.common.utils.Query;

import com.caq.mall.ware.dao.PurchaseDao;
import com.caq.mall.ware.entity.PurchaseEntity;
import com.caq.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public PageUtils queryUnreceivePage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> {
                w.eq("purchase_id", key).or().eq("sku_id", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean mergePurchase(MergeVo mergeVo) {
        //一、获取Vo中的信息
        //如果指定了采购单，那就获取采购单的id
        Long purchaseId = mergeVo.getPurchaseId();
        //获得采购需求的id
        List<Long> items = mergeVo.getItems();

        //二、过滤采购需求
        //对采购需求id进行过滤，如果采购需求处于新建或者已分配的收集成新的集合
        //这样做的目的是为了进行筛选，如果你选中正在采购的是不会被合并的
        List<Long> collect = items.stream()
                .filter(i -> {
                    //通过采购需求的id获取采购需求实体类
                            PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(i);
                            if (purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode()
                                    || purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode()) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                ).collect(Collectors.toList());

        //三、没有指定采购单逻辑和指定了的逻辑
        if (collect != null && collect.size() > 0) {
            //3.1如果没有指定采购单，那就自动创建一个
            if (purchaseId == null) {
                PurchaseEntity purchaseEntity = new PurchaseEntity();
                //如果是新创建的采购单，创建时间更新时间，状态都是没有默认值的所以这默认值我们自己来赋值
                purchaseEntity.setCreateTime(new Date());
                purchaseEntity.setUpdateTime(new Date());
                //这里设置采购单的状态采用的是枚举类的形式获取
                purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
                this.save(purchaseEntity);
                //获得自动创建的采购单id
                purchaseId = purchaseEntity.getId();
            }

            /** 3.2指定采购单了，逻辑如下
             * 1.采购单id为Vo中获取的指定id
             * 2.设置所有的采购需求对象并收集成对象
             */
            Long finalPurchaseId = purchaseId;
            List<PurchaseDetailEntity> collect1 = collect.stream().map(i -> {
                //获取所有的采购需求对象
                //更新采购需求的状态，一共需要该两个点，一个是采购状态，一个是采购单id。设置采购需求的id是为了区分是哪一个进行了更改
                PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(i);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setId(i);
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            //批量更改采购需求，这里是MP里的接口，可直接传入对象，MP会自动读取里面的ID
            purchaseDetailService.updateBatchById(collect1);

            //四、优化时间更新，为了显示的时间符合我们的样式
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());

            //五、更新采购单
            return this.updateById(purchaseEntity);
        } else {
            return false;
        }
    }


    @Override
    public void received(List<Long> ids) {
        //1.确认当前采购单状态
        List<PurchaseEntity> collect = ids.stream().map(item -> {
            //通过采购单id获取采购单对象
            PurchaseEntity purchaseEntity = this.getById(item);
            return purchaseEntity;
        }).filter(id -> {
            //对采购单对象进行过滤，如果状态为新建或者已分配的留下
            if (id.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    id.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            } else {
                return false;
            }
        }).map(item -> {
            //对上面收集好的在进行过滤，改变采购单状态为已领取（RECEIVE）
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            //对上面收集好的在进行过滤，改变采购单更新时间
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2.批量修改改变采购单状态
        this.updateBatchById(collect);

        //3.改变采购需求中的状态
        if (collect != null && collect.size() > 0) {
            collect.forEach(item -> {
                List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
                List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                    PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                    purchaseDetailEntity.setId(entity.getId());
                    //将采购需求中的状态改为正在采购
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                    return purchaseDetailEntity;
                }).collect(Collectors.toList());
                purchaseDetailService.updateBatchById(detailEntities);
            });
        }
    }

    /**
     * 采购完成一共三地方会发生变化
     *  1.采购单状态
     *  2.库存增加
     *  3.采购需求状态发生变化
     * @param doneVo
     */
    @Override
    public void done(PurchaseDoneVo doneVo) {
        //获取完成的是哪一个采购单
        Long id = doneVo.getId();
        //一、初始化
        Boolean flag = true;
        //获取采购单id集合
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        //收集结果
        List<PurchaseDetailEntity> updates = new ArrayList<>();

        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            } else {
                //二、采购需求状态发生变化
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //由采购单的id获取采购需求对象，有什么用呢？是用来给增加库存时赋值用的
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                //三、库存增加
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            //采购完成，采购需求中的状态也会发生变化，给实体类对象指明id，从而修改对象的状态
            purchaseDetailEntity.setId(item.getItemId());
            //把要修改的采购需求对象放到集合里
            updates.add(purchaseDetailEntity);
        }
        //因为一个采购单里有多个采购需求合并的，所以批量修改采购需求对象
        purchaseDetailService.updateBatchById(updates);

        //四.改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() :
                WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }


}