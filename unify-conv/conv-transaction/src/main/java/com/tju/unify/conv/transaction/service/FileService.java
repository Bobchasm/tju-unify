package com.tju.unify.conv.transaction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tju.unify.conv.api.client.outer.ElmUserClient;
import com.tju.unify.conv.common.exception.APIException;
import com.tju.unify.conv.common.utils.UserContext;
import com.tju.unify.conv.transaction.mapper.FileMapper;
import com.tju.unify.conv.transaction.mapper.PostMapper;
import com.tju.unify.conv.transaction.pojo.entity.Picture;
import com.tju.unify.conv.transaction.pojo.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class FileService extends ServiceImpl<FileMapper, Picture> {
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private ElmUserClient userClient;

    public List<Picture> getPostPicture(Integer postId) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId);
        return fileMapper.selectList(queryWrapper);
    }


    @Transactional(rollbackFor = Exception.class)
    public Boolean savePicture(List<Picture> pictureList, Integer postId) {
        Post post = postMapper.selectById(postId);
        if(post == null) {
            throw new APIException("该帖子不存在");
        }
        if(!Objects.equals(post.getUserId(), userClient.getUserByName(UserContext.getUsername()).getId())) {
            throw new APIException("无法操作其他用户的帖子");
        }
        fileMapper.insertBatch(pictureList);
        return true;
    }
}
