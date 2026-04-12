package com.tju.unify.conv.transaction.controller;

import com.tju.unify.conv.transaction.pojo.SecComment;
import com.tju.unify.conv.transaction.service.CommentService;
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
