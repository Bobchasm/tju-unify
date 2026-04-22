package com.tju.unify.conv.memo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tju.unify.conv.common.exception.APIException;
import com.tju.unify.conv.memo.mapper.MemoMapper;
import com.tju.unify.conv.memo.mapper.MemoTaskMapper;
import com.tju.unify.conv.memo.pojo.entity.Memo;
import com.tju.unify.conv.memo.pojo.entity.MemoTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoTaskService {

    @Autowired
    private MemoTaskMapper memoTaskMapper;
    @Autowired
    private MemoMapper memoMapper;
    @Autowired
    private MemoCurrentUserService memoCurrentUserService;

    public List<MemoTask> listByMemoId(Long memoId) {
        Long userId = memoCurrentUserService.requireUserId();
        requireMemoOwned(memoId, userId);
        LambdaQueryWrapper<MemoTask> q = new LambdaQueryWrapper<>();
        q.eq(MemoTask::getMemoId, memoId).eq(MemoTask::getIsDeleted, 0)
                .orderByAsc(MemoTask::getSortOrder).orderByAsc(MemoTask::getId);
        return memoTaskMapper.selectList(q);
    }

    public Long add(MemoTask task) {
        Long userId = memoCurrentUserService.requireUserId();
        if (task.getMemoId() == null) {
            throw new APIException("memoId 不能为空");
        }
        requireMemoOwned(task.getMemoId(), userId);
        task.setId(null);
        if (task.getDone() == null) {
            task.setDone(0);
        }
        if (task.getSortOrder() == null) {
            task.setSortOrder(0);
        }
        task.setIsDeleted(0);
        memoTaskMapper.insert(task);
        return task.getId();
    }

    public int update(MemoTask task) {
        if (task.getId() == null) {
            throw new APIException("子任务ID不能为空");
        }
        Long userId = memoCurrentUserService.requireUserId();
        MemoTask db = requireOwnedTask(task.getId(), userId);
        if (task.getTitle() != null) {
            db.setTitle(task.getTitle());
        }
        if (task.getDone() != null) {
            db.setDone(task.getDone());
        }
        if (task.getSortOrder() != null) {
            db.setSortOrder(task.getSortOrder());
        }
        return memoTaskMapper.updateById(db);
    }

    public int setDone(Long taskId, boolean done) {
        Long userId = memoCurrentUserService.requireUserId();
        MemoTask db = requireOwnedTask(taskId, userId);
        db.setDone(done ? 1 : 0);
        return memoTaskMapper.updateById(db);
    }

    public int delete(Long taskId) {
        Long userId = memoCurrentUserService.requireUserId();
        requireOwnedTask(taskId, userId);
        MemoTask patch = new MemoTask();
        patch.setId(taskId);
        patch.setIsDeleted(1);
        return memoTaskMapper.updateById(patch);
    }

    private Memo requireMemoOwned(Long memoId, Long userId) {
        Memo memo = memoMapper.selectById(memoId);
        if (memo == null || (memo.getIsDeleted() != null && memo.getIsDeleted() == 1)) {
            throw new APIException("备忘录不存在");
        }
        if (!userId.equals(memo.getUserId())) {
            throw new APIException("无权操作该备忘录");
        }
        return memo;
    }

    private MemoTask requireOwnedTask(Long taskId, Long userId) {
        MemoTask t = memoTaskMapper.selectById(taskId);
        if (t == null || (t.getIsDeleted() != null && t.getIsDeleted() == 1)) {
            throw new APIException("子任务不存在");
        }
        requireMemoOwned(t.getMemoId(), userId);
        return t;
    }
}
