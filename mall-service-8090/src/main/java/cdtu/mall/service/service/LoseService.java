package cdtu.mall.service.service;


import cdtu.mall.service.mapper.LoseMapper;
import cdtu.mall.service.pojo.Lose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LoseService {

    @Autowired
    private LoseMapper loseMapper;

    public void issue(Lose lose)
    {
        String s = UUID.randomUUID().toString();
        lose.setId(s);
        loseMapper.insert(lose);
    }

    public List<Lose> getLoses()
    {
        return loseMapper.selectAll();
    }

    public Lose getOne(String id)
    {
        Lose lose = loseMapper.selectByPrimaryKey(id);
        return lose;
    }
}
