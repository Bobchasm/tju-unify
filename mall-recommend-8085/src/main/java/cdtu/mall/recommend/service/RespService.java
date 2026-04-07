package cdtu.mall.recommend.service;

import cdtu.mall.item.response.RespGood;

import java.util.List;

public interface RespService {

    public List<RespGood> findAll(int pageNum,int pageSize,String key);
}
