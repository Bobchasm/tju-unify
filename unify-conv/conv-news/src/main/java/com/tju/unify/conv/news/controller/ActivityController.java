package com.tju.unify.conv.news.controller;

import com.tju.unify.conv.news.pojo.Activity;
import com.tju.unify.conv.news.service.ActivityService;
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

