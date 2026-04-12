package com.tju.unify.conv.transaction.service;

import cdtu.mall.common.entity.UserInfo;
import com.tju.unify.conv.transaction.interceptor.LoginInterceptor;
import com.tju.unify.conv.transaction.mapper.SecCommentMapper;
import com.tju.unify.conv.transaction.pojo.SecComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class CommentService {

    @Autowired
    private SecCommentMapper secCommentMapper;

    /**
     * 二手商品进行评论
     * @param secComment
     */
    public void addComment(SecComment secComment)
    {
        Date date=new Date();
        java.sql.Date date1=new java.sql.Date(date.getTime());
        secComment.setTime(date1);
        String id = UUID.randomUUID().toString();
        secComment.setId(id);
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();
        secComment.setUserName(user.getUsername());
        System.out.println(user.getUsername());
    }
}
