package com.tju.unify.conv.news.pojo.dto;

import com.tju.unify.conv.news.pojo.SchoolNews;
import lombok.Data;

import java.util.List;


@Data
public class SchoolNewsPageVO {

    private List<SchoolNews> records;
    private long total;
    private long current;
    private long size;
    private long pages;
}
