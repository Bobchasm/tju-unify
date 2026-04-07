package cdtu.mall.service.service;


import cdtu.mall.service.mapper.ActivityMapper;
import cdtu.mall.service.pojo.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    public void issue(Activity activity)
    {
        String s = UUID.randomUUID().toString();
        activity.setId(s);
        activityMapper.insert(activity);
    }

    public List<Activity> getActivities()
    {
        List<Activity> activities = activityMapper.selectAll();
        return activities;
    }

    public Activity getOne(String id)
    {
        Activity activity = activityMapper.selectByPrimaryKey(id);
        return activity;
    }

}
