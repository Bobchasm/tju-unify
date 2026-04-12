package com.tju.unify.conv.news.controller;

import com.tju.unify.conv.news.pojo.Lose;
import com.tju.unify.conv.news.service.LoseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lose")
public class LoseController {

    @Autowired
    private LoseService loseService;

    @PostMapping("/issue")
    public void issue(@RequestBody Lose lose)
    {
        loseService.issue(lose);
    }

    @GetMapping("/get")
    public List<Lose> getLoses()
    {
        return loseService.getLoses();
    }

    @GetMapping("getOne")
    public Lose getOne(@RequestParam("id") String id)
    {
        return loseService.getOne(id);
    }
}
