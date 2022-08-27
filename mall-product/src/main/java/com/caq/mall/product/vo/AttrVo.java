package com.caq.mall.product.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttrVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long attrId;
    private String attrName;
    private Integer searchType;
    private Integer valueType;
    private String icon;
    private String valueSelect;
    private Integer attrType;
    private Long enable;
    private Long catelogId;
    private Integer showDesc;
    private Long[] catelogPath;

    /**
     * 分组
     */
    private Long attrGroupId;
}

