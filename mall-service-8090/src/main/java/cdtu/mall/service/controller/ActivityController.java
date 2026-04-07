package cdtu.mall.service.controller;

import cdtu.mall.service.pojo.Activity;
import cdtu.mall.service.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping("/get")
    public List<Activity> getActivities()
    {
        List<Activity> activities = activityService.getActivities();
        return activities;
    }

    @PostMapping("/issue")
    public void issue(@RequestBody Activity activity)
    {
        activityService.issue(activity);
    }

    @GetMapping("getOne")
    public Activity getOne(@RequestParam("id") String id)
    {
        return activityService.getOne(id);
    }
}

