package com.tju.unify.conv.memo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tju.unify.conv.common.exception.APIException;
import com.tju.unify.conv.memo.mapper.MemoCategoryMapper;
import com.tju.unify.conv.memo.mapper.MemoMapper;
import com.tju.unify.conv.memo.pojo.entity.Memo;
import com.tju.unify.conv.memo.pojo.entity.MemoCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemoCategoryService {

    @Autowired
    private MemoCategoryMapper memoCategoryMapper;
    @Autowired
    private MemoMapper memoMapper;
    @Autowired
    private MemoCurrentUserService memoCurrentUserService;

    public List<MemoCategory> list() {
        Long userId = memoCurrentUserService.requireUserId();
        LambdaQueryWrapper<MemoCategory> q = new LambdaQueryWrapper<>();
        q.eq(MemoCategory::getUserId, userId).eq(MemoCategory::getIsDeleted, 0)
                .orderByAsc(MemoCategory::getSortOrder).orderByAsc(MemoCategory::getId);
        return memoCategoryMapper.selectList(q);
    }

    public Long add(MemoCategory category) {
        Long userId = memoCurrentUserService.requireUserId();
        category.setId(null);
        category.setUserId(userId);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        category.setIsDeleted(0);
        memoCategoryMapper.insert(category);
        return category.getId();
    }

    public int update(MemoCategory category) {
        if (category.getId() == null) {
            throw new APIException("分类ID不能为空");
        }
        Long userId = memoCurrentUserService.requireUserId();
        MemoCategory db = requireOwned(category.getId(), userId);
        db.setName(category.getName());
        if (category.getSortOrder() != null) {
            db.setSortOrder(category.getSortOrder());
        }
        return memoCategoryMapper.updateById(db);
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        Long userId = memoCurrentUserService.requireUserId();
        requireOwned(id, userId);
        MemoCategory patch = new MemoCategory();
        patch.setId(id);
        patch.setIsDeleted(1);
        memoCategoryMapper.updateById(patch);
        // categoryId 置空必须用 wrapper.set：MP 默认忽略实体 null 字段，否则会生成无 SET 的非法 UPDATE
        LambdaUpdateWrapper<Memo> uw = new LambdaUpdateWrapper<>();
        uw.set(Memo::getCategoryId, null)
                .eq(Memo::getCategoryId, id)
                .eq(Memo::getUserId, userId)
                .eq(Memo::getIsDeleted, 0);
        memoMapper.update(null, uw);
        return 1;
    }

    private MemoCategory requireOwned(Long id, Long userId) {
        MemoCategory c = memoCategoryMapper.selectById(id);
        if (c == null || c.getIsDeleted() != null && c.getIsDeleted() == 1) {
            throw new APIException("分类不存在");
        }
        if (!userId.equals(c.getUserId())) {
            throw new APIException("无权操作该分类");
        }
        return c;
    }
}
