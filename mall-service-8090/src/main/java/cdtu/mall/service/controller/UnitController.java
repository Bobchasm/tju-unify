package cdtu.mall.service.controller;

import cdtu.mall.service.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 龙灵
 * 百度机器人服务
 * 提供天气查询，疫情信息查询，笑话讲解，
 */

@RestController
public class UnitController {

    @Autowired
    private UnitService unitService;

    @PostMapping("/unit")
    public String utterance(@RequestParam String query, @RequestParam String botId,@RequestParam String userId)
    {
        String utterance = unitService.utterance(query,botId,userId);
        return utterance;
    }
}
