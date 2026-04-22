package com.tju.unify.conv.memo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tju.unify.conv.memo.mapper.MemoMapper;
import com.tju.unify.conv.memo.pojo.dto.MemoAgentSnapshotVO;
import com.tju.unify.conv.memo.pojo.entity.Memo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoAgentService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private MemoMapper memoMapper;
    @Autowired
    private MemoCurrentUserService memoCurrentUserService;
    @Autowired
    private MemoService memoService;

    public MemoAgentSnapshotVO buildSnapshot() {
        Long userId = memoCurrentUserService.requireUserId();
        LocalDateTime since = LocalDateTime.now().minusDays(7);

        MemoAgentSnapshotVO vo = new MemoAgentSnapshotVO();
        Long c1 = memoMapper.countCompletedTasksSince(userId, since);
        Long c2 = memoMapper.countMemosTouchedSince(userId, since);
        vo.setWeekCompletedTasks(c1 != null ? c1 : 0);
        vo.setWeekUpdatedMemos(c2 != null ? c2 : 0);

        List<Memo> due = memoService.listDueReminders(
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().plusDays(7)
        );
        vo.setPendingReminders(due.size());
        Memo next = due.stream()
                .filter(m -> m.getRemindAt() != null)
                .min((a, b) -> a.getRemindAt().compareTo(b.getRemindAt()))
                .orElse(null);
        if (next != null) {
            vo.setUpcomingReminderTitle(next.getTitle());
            vo.setUpcomingReminderAt(next.getRemindAt() != null ? next.getRemindAt().format(FMT) : null);
        }

        LambdaQueryWrapper<Memo> pinnedQ = new LambdaQueryWrapper<>();
        pinnedQ.eq(Memo::getUserId, userId).eq(Memo::getIsDeleted, 0).eq(Memo::getPinned, 1)
                .orderByDesc(Memo::getUpdateTime).last("LIMIT 5");
        vo.setPinnedTitles(memoMapper.selectList(pinnedQ).stream().map(Memo::getTitle).collect(Collectors.toList()));

        LambdaQueryWrapper<Memo> recentQ = new LambdaQueryWrapper<>();
        recentQ.eq(Memo::getUserId, userId).eq(Memo::getIsDeleted, 0)
                .orderByDesc(Memo::getUpdateTime).last("LIMIT 10");
        vo.setRecentMemoTitles(memoMapper.selectList(recentQ).stream().map(Memo::getTitle).collect(Collectors.toList()));

        return vo;
    }
}
