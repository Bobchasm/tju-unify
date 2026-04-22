package com.tju.unify.conv.news.pojo.dto;

import com.tju.unify.conv.news.pojo.SchoolNews;
import lombok.Data;

import java.util.List;

/**
 * 校园新闻分页结果（与 MyBatis-Plus Page 字段对齐，便于前端展示）
 */
@Data
public class SchoolNewsPageVO {

    private List<SchoolNews> records;
    private long total;
    private long current;
    private long size;
    private long pages;
}
