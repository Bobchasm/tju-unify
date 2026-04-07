package cdtu.mall.upload.controller;



import cdtu.mall.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("image")
    public ResponseEntity<String> UploadImage(@RequestParam("file")MultipartFile file,@RequestParam("dir")String dir)
    {
        String url=uploadService.upload(file,dir);
        if(StringUtils.isEmpty(url))
        {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(url);
    }
}
