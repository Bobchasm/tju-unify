package com.tju.unify.conv.transaction.service;

import com.tju.unify.conv.transaction.mapper.SecGoodsMapper;
import com.tju.unify.conv.transaction.pojo.SecGoods;
import com.tju.unify.conv.transaction.utils.ComparatorDate;
import com.tju.unify.conv.transaction.utils.ComparatorHot;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private SecGoodsMapper secGoodsMapper;

    /**
     * category=-1
     * 分页查询所有二手商品
     * flag=0--按照最新发布查询，flag=1--按照热门排序
     * category=0,1,2,3
     * 按分类分页查询
     * @param category
     * @param flag
     * @return
     */
    public List<SecGoods> findAll(int category,int flag,int page)
    {
        Example example=new Example(SecGoods.class);
        Example.Criteria criteria = example.createCriteria();
        ComparatorDate d=new ComparatorDate();
        ComparatorHot h=new ComparatorHot();
        List<SecGoods> items=new ArrayList<>();
        if(category==-1)
        {
            PageHelper.startPage(page,30);
            items = this.secGoodsMapper.selectByExample(example);
            //按最新上架分页查询所有
            if(flag==0)
            {
                Collections.sort(items,d);
            }
            //按最新热门上架
            if(flag==1)
            {
                Collections.sort(items,h);
            }
        }
        else {
            criteria.andEqualTo("category",category);
            PageHelper.startPage(page,10);
            items = this.secGoodsMapper.selectByExample(example);
            //按最新上架分页查询所有
            if(flag==0)
            {
                Collections.sort(items,d);
            }
            //按最新热门上架
            if(flag==1)
            {
                Collections.sort(items,h);
            }
        }
        return items;
    }

    /**
     * 点击浏览量更新
     * @param id
     */
    public void click(String id)
    {
        SecGoods secGoods = secGoodsMapper.selectByPrimaryKey(id);
        secGoodsMapper.deleteByPrimaryKey(id);
        int look = secGoods.getLook();
        look++;
        secGoods.setLook(look);
        secGoodsMapper.insert(secGoods);
    }

    /**
     * 根据商品id查询商品
    @param id
     * @return
     */
    public SecGoods getOne(String id)
    {
        return secGoodsMapper.selectByPrimaryKey(id);
    }


}
