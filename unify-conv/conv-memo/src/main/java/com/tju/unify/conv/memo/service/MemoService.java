package com.tju.unify.conv.memo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tju.unify.conv.common.exception.APIException;
import com.tju.unify.conv.memo.mapper.MemoCategoryMapper;
import com.tju.unify.conv.memo.mapper.MemoMapper;
import com.tju.unify.conv.memo.mapper.MemoTaskMapper;
import com.tju.unify.conv.memo.pojo.dto.MemoCreateRequest;
import com.tju.unify.conv.memo.pojo.dto.MemoDetailVO;
import com.tju.unify.conv.memo.pojo.dto.MemoRemindUpdateRequest;
import com.tju.unify.conv.memo.pojo.entity.Memo;
import com.tju.unify.conv.memo.pojo.entity.MemoCategory;
import com.tju.unify.conv.memo.pojo.entity.MemoTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemoService {

    @Autowired
    private MemoMapper memoMapper;
    @Autowired
    private MemoTaskMapper memoTaskMapper;
    @Autowired
    private MemoCategoryMapper memoCategoryMapper;
    @Autowired
    private MemoCurrentUserService memoCurrentUserService;

    public List<Memo> list(Long categoryId, Integer pinned) {
        Long userId = memoCurrentUserService.requireUserId();
        LambdaQueryWrapper<Memo> q = new LambdaQueryWrapper<>();
        q.eq(Memo::getUserId, userId).eq(Memo::getIsDeleted, 0);
        if (categoryId != null) {
            q.eq(Memo::getCategoryId, categoryId);
        }
        if (pinned != null) {
            q.eq(Memo::getPinned, pinned);
        }
        q.orderByDesc(Memo::getPinned).orderByAsc(Memo::getSortOrder).orderByDesc(Memo::getUpdateTime).orderByDesc(Memo::getId);
        return memoMapper.selectList(q);
    }

    public MemoDetailVO detail(Long memoId) {
        Long userId = memoCurrentUserService.requireUserId();
        Memo memo = requireOwnedMemo(memoId, userId);
        LambdaQueryWrapper<MemoTask> tq = new LambdaQueryWrapper<>();
        tq.eq(MemoTask::getMemoId, memoId).eq(MemoTask::getIsDeleted, 0)
                .orderByAsc(MemoTask::getSortOrder).orderByAsc(MemoTask::getId);
        List<MemoTask> tasks = memoTaskMapper.selectList(tq);
        MemoDetailVO vo = new MemoDetailVO();
        vo.setMemo(memo);
        vo.setTasks(tasks);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(MemoCreateRequest req) {
        Long userId = memoCurrentUserService.requireUserId();
        if (!StringUtils.hasText(req.getTitle())) {
            throw new APIException("标题不能为空");
        }
        validateCategory(req.getCategoryId(), userId);
        Memo memo = new Memo();
        memo.setUserId(userId);
        memo.setCategoryId(req.getCategoryId());
        memo.setTitle(req.getTitle().trim());
        memo.setContent(req.getContent());
        memo.setPinned(Boolean.TRUE.equals(req.getPinned()) ? 1 : 0);
        memo.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        memo.setRemindAt(req.getRemindAt());
        memo.setRemindDone(req.getRemindAt() == null ? 0 : 0);
        memo.setIsDeleted(0);
        memoMapper.insert(memo);
        if (req.getTasks() != null) {
            int i = 0;
            for (MemoCreateRequest.MemoTaskInit init : req.getTasks()) {
                if (init == null || !StringUtils.hasText(init.getTitle())) {
                    continue;
                }
                MemoTask task = new MemoTask();
                task.setMemoId(memo.getId());
                task.setTitle(init.getTitle().trim());
                task.setDone(Boolean.TRUE.equals(init.getDone()) ? 1 : 0);
                task.setSortOrder(init.getSortOrder() != null ? init.getSortOrder() : i++);
                task.setIsDeleted(0);
                memoTaskMapper.insert(task);
            }
        }
        return memo.getId();
    }

    /**
     * 部分更新：未传的字段保持原值。分类置空约定：传 categoryId = -1 表示取消分类。
     */
    public int update(Memo memo) {
        if (memo.getId() == null) {
            throw new APIException("备忘录ID不能为空");
        }
        Long userId = memoCurrentUserService.requireUserId();
        Memo db = requireOwnedMemo(memo.getId(), userId);
        if (memo.getCategoryId() != null && memo.getCategoryId() == -1L) {
            db.setCategoryId(null);
        } else if (memo.getCategoryId() != null) {
            validateCategory(memo.getCategoryId(), userId);
            db.setCategoryId(memo.getCategoryId());
        }
        if (StringUtils.hasText(memo.getTitle())) {
            db.setTitle(memo.getTitle().trim());
        }
        if (memo.getContent() != null) {
            db.setContent(memo.getContent());
        }
        if (memo.getPinned() != null) {
            db.setPinned(memo.getPinned());
        }
        if (memo.getSortOrder() != null) {
            db.setSortOrder(memo.getSortOrder());
        }
        if (memo.getRemindAt() != null) {
            db.setRemindAt(memo.getRemindAt());
        }
        if (memo.getRemindDone() != null) {
            db.setRemindDone(memo.getRemindDone());
        }
        return memoMapper.updateById(db);
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(Long memoId) {
        Long userId = memoCurrentUserService.requireUserId();
        requireOwnedMemo(memoId, userId);
        Memo patch = new Memo();
        patch.setId(memoId);
        patch.setIsDeleted(1);
        memoMapper.updateById(patch);
        LambdaUpdateWrapper<MemoTask> uw = new LambdaUpdateWrapper<>();
        uw.eq(MemoTask::getMemoId, memoId).eq(MemoTask::getIsDeleted, 0);
        MemoTask taskPatch = new MemoTask();
        taskPatch.setIsDeleted(1);
        memoTaskMapper.update(taskPatch, uw);
        return 1;
    }

    public int setPinned(Long memoId, boolean pinned) {
        Long userId = memoCurrentUserService.requireUserId();
        Memo db = requireOwnedMemo(memoId, userId);
        db.setPinned(pinned ? 1 : 0);
        return memoMapper.updateById(db);
    }

    public int updateRemind(Long memoId, MemoRemindUpdateRequest req) {
        Long userId = memoCurrentUserService.requireUserId();
        Memo db = requireOwnedMemo(memoId, userId);
        if (Boolean.TRUE.equals(req.getClearRemind())) {
            db.setRemindAt(null);
            db.setRemindDone(0);
        } else {
            if (req.getRemindAt() != null) {
                db.setRemindAt(req.getRemindAt());
            }
            if (req.getRemindDone() != null) {
                db.setRemindDone(req.getRemindDone());
            }
        }
        return memoMapper.updateById(db);
    }

    public List<Memo> listDueReminders(LocalDateTime from, LocalDateTime until) {
        Long userId = memoCurrentUserService.requireUserId();
        LocalDateTime f = from != null ? from : LocalDateTime.now().minusDays(7);
        LocalDateTime u = until != null ? until : LocalDateTime.now().plusDays(1);
        LambdaQueryWrapper<Memo> q = new LambdaQueryWrapper<>();
        q.eq(Memo::getUserId, userId).eq(Memo::getIsDeleted, 0)
                .eq(Memo::getRemindDone, 0)
                .isNotNull(Memo::getRemindAt)
                .ge(Memo::getRemindAt, f)
                .le(Memo::getRemindAt, u)
                .orderByAsc(Memo::getRemindAt);
        return memoMapper.selectList(q);
    }

    private void validateCategory(Long categoryId, Long userId) {
        if (categoryId == null) {
            return;
        }
        MemoCategory c = memoCategoryMapper.selectById(categoryId);
        if (c == null || (c.getIsDeleted() != null && c.getIsDeleted() == 1)) {
            throw new APIException("分类不存在");
        }
        if (!userId.equals(c.getUserId())) {
            throw new APIException("分类不属于当前用户");
        }
    }

    private Memo requireOwnedMemo(Long memoId, Long userId) {
        Memo memo = memoMapper.selectById(memoId);
        if (memo == null || (memo.getIsDeleted() != null && memo.getIsDeleted() == 1)) {
            throw new APIException("备忘录不存在");
        }
        if (!userId.equals(memo.getUserId())) {
            throw new APIException("无权操作该备忘录");
        }
        return memo;
    }
}
