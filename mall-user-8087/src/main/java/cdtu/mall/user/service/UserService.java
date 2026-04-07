package cdtu.mall.user.service;

import cdtu.mall.common.utils.NumberUtils;
import cdtu.mall.user.config.SmsProperties;
import cdtu.mall.user.mapper.UserInfoMapper;
import cdtu.mall.user.pojo.UserInfo;
import cdtu.mall.user.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import cdtu.mall.user.listener.SmsListener;
import cdtu.mall.user.mapper.UserMapper;
import cdtu.mall.user.pojo.User;
import cdtu.mall.user.utils.CodecUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private SmsListener smsListener;

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties smsProperties;

//    @Autowired
//    private CodecUtils codecUtils;

    static final String KEY_PREFIX = "user:code:phone:";

    static final Logger logger = LoggerFactory.getLogger(UserService.class);


    public Boolean check(String data,Integer type)
    {
        User u=new User();
        if(type==1)
        {
            u.setUsername(data);
        }
        if(type==2)
        {
            u.setPhone(data);
        }
        else
        {
            return null;
        }
        return this.userMapper.selectCount(u)==0;
    }

    //监听发送短信
//    @RabbitListener(queues = "queue")
//    public void test(Map<String,String> map)
//    {
//            System.out.println("短信发送成功");
//             smsListener.listen(map);
//
//    }

    public Boolean sendVerifyCode(String phone) {
        // 生成验证码
        String code = NumberUtils.generateCode(6);
        try {
            // 发送短信
//            Map<String, String> msg = new HashMap<>();
//            msg.put("phone", phone);
//            msg.put("code", code);
            //amqpTemplate.
            //this.amqpTemplate.convertAndSend("leyou.sms.test.exchange", "sms.verify", msg);
            // 将code存入redis
            System.out.println(code);
            this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);
            System.out.println("结束"+redisTemplate.opsForValue().get(KEY_PREFIX+phone));
//            this.amqpTemplate.convertAndSend("queue",msg);
            if(StringUtils.isNoneBlank(phone)&&StringUtils.isNoneBlank(code))
            {
                try {
                    smsUtils.sendSms(phone,code,smsProperties.getSignName(),smsProperties.getVerifyCodeTemplate());
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            return true;
        } catch (Exception e) {
            logger.error("发送短信失败。phone：{}， code：{}", phone, code);
            return false;
        }
    }

    public int Register(User user, UserInfo userInfo,String code)
    {

        String rediscode=this.redisTemplate.opsForValue().get(KEY_PREFIX+user.getPhone());
        if(!code.equals(rediscode))
        {
            return -1;//输入验证码错误
        }
        //如果验证码正确，就注册用户
        //验证注册用户是否已经存在
        UserInfo temp1=new UserInfo();
        int count;
        temp1.setCard(userInfo.getCard());
//        count = userMapper.selectCount(user);
        count=userInfoMapper.selectCount(userInfo);
        if(count==1)
        {
            return -2;//该用户已经存在
        }
        User temp2=new User();
        temp2.setPhone(user.getPhone());
        count=userMapper.selectCount(temp2);
        if(count==1)
        {
            return -2;//该手机号已经被注册
        }
        user.setUserId(null);
        user.setCreated(new Date());
        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        String pwd = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(pwd);
//        Boolean boo= userMapper.insert(user)==1;
        int i = userMapper.insert(user);
        if (i==1) {
            //插入用户基本信息
            User user1=new User();
            user1.setSalt(user.getSalt());
            User one = userMapper.selectOne(user1);
//            UserInfo userInfo1=new UserInfo();
//            userInfo1.setUserId(one.getUserId());
//            userInfo1.setUserInfoId(null);
            userInfo.setUserInfoId(null);
            userInfo.setUserId(one.getUserId());
            int insert = userInfoMapper.insert(userInfo);
            if(insert==1)
            {
                try {
                    this.redisTemplate.delete(KEY_PREFIX+user.getPhone());
                } catch (Exception e) {
                    logger.error("删除缓存验证码失败，code：{}", code, e);
                }
            }
        }
        return user.getUserId().intValue();
    }

    public User queryUser(String username, String password) {
        User u=new User();
        u.setUsername(username);
        User user = userMapper.selectOne(u);
        if (user==null)
        {
            return user;
        }
        // 校验密码
        if (!user.getPassword().equals(CodecUtils.md5Hex(password, user.getSalt()))) {
            return null;
        }
        // 用户名密码都正确
        return user;
    }

    public User queryUserByUid(Long uid)
    {
        User user = userMapper.selectByPrimaryKey(uid);
        return user;
    }
}