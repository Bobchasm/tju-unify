package cdtu.mall.alipay.service;

import cdtu.mall.alipay.config.AlipayConfig;
import cdtu.mall.alipay.config.PhoneAlipayConfig;
import cdtu.mall.order.order.service.OrderService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class PhonePayService {

    @Autowired
    private OrderService orderService;


    /**
     * 支付回调
     * @param request
     * @return
     * @throws Exception
     */
    public int notify(HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            map.put(name, valueStr);
        }
        //验证签名
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(map, PhoneAlipayConfig.alipay_public_key, PhoneAlipayConfig.charset,PhoneAlipayConfig.sign_type);
        } catch (com.alipay.api.AlipayApiException e) {
            System.out.println("验签错误！");
            return -1;
        }
        //订单号
        String out_trade_no=request.getParameter("out_trade_no");
        //总金额
        String total_amount=request.getParameter("total_amount");
        //商品的标题/交易标题/订单标题/订单关键字等。
        String subject=request.getParameter("subject");
        String product_code=request.getParameter("product_code");
        //付款时间
        String gmt_payment=request.getParameter("gmt_payment");
        //交易创建时间
        String gmt_create=request.getParameter("gmt_create");
        //交易状态
        String tradeStatus = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
        //支护宝交易凭证
        String trade_no=request.getParameter("gmt_create");
        //交易结束时间
        String gmt_close=request.getParameter("gmt_close");
        if (signVerified) {

            System.out.println(tradeStatus);
            //处理自己的业务逻辑
            // TRADE_FINISHED(表示交易已经成功结束，并不能再对该交易做后续操作);
            // TRADE_SUCCESS(表示交易已经成功结束，可以对该交易做后续操作，如：分润、退款等);
            if (tradeStatus.equals("TRADE_FINISHED")) {

                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，
                // 并判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序

            } else if (tradeStatus.equals("TRADE_SUCCESS")) {
                //更新状态
                orderService.updateStatus(Long.valueOf(request.getParameter("out_trade_no")),2);

                return 1;
            }
        }
        this.tradeClose(out_trade_no);
        orderService.updateStatus(Long.valueOf(request.getParameter("out_trade_no")),5);
        return -1;

    }

    public void tradeClose(String orderNo) throws Exception
    {
        AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();
        AlipayTradeCloseModel model =new AlipayTradeCloseModel();
        model.setOutTradeNo(orderNo);
        alipayRequest.setBizModel(model);
        AlipayClient alipayClient = new DefaultAlipayClient(PhoneAlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);
        AlipayTradeCloseResponse alipayResponse= alipayClient.execute(alipayRequest);
        System.out.println(alipayResponse.getBody());
    }


}
