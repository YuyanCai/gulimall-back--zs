package com.caq.mall.ware.vo;

import lombok.Data;

@Data
public class LockStockResult {

    private Long skuId;
    private Integer num;
    private boolean lock;
}
