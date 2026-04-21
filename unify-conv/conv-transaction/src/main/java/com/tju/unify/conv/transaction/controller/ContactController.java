package com.tju.unify.conv.transaction.controller;

import com.tju.unify.conv.common.result.HttpResult;
import com.tju.unify.conv.transaction.pojo.entity.Contact;
import com.tju.unify.conv.transaction.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unify-api/transaction/contact")
@Tag(name="用户联系方式接口")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/list")
    @Operation(summary = "获取用户所有联系方式")
    public HttpResult<List<Contact>> getList() {
        return HttpResult.success(contactService.findAll());
    }

    @PostMapping("/add")
    @Operation(summary = "添加联系方式")
    public HttpResult<Integer> add(@RequestBody Contact contact) {
        return HttpResult.success(contactService.insert(contact));
    }

    @GetMapping("/delete")
    @Operation(summary = "删除联系方式")
    public HttpResult<Integer> delete(@RequestParam Integer id) {
        return HttpResult.success(contactService.deleteById(id));
    }

    @PostMapping("/update")
    @Operation(summary = "修改联系方式")
    public HttpResult<Integer> update(@RequestBody Contact contact) {
        return HttpResult.success(contactService.update(contact));
    }

    @GetMapping("/getByUserId")
    @Operation(summary = "根据用户ID获取联系方式")
    public HttpResult<List<Contact>> getByUserId(@RequestParam Long userId) {
        return HttpResult.success(contactService.findByUserId(userId));
    }
}
