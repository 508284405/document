package com.yuwang.shorturlserver.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwang.shorturlserver.adapter.cmd.ShortUrlCmd;
import com.yuwang.shorturlserver.domain.model.UrlEntity;

public interface ShortUrlService {
    /**
     * 创建短链接
     *
     * @param request 短链接创建请求
     * @return 生成的短码
     */
    String createShortUrl(ShortUrlCmd request);

    /**
     * 根据短码获取原始长链接
     *
     * @param shortCode 短码
     * @return 原始长链接，如果不存在或已过期则返回null
     */
    String getLongUrl(String shortCode);

    /**
     * 分页查询短链接列表
     *
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    IPage<UrlEntity> page(Page<UrlEntity> page, LambdaQueryWrapper<UrlEntity> queryWrapper);
}