package com.tju.unify.conv.news.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tju.unify.conv.news.mapper.SchoolNewsMapper;
import com.tju.unify.conv.news.pojo.SchoolNews;
import io.swagger.v3.oas.models.examples.Example;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SchoolNewsService {

    @Autowired
    private SchoolNewsMapper schoolNewsMapper;

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
//        Example ex = new Example(SchoolNews.class);
//        ex.createCriteria().andEqualTo("url", url.trim());
//        return schoolNewsMapper.selectCountByExample(ex) > 0;
        // TODO
        return true;
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

    public List<SchoolNews> getByFlag(Long flag, int page) {
        // 构建查询条件
        LambdaQueryWrapper<SchoolNews> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchoolNews::getFlag, flag)
                .ne(SchoolNews::getContent, "")
                .orderByDesc(SchoolNews::getId);

        // 分页查询
        Page<SchoolNews> pageResult = schoolNewsMapper.selectPage(
                new Page<>(page, 10),
                queryWrapper
        );

        // 返回当前页的数据列表
        return pageResult.getRecords();
    }

    public SchoolNews getById(String id) {
        SchoolNews schoolNews = schoolNewsMapper.selectById(id);
        return schoolNews;
    }
}
