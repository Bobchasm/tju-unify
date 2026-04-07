package cdtu.mall.service.controller;

import cdtu.mall.service.pojo.Lose;
import cdtu.mall.service.service.LoseService;
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
