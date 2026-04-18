package com.tju.unify.conv.transaction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tju.unify.conv.api.client.outer.ElmUserClient;
import com.tju.unify.conv.common.exception.APIException;
import com.tju.unify.conv.common.utils.UserContext;
import com.tju.unify.conv.transaction.mapper.ContactMapper;
import com.tju.unify.conv.transaction.pojo.entity.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ContactService {
    @Autowired
    private ContactMapper contactMapper;
    @Autowired
    private ElmUserClient userClient;

    public List<Contact> findAll() {
        QueryWrapper<Contact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userClient.getUserByName(UserContext.getUsername()).getId());
        return contactMapper.selectList(queryWrapper);
    }

    public Integer deleteById(Integer id) {
        Contact contact = contactMapper.selectById(id);
        if(contact != null && !Objects.equals(contact.getUserId(), userClient.getUserByName(UserContext.getUsername()).getId())) {
            throw new APIException("无法操作其他用户的信息");
        }
        return contactMapper.deleteById(id);
    }

    public Integer insert(Contact contact) {
        contact.setUserId(userClient.getUserByName(UserContext.getUsername()).getId());
        return contactMapper.insert(contact);
    }

    public Integer update(Contact contact) {
        if(contact != null && !Objects.equals(contact.getUserId(), userClient.getUserByName(UserContext.getUsername()).getId())) {
            throw new APIException("无法操作其他用户的信息");
        }
        return contactMapper.updateById(contact);
    }
}
