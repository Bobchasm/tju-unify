package com.tju.elm.user.service;



import com.tju.elm.user.zoo.pojo.dto.AuditPermissionDTO;
import com.tju.elm.user.zoo.pojo.entity.PermissionApplication;
import com.tju.elm.user.zoo.pojo.vo.MerchantApplicationsVO;

import java.util.List;

public interface PermissionApplicationService {
    PermissionApplication applyMerchant();

    PermissionApplication auditApplication(AuditPermissionDTO auditDTO);



    List<MerchantApplicationsVO> getMerchantApplications();

}
