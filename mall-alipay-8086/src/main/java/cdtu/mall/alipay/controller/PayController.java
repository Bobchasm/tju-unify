package cdtu.mall.alipay.controller;


import cdtu.mall.alipay.config.AlipayConfig;
import cdtu.mall.alipay.config.PhoneAlipayConfig;
import cdtu.mall.order.order.service.OrderService;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePayResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName: PayController
 * @Author: 龙灵
 * @Description: ${description}
 * @Date: 2020/3/11 10:09
 * @Version: 1.0
 */
@RestController
public class PayController {

    @Autowired
    private OrderService orderService;

    /**
     * long ling
     * @param out_trade_no 商户订单号，商户网站订单系统中唯一订单号，必填
     * @param total_amount 付款金额，必填
     * @param subject 订单名称，必填
     * @param body  商品描述，可空
     * @return
     * @throws Exception
     */
    @PostMapping("/toPay")
    public String toPay(@RequestParam(name ="out_trade_no")String out_trade_no,@RequestParam(name="total_amount")String total_amount,@RequestParam(name="subject")String subject,@RequestParam(name ="body")String body) throws Exception{
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

        AlipayTradePayRequest request = new AlipayTradePayRequest();

        request.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        try {
            alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                    + "\"total_amount\":\""+ total_amount +"\","
                    + "\"subject\":\""+ subject +"\","
                    + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

            //请求
            String result;
            result = alipayClient.pageExecute(alipayRequest).getBody();
            AlipayTradePayResponse response = alipayClient.execute(request);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
