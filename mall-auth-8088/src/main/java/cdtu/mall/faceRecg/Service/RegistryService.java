package cdtu.mall.faceRecg.Service;

import cdtu.mall.faceRecg.Face.BaiduAIFace;
import cdtu.mall.faceRecg.SetingModel.Setingmodel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistryService {

    @Autowired
    private BaiduAIFace baiduAIFace;

    public void Registry(Long uid,String imgbase64) throws Exception
    {
        Setingmodel setingmodel=new Setingmodel();
        setingmodel.setImgpath(imgbase64);
        setingmodel.setGroupID("group1");
        setingmodel.setUserID(uid.toString());
        setingmodel.setLiveness_Control("NORMAL");
        setingmodel.setQuality_Control("NORMAL");
        setingmodel.setImage_Type("BASE64");
        baiduAIFace.FaceRegistration(setingmodel);
    }
}
