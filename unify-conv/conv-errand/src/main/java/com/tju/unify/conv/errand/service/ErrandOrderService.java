package com.tju.unify.conv.errand.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tju.unify.conv.common.exception.APIException;
import com.tju.unify.conv.errand.mapper.ErrandOrderMapper;
import com.tju.unify.conv.errand.pojo.dto.ErrandPublishRequest;
import com.tju.unify.conv.errand.pojo.entity.ErrandOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ErrandOrderService {

    public static final int STATUS_OPEN = 0;
    public static final int STATUS_ACCEPTED = 1;
    public static final int STATUS_DONE = 2;
    public static final int STATUS_CANCELLED = 3;

    @Autowired
    private ErrandOrderMapper errandOrderMapper;
    @Autowired
    private ErrandCurrentUserService currentUserService;

    public Long publish(ErrandPublishRequest req) {
        Long uid = currentUserService.requireUserId();
        if (!StringUtils.hasText(req.getStartPoint()) || !StringUtils.hasText(req.getEndPoint())) {
            throw new APIException("请填写起点和终点");
        }
        if (!StringUtils.hasText(req.getContactInfo())) {
            throw new APIException("请填写联系方式");
        }
        if (req.getExpectTime() == null) {
            throw new APIException("请填写期望时间");
        }
        BigDecimal tip = req.getTipAmount() == null ? BigDecimal.ZERO : req.getTipAmount();
        if (tip.compareTo(BigDecimal.ZERO) < 0) {
            throw new APIException("小费不能为负数");
        }

        ErrandOrder o = new ErrandOrder();
        o.setPublisherId(uid);
        o.setStartPoint(req.getStartPoint().trim());
        o.setEndPoint(req.getEndPoint().trim());
        o.setTipAmount(tip);
        o.setExpectTime(req.getExpectTime());
        o.setContactInfo(req.getContactInfo().trim());
        o.setRemark(StringUtils.hasText(req.getRemark()) ? req.getRemark().trim() : null);
        o.setStatus(STATUS_OPEN);
        o.setIsDeleted(0);
        o.setCreateTime(LocalDateTime.now());
        errandOrderMapper.insert(o);
        return o.getId();
    }

    public List<ErrandOrder> listOpen() {
        LambdaQueryWrapper<ErrandOrder> w = new LambdaQueryWrapper<>();
        w.eq(ErrandOrder::getStatus, STATUS_OPEN)
                .eq(ErrandOrder::getIsDeleted, 0)
                .orderByAsc(ErrandOrder::getExpectTime);
        return errandOrderMapper.selectList(w);
    }

    public List<ErrandOrder> listMine() {
        Long uid = currentUserService.requireUserId();
        LambdaQueryWrapper<ErrandOrder> w = new LambdaQueryWrapper<>();
        w.eq(ErrandOrder::getIsDeleted, 0)
                .and(q -> q.eq(ErrandOrder::getPublisherId, uid).or().eq(ErrandOrder::getRunnerId, uid))
                .orderByDesc(ErrandOrder::getCreateTime);
        return errandOrderMapper.selectList(w);
    }

    public ErrandOrder getById(Long id) {
        ErrandOrder o = errandOrderMapper.selectById(id);
        if (o == null || (o.getIsDeleted() != null && o.getIsDeleted() == 1)) {
            throw new APIException("订单不存在");
        }
        return o;
    }

    public void accept(Long id) {
        Long uid = currentUserService.requireUserId();
        ErrandOrder o = getById(id);
        if (o.getStatus() == null || o.getStatus() != STATUS_OPEN) {
            throw new APIException("当前状态不可接单");
        }
        if (uid.equals(o.getPublisherId())) {
            throw new APIException("不能接自己发布的单");
        }
        o.setStatus(STATUS_ACCEPTED);
        o.setRunnerId(uid);
        o.setAcceptTime(LocalDateTime.now());
        o.setUpdateTime(LocalDateTime.now());
        errandOrderMapper.updateById(o);
    }

    public void complete(Long id) {
        Long uid = currentUserService.requireUserId();
        ErrandOrder o = getById(id);
        if (o.getRunnerId() == null || !o.getRunnerId().equals(uid)) {
            throw new APIException("仅接单者可确认完成");
        }
        if (o.getStatus() == null || o.getStatus() != STATUS_ACCEPTED) {
            throw new APIException("当前状态不可完成");
        }
        o.setStatus(STATUS_DONE);
        o.setUpdateTime(LocalDateTime.now());
        errandOrderMapper.updateById(o);
    }

    public void cancel(Long id) {
        Long uid = currentUserService.requireUserId();
        ErrandOrder o = getById(id);
        if (!o.getPublisherId().equals(uid)) {
            throw new APIException("仅发起人可取消");
        }
        if (o.getStatus() == null || o.getStatus() != STATUS_OPEN) {
            throw new APIException("仅待接单可取消");
        }
        o.setStatus(STATUS_CANCELLED);
        o.setUpdateTime(LocalDateTime.now());
        errandOrderMapper.updateById(o);
    }
}
