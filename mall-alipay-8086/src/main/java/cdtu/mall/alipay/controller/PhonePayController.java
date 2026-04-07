package cdtu.mall.alipay.controller;

import cdtu.mall.alipay.config.AlipayConfig;
import cdtu.mall.alipay.config.PhoneAlipayConfig;
import cdtu.mall.alipay.service.PhonePayService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
public class PhonePayController {

    @Autowired
    private PhonePayService phonePayService;



    @PostMapping("/toAliPay")
    public String phoneToPay(@RequestParam(name ="out_trade_no")String out_trade_no, @RequestParam(name="total_amount")String total_amount, @RequestParam(name="subject")String subject, @RequestParam(name ="body")String body) throws Exception{
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(PhoneAlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);


        // 订单模型
        String productCode = "CDTU_ALIPAY";
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(out_trade_no);
        model.setSubject(subject);
        model.setTotalAmount(total_amount);
//        model.setBody(body);
        model.setProductCode(productCode);

        AlipayTradeWapPayRequest wapPayRequest = new AlipayTradeWapPayRequest();
        wapPayRequest.setReturnUrl(PhoneAlipayConfig.return_url);
        wapPayRequest.setNotifyUrl(PhoneAlipayConfig.notify_url);
        wapPayRequest.setBizModel(model);
        // 调用SDK生成表单, 并直接将完整的表单html输出到页面
        String form = alipayClient.pageExecute(wapPayRequest).getBody();
        return form;
    }

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) throws Exception {

        int notify = phonePayService.notify(request);
        if(notify==1)
        {
            return "finish";
        }
        return "fail";
    }


}
