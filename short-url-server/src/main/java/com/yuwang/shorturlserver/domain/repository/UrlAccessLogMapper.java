package com.yuwang.shorturlserver.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwang.shorturlserver.domain.model.UrlAccessLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UrlAccessLogMapper extends BaseMapper<UrlAccessLog> {
}