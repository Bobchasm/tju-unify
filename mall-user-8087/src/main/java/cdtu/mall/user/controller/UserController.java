package cdtu.mall.user.controller;

import cdtu.mall.user.pojo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import cdtu.mall.user.pojo.User;
import cdtu.mall.user.service.UserService;

import jakarta.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 校验数据是否可用
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data")String data, @PathVariable("type")Integer type)
    {
            Boolean b=this.userService.check(data,type);
            if(b==null)
            {
                return ResponseEntity.notFound().build();
            }

        return ResponseEntity.ok(b);
    }

    /**
     * 发送手机验证码
     * @param phone
     * @return
     */
    @GetMapping("code")
    public ResponseEntity<Void> sendVerifyCode(String phone) {
        System.out.println(phone);
        Boolean boo = this.userService.sendVerifyCode(phone);
        if (boo == null || !boo) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("register")
    public ResponseEntity<Integer> Register(@Valid User user, UserInfo userInfo,@RequestParam("code") String code)
    {

        int i=userService.Register(user,userInfo,code);
        if (i == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if(i==-2)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();//用户名或者手机号已经注册409
        }
            //返回用户uid-->用于人脸识别
        return ResponseEntity.ok(i);
    }

    @GetMapping("query")
    public ResponseEntity<User> queryUser(@RequestParam("username")String username,@RequestParam("password")String password)
    {
        User user=userService.queryUser(username,password);
        if(user==null)
        {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);
    }
}