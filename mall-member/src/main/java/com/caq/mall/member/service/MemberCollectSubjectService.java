package com.caq.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caq.common.utils.PageUtils;
import com.caq.mall.member.entity.MemberCollectSubjectEntity;

import java.util.Map;

/**
 * 会员收藏的专题活动
 *
 * @author xiaocai
 * @email mildcaq@gmail.com
 * @date 2022-07-28 10:32:21
 */
public interface MemberCollectSubjectService extends IService<MemberCollectSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

