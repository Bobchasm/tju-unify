package cdtu.mall.alipay.config;

public class PhoneAlipayConfig {
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016102100728779";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDOYjhppQOBmBWq4lacSnqWRKng/mlIMtGqSOVHnRjj295OFcX9p1a5pjV7aHToOnbvBlIO+LX/UU6LExAhw2UuY1AfuFifDI5nem0DAFerVMMdfwqrRD8AtYqMOORCYQYm4EVA1JNyViJGZsrGHPALL7x5hgsD52R/0aFpkyNYbiXUjc0c/KtyKxwwIWg14fv3EAcIzTYQ4t0mvd8UaH/BEgwp/+Xi7MO4rc4PPNiXNUJXyAarB0950xyLhbREsvC/DDVXbasYxuX0UVYLkKeb/NBARKWTkeB+66SZ1J/dJklXx8kq5IfxNZJZFaFDWRNX0tcyj+QwEohsl13g4ZkdAgMBAAECggEAE+ypk5FpuD4tgVTelbIfCZjRabZGBcFaOn1VHUfOWlkkqhcyCR35omyoZySbfLwANW0/yNA8Cc0YrxaFDS1m/EZ8OsO6UoiMyvKnyY1j6VeCirt0FTq+dlwQZx8q8jmZtXEbXnnnTjeAkZlk5CFxDzNpl7XYJGVXb3uWgLHCzMuRBORg7kl8bvkc2Fte/P+9RU78pAR1DSEym8J+jFY9cP/clsxRn7rrbLNxmXiyVKgZbrvr+jOsBAqc5XRVTC0icOuBWMUUDcDwouxGFKtk7WtwNelwlhrfkPrlzUTBBifLDOSN2DNCU/3BoqO/rnel+17Lt+6P2q3786iWtdsUQQKBgQD03yE2tWu5MhrHCFfwb5gbG1pjIXyzuoxC0mSvri+aiLrS82odlkNMD0PLpyFXqLZZ8G+Qa39XIzOu2reIQbxcmJ2p9n+UY3NCvXHNGrCjDQFLJGcZ0wXsbum6TkqTZ9qiZfVbUa1PhTj3X7ctP6xG6fo7L0r6S5HWYrDibGrh0QKBgQDXw1EUss7IZPuqBlEKWgZL9Tr2hpuFoaCJYLm2v/l18PRPzfSREUhvOH3IbcKTxbvMffL/AModp824DmIwrzsYdQKfb7jEf0+TW+Yro5MJLb0aURYho9dD+t4n86JlCDf6wMxzkEJoKAIK1YuDqgoVqeZnOLUfQGW4+NgafbzpjQKBgBKGZt7arsGd9sSj7vMznhA+64GgYOy/6iiL/i4EQXmxamDXJWoDmifA5+IaTL1y+Aip1ebZGxMeiKRry4K1Zmii6xqWmWRCec/MwTVBFafgLf7fTCV5/3K5mVkM3ztzmei7SeqUrUXc0Eh2p0b3EDN1NAcSNtvkp63OXY9joFphAoGBALXx9dCvjCJ+zZ4uH27MRZ0OGf/DuSgyCK9wSotqTdIAM/WMJt2iJmPrzl4S+M8mIlyqJRyWrNnfJGI+dty5F+ti2KhcTQIenZsVFv7bvGU0FdA4aqJJiIDB707fa6UpDhO9O2uETguJGVfLhg+sdz6XfDy1HD9TTbRy28FTINgNAoGBAJzT/DlbEaMhzhC+s+U+YH/bGTZyHoozXpO7L2QgbXgjAs3WvPV6MjZ3cJWwmvlT8h1wd77mTYHCiGvpjyQYPsW6iH9YHdGymO4pYPpV2LUoFYyL2OyJEc97f4ZlNcWOExxwjugQ5y1SghkGsVD8My7ZJ1ZUqjULCFbORkJiLqHN";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAldCtf19Hw5qymaF2hTwWgighXPmbMyrDE8xI88kZNYtj0G6nouIOol/AZCPz5s9k3SW7QCO5ePKHNwn57tzMOC8UAJ6P4yiuTvquThmerMYrW8kq5L3ePmWRtC468QZ+11fBJ6K7VMhGwTeR+hnQoByKQqvtsgxezhGzTMa503PN/FSk8YVTGT/EBMMq4wOiCoS3bmvt6yXtZBTfueShCjbRZ/tLR+2dhwaPeKn5kCHVvevpFTH3ei9/jreE4x3A7nTHeI9kLmp32/UM/VGRpscVrNyufF8rUYFm8F8CnYb55TXQr8aSMrfmB+UJ2xLSU2dZ/REmv85ZPr9qg+osXQIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://3p0998832l.wicp.vip/notify";
//    alipay.trade.wap.pay-JAVA-UTF-8/notify_url.jsp

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    /*public static String return_url = "http://localhost/alipay.trade.page.pay-JAVA-UTF-8/return_url.jsp";*/
    public static String return_url = "http://localhost:8001/#/orderDetail";

    // 签名方式，注意这里，如果步骤设置的是RSA则用RSA
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "C:\\";



}
