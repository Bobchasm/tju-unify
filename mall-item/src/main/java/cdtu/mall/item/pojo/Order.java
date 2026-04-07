package cdtu.mall.item.pojo;

import lombok.Data;

import jakarta.validation.constraints.Pattern;

@Data
public class Order {

    private String orderId;//订单Id
    private String customerId; //收货人id
    private String customerName; //收货人姓名
//    @Pattern(regexp = "^1(3|4|5|7|8)\\d{9}$",message = "手机号码格式错误")
    private String customerPhone; //收货人电话
}
