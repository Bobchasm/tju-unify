package com.tju.unify.conv.news.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tju.unify.conv.news.Task.TjuNewsCrawlerTask;
import com.tju.unify.conv.news.mapper.SchoolNewsMapper;
import com.tju.unify.conv.news.pojo.SchoolNews;
import com.tju.unify.conv.news.pojo.dto.SchoolNewsPageVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SchoolNewsService {

    @Autowired
    private SchoolNewsMapper schoolNewsMapper;

    @Autowired
    @Lazy
    private TjuNewsCrawlerTask tjuNewsCrawlerTask;

    public void save(SchoolNews schoolNews)
    {
        String id = UUID.randomUUID().toString();
        schoolNews.setId(id);
        schoolNewsMapper.insert(schoolNews);
    }

    public boolean existsByUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        LambdaQueryWrapper<SchoolNews> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchoolNews::getUrl, url.trim());
        Long count = schoolNewsMapper.selectCount(queryWrapper);
        return count != null && count > 0;
    }

    public void saveIfNotExists(SchoolNews schoolNews) {
        if (schoolNews == null || StringUtils.isBlank(schoolNews.getUrl())) {
            return;
        }
        if (existsByUrl(schoolNews.getUrl().trim())) {
            return;
        }
        save(schoolNews);
    }

    public List<SchoolNews> getAll()
    {
        List<SchoolNews> schoolNews = schoolNewsMapper.selectList(null);
        return schoolNews;
    }

    public void deleteById(String id)
    {
        schoolNewsMapper.deleteById(id);
    }

    public SchoolNewsPageVO getByFlag(Long flag, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 8;
        }
        if (size > 50) {
            size = 50;
        }

        LambdaQueryWrapper<SchoolNews> queryWrapper = new LambdaQueryWrapper<>();
        if (flag != null && flag != 0) {
            queryWrapper.eq(SchoolNews::getFlag, flag);
        }
        queryWrapper.orderByDesc(SchoolNews::getId);

        Page<SchoolNews> pageResult = schoolNewsMapper.selectPage(
                new Page<>(page, size),
                queryWrapper
        );

        SchoolNewsPageVO vo = new SchoolNewsPageVO();
        vo.setRecords(pageResult.getRecords());
        vo.setTotal(pageResult.getTotal());
        vo.setCurrent(pageResult.getCurrent());
        vo.setSize(pageResult.getSize());
        vo.setPages(pageResult.getPages());
        return vo;
    }

    public SchoolNews getById(String id) {
        SchoolNews schoolNews = schoolNewsMapper.selectById(id);
        return schoolNews;
    }

    public void triggerCrawler() {
        tjuNewsCrawlerTask.triggerCrawler();
    }
}
