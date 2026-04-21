package com.tju.unify.conv.transaction.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.transaction.pojo.dto.CommentDTO;
import com.tju.unify.conv.transaction.pojo.entity.Comment;
import com.tju.unify.conv.transaction.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/transaction/comment")
@Tag(name="评论接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/list")
    @Operation(summary = "根据帖子ID获取所有评论")
    public HttpResult<List<CommentDTO>> getList(@RequestParam Integer postId) {
        return HttpResult.success(commentService.findByPostId(postId));
    }

    @PostMapping("/add")
    @Operation(summary = "添加评论")
    public HttpResult<Integer> add(@RequestBody Comment comment) {
        return HttpResult.success(commentService.insert(comment));
    }
}
