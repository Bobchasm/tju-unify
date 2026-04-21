package com.tju.unify.conv.transaction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tju.unify.conv.api.client.outer.ElmUserClient;
import com.tju.unify.conv.common.exception.APIException;
import com.tju.unify.conv.common.utils.UserContext;
import com.tju.unify.conv.transaction.mapper.TransactionRequestMapper;
import com.tju.unify.conv.transaction.pojo.entity.TransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TransactionRequestService {
    @Autowired
    private TransactionRequestMapper transactionRequestMapper;
    @Autowired
    private ElmUserClient userClient;

    public List<TransactionRequest> findByPostId(Integer postId) {
        QueryWrapper<TransactionRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId)
                .eq("is_deleted", 0)
                .orderByDesc("create_time");
        return transactionRequestMapper.selectList(queryWrapper);
    }

    public Integer insert(TransactionRequest request) {
        Long currentUserId = userClient.getUserByName(UserContext.getUsername()).getId();
        request.setBuyerId(currentUserId);
        return transactionRequestMapper.insert(request);
    }

    public Integer updateStatus(Integer id, Integer status) {
        Long currentUserId = userClient.getUserByName(UserContext.getUsername()).getId();
        TransactionRequest request = transactionRequestMapper.selectById(id);
        
        if (request == null) {
            throw new APIException("请求不存在");
        }
        
        // 只有卖家可以同意或拒绝，只有买家可以取消
        if (status == 1 || status == 2) {
            if (!Objects.equals(request.getSellerId(), currentUserId)) {
                throw new APIException("只有卖家可以操作");
            }
        } else if (status == 3) {
            if (!Objects.equals(request.getBuyerId(), currentUserId)) {
                throw new APIException("只有买家可以取消");
            }
        }
        
        request.setStatus(status);
        return transactionRequestMapper.updateById(request);
    }
}
