package cdtu.mall.second.controller;

import cdtu.mall.second.pojo.SecComment;
import cdtu.mall.second.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/sec/comment")
    public void addComment(SecComment secComment)
    {
        commentService.addComment(secComment);
        return;
    }
}
