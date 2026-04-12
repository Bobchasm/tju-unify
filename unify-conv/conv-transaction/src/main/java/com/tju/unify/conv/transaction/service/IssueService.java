package com.tju.unify.conv.transaction.service;

import cdtu.mall.common.entity.UserInfo;
import com.tju.unify.conv.transaction.mapper.InsertDao;
import com.tju.unify.conv.transaction.mapper.SecGoodsMapper;
import com.tju.unify.conv.transaction.pojo.SecGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class IssueService {

    @Autowired
    private SecGoodsMapper goodsMapper;
    @Autowired
    private InsertDao insertDao;

    public Boolean IssueSecGoods(SecGoods secGoods)
    {
        // 获取登录用户
//        UserInfo user = LoginInterceptor.getLoginUser();
//        secGoods.setUserId(user.getId());
        String id = UUID.randomUUID().toString();
        secGoods.setId(id);
        Date date=new Date();
        java.sql.Date date1=new java.sql.Date(date.getTime());
        secGoods.setTime(date1);
        return goodsMapper.insert(secGoods)==1;
//        return insertDao.Insert(secGoods)==1;
    }


}
