package cdtu.mall.user.listener;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import cdtu.mall.user.config.SmsProperties;
import cdtu.mall.user.utils.SmsUtils;

import java.util.Map;

@Component
//@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties smsProperties;

//    @RabbitListener(bindings=@QueueBinding(
//            value = @Queue(value = "leyou.sms.verify.queue",durable = "true"),
//            exchange = @Exchange(value = "leyou.sms.test.exchange",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
//            key = {"sms.verify"}
//    ))
    public void listen(Map<String,String> msg)
    {
        if(CollectionUtils.isEmpty(msg))
        {
            return;
        }
        String phone=msg.get("phone");
        String code=msg.get("code");
        System.out.println("监听到了");
        if(StringUtils.isNoneBlank(phone)&&StringUtils.isNoneBlank(code))
        {
            try {
                smsUtils.sendSms(phone,code,smsProperties.getSignName(),smsProperties.getVerifyCodeTemplate());
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }
}
