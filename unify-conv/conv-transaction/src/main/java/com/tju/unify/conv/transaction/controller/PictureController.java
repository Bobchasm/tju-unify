package com.tju.unify.conv.transaction.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.transaction.pojo.entity.Picture;
import com.tju.unify.conv.transaction.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/transaction/file")
@Tag(name="文件接口")
public class PictureController {

    @Autowired
    private FileService fileService;

    @GetMapping("/list")
    public HttpResult<List<Picture>> getPictureList(@RequestParam Integer postId) {
        return HttpResult.success(fileService.getPostPicture(postId));
    }

    @PostMapping("/save")
    public HttpResult<Boolean> savePicture(@RequestBody List<Picture> pictureList, @RequestParam Integer postId) {
        return HttpResult.success(fileService.savePicture(pictureList, postId));
    }
}
