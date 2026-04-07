package cdtu.mall.second.service;

import cdtu.mall.common.entity.UserInfo;
import cdtu.mall.second.interceptor.LoginInterceptor;
import cdtu.mall.second.mapper.InsertDao;
import cdtu.mall.second.mapper.SecGoodsMapper;
import cdtu.mall.second.pojo.SecGoods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class IssueService {

    @Autowired
    private SecGoodsMapper goodsMapper;
    @Autowired
    private InsertDao insertDao;

    public Boolean IssueSecGoods(SecGoods secGoods)
    {
        // 获取登录用户
//        UserInfo user = LoginInterceptor.getLoginUser();
//        secGoods.setUserId(user.getId());
        String id = UUID.randomUUID().toString();
        secGoods.setId(id);
        Date date=new Date();
        java.sql.Date date1=new java.sql.Date(date.getTime());
        secGoods.setTime(date1);
        return goodsMapper.insert(secGoods)==1;
//        return insertDao.Insert(secGoods)==1;
    }


}
