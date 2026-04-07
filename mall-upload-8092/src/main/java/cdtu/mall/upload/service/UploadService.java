package cdtu.mall.upload.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {

    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg", "image/gif","image/png");
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    /**
     * 上传图片
     * @param file
     * @return
     */
    public String upload(MultipartFile file,String dir)
    {
        String contype=file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String sub=originalFilename.substring(originalFilename.lastIndexOf("."),originalFilename.length());
        if(!CONTENT_TYPES.contains(contype))
        {
            LOGGER.info("文件类型出错"+originalFilename);
            return null;
        }
        try
        {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if(bufferedImage==null)
            {
                LOGGER.info("文件内容不合法"+originalFilename);
                return null;
            }
            //上传服务器
            originalFilename= UUID.randomUUID().toString();
            file.transferTo(new File("D:\\images\\"+dir+"\\"+originalFilename+sub));
            //返回URL地址
            System.out.println("http://image.cdtull.com/" +dir+ "/"+originalFilename+sub);
            return "http://image.cdtull.com/" +dir+ "/"+originalFilename+sub;

        }catch (Exception e)
        {
            LOGGER.info("服务器内部出错!");
            e.printStackTrace();
        }
        return null;
    }
}
