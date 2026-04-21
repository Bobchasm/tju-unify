package com.tju.unify.conv.transaction.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.transaction.pojo.entity.Post;
import com.tju.unify.conv.transaction.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/transaction/post")
@Tag(name="帖子接口")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/list/user")
    @Operation(summary = "获取用户所有帖子,可选状态")
    HttpResult<List<Post>> getPostList(@RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer status) {
        return HttpResult.success(postService.getPostList(0, pageNo, status));
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有帖子", description = "分页10页,不传页数默认第1页")
    HttpResult<List<Post>> getPostList(@RequestParam(required = false) Integer pageNo) {
        return HttpResult.success(postService.getPostList(null, pageNo, 0));
    }

    @GetMapping("/delete")
    @Operation(summary = "删除帖子")
    HttpResult<Integer> deletePost(@RequestParam Integer id) {
        return HttpResult.success(postService.deletePost(id));
    }

    @PostMapping("/add")
    @Operation(summary = "发布帖子")
    HttpResult<Integer> addPost(@RequestBody Post post) {
        return HttpResult.success(postService.createPost(post));
    }

    @PostMapping("/update")
    @Operation(summary = "更新帖子")
    HttpResult<Integer> updatePost(@RequestBody Post post) {
        return HttpResult.success(postService.updatePost(post));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索帖子")
    HttpResult<List<Post>> searchPost(@RequestParam String keyword) {
        return HttpResult.success(postService.searchPosts(keyword));
    }

    @GetMapping("/detail")
    @Operation(summary = "获取帖子详情")
    HttpResult<Post> getPostDetail(@RequestParam Integer id) {
        return HttpResult.success(postService.getPostDetail(id));
    }

}
