package cdtu.mall.faceRecg.Controller;

import cdtu.mall.faceRecg.Service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController {

    @Autowired
    private RegistryService registryService;

    @PostMapping("face_register")
    public String faceRegiter(@RequestParam(name = "img")StringBuffer stringBuffer,@RequestParam(name = "uid") Long uid) throws  Exception
    {
        registryService.Registry(uid,stringBuffer.toString());

        return "sucess";
    }
}
