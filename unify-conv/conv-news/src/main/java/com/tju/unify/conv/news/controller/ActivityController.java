package com.tju.unify.conv.news.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.news.pojo.Activity;
import com.tju.unify.conv.news.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/news/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping("/get")
    public HttpResult<List<Activity>> getActivities() {
        List<Activity> activities = activityService.getActivities();
        return HttpResult.success(activities);
    }

    @PostMapping("/issue")
    public HttpResult<Boolean> issue(@RequestBody Activity activity) {
        return HttpResult.success(activityService.issue(activity));
    }

    @GetMapping("/getOne")
    public HttpResult<Activity> getOne(@RequestParam("id") String id) {
        return HttpResult.success(activityService.getOne(id));
    }
}

