package com.tju.unify.conv.transaction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tju.unify.conv.api.client.outer.ElmUserClient;
import com.tju.unify.conv.api.po.User;
import com.tju.unify.conv.common.exception.APIException;
import com.tju.unify.conv.common.result.ResultCodeEnum;
import com.tju.unify.conv.common.utils.UserContext;
import com.tju.unify.conv.transaction.mapper.PostMapper;
import com.tju.unify.conv.transaction.pojo.entity.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class PostService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private ElmUserClient userClient;

    private final Integer pageSize  = 10;

    public Integer createPost(Post post) {
        post.setUserId(userClient.getUserByName(UserContext.getUsername()).getId());
        postMapper.insertPost(post);
        return post.getId();
    }

    public Integer updatePost(Post post) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", post.getId());
        return postMapper.updateById(post);
    }

    public Integer deletePost(Integer id) {
        Post post = postMapper.selectById(id);
        if(!Objects.equals(post.getUserId(), userClient.getUserByName(UserContext.getUsername()).getId())) {
            throw new APIException("无法操作其他用户的帖子");
        }
        return postMapper.deleteById(id);
    }

    /**
     *
     * @param userId 0-当前用户 null-不分用户
     * @param pageNo
     * @param status
     * @return
     */
    public List<Post> getPostList(Integer userId, Integer pageNo, Integer status) {
        Page<Post> page=new Page<>(pageNo==null?1:pageNo, pageSize);
        IPage<Post> res = postMapper.getPosts(page, userId==null ? null : userClient.getUserByName(UserContext.getUsername()).getId(), status);
        return res.getRecords();
    }

    public List<Post> searchPosts(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return postMapper.selectList(null);
        }
        return postMapper.searchPostsByKeyword(keyword.trim());
    }

    public Post getPostDetail(Integer id) {
        return postMapper.selectById(id);
    }
}
