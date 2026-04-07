package cdtu.mall.faceRecg.Controller;


import cdtu.mall.auth.config.JwtProperties;
import cdtu.mall.auth.service.AuthService;
import cdtu.mall.common.utils.CookieUtils;
import cdtu.mall.faceRecg.FeignClient.AuthClient;
import cdtu.mall.faceRecg.Service.LoginService;
import cdtu.mall.faceRecg.Utils.Base64Util;
import cdtu.mall.faceRecg.Utils.FileUtil;
import cdtu.mall.faceRecg.Utils.GsonUtils;
import cdtu.mall.user.pojo.User;
import cdtu.mall.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@Controller
@SessionAttributes(value = "useinf")
public class LoginController {
    @Autowired
    private  LoginService loginService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    @PostMapping("/faceLogin")
    @ResponseBody
    public ResponseEntity<String> searchface(@RequestParam(name = "img")StringBuffer stringBuffer,HttpServletRequest request,HttpServletResponse response) throws IOException {
        String imagebast64=stringBuffer.toString();
        User user=null;
        Map<String, Object> searchface = loginService.searchface(imagebast64);
        if(searchface==null||searchface.get("user_id")==null){
//            String flag="{'meg':'404'}";
//            String s = GsonUtils.toJson(flag);
            return ResponseEntity.ok("fail");
        }
            Long uid =Long.parseLong(searchface.get("user_id").toString());
            String score=searchface.get("score").toString().substring(0,2);
            int i = Integer.parseInt(score);
            if(i>80){
                // 登录校验
                user = userService.queryUserByUid(uid);
                String token = this.authService.faceAuthentication(user.getUsername(),user.getUserId());
                if (StringUtils.isBlank(token)) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
                // 将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改, null, true
                CookieUtils.setCookie(request, response, prop.getCookieName(),token, prop.getCookieMaxAge());
//                return ResponseEntity.ok().build();
                return  ResponseEntity.ok().build();
            }
        return ResponseEntity.ok("fail");

    }
}
