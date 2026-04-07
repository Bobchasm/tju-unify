package cdtu.mall.service.service;

import cdtu.mall.service.mapper.RunMapper;
import cdtu.mall.service.pojo.Run;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RunService {

    @Autowired
    private RunMapper runMapper;

    public void issue(Run run)
    {
        String s = UUID.randomUUID().toString();
        run.setId(s);
        runMapper.insert(run);
    }

    public List<Run> getRuns()
    {
        return runMapper.selectAll();
    }

    public Run getOne(String id)
    {
        Run activity = runMapper.selectByPrimaryKey(id);
        return activity;
    }
}
