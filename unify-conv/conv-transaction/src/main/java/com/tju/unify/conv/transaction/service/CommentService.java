package com.tju.unify.conv.transaction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tju.unify.conv.api.client.outer.ElmUserClient;
import com.tju.unify.conv.api.po.Person;
import com.tju.unify.conv.common.utils.UserContext;
import com.tju.unify.conv.transaction.mapper.CommentMapper;
import com.tju.unify.conv.transaction.pojo.dto.CommentDTO;
import com.tju.unify.conv.transaction.pojo.entity.Comment;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ElmUserClient userClient;

    public List<CommentDTO> findByPostId(Integer postId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId)
                .eq("is_deleted", 0)
                .orderByDesc("create_time");
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDTO dto = new CommentDTO();
            BeanUtils.copyProperties(comment, dto);
            
            // 获取用户名
            try {
                Person person = userClient.getPersonById(comment.getUserId());
                if (person != null) {
                    if (person.getUser() != null) {
                        dto.setUsername(person.getUser().getUsername());
                    } else if (person.getFirstName() != null || person.getLastName() != null) {
                        String name = "";
                        if (person.getFirstName() != null) name += person.getFirstName();
                        if (person.getLastName() != null) name += person.getLastName();
                        dto.setUsername(name.isEmpty() ? "用户" + comment.getUserId() : name);
                    } else {
                        dto.setUsername("用户" + comment.getUserId());
                    }
                } else {
                    dto.setUsername("用户" + comment.getUserId());
                }
            } catch (Exception e) {
                dto.setUsername("用户" + comment.getUserId());
            }
            
            commentDTOList.add(dto);
        }
        return commentDTOList;
    }

    public Integer insert(Comment comment) {
        Long currentUserId = userClient.getUserByName(UserContext.getUsername()).getId();
        comment.setUserId(currentUserId);
        return commentMapper.insert(comment);
    }
}
