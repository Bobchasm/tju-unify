package cdtu.mall.second.controller;

import cdtu.mall.second.pojo.SecGoods;
import cdtu.mall.second.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class IssueController {

    @Autowired
    private IssueService issueService;

    @PostMapping("sec/issue")
    public ResponseEntity<String> IssueSecgoods(SecGoods secGoods )
    {

        Boolean flag = issueService.IssueSecGoods(secGoods);
        System.out.println("11111111"+secGoods.getPhone());
        if(!flag)
        {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
