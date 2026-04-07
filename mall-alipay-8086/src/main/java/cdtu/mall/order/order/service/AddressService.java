package cdtu.mall.order.order.service;

import cdtu.mall.common.entity.UserInfo;
import cdtu.mall.order.order.interceptor.LoginInterceptor;
import cdtu.mall.order.order.mapper.AddressMapper;
import cdtu.mall.order.order.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    private static UserInfo user = LoginInterceptor.getLoginUser();

    public List<Address> getAddresses()
    {
        Long userId = user.getId();
        Address address=new Address();
        address.setUserId(userId);
        try
        {
            List<Address> select = addressMapper.select(address);
            return select;
        }
        catch (Exception e)
        {
            System.out.println("查询地址集异常");
            e.printStackTrace();
        }
        return null;
    }

    public void addAddress(Address address)
    {
        // 生成Id
        UUID uuid = UUID.randomUUID();
        String id=uuid.toString();
        // 获取登录用户
        // 初始化数据
        address.setId(id);
        address.setUserId(user.getId());
        // 保存数据
        try
        {
            this.addressMapper.insert(address);
        }
        catch (Exception e)
        {
            System.out.println("地址插入异常");
            e.printStackTrace();
        }
    }

    public void deleteAddress(String id)
    {
        Address address=new Address();
        address.setId(id);
        try
        {
            addressMapper.delete(address);
        }
        catch (Exception e)
        {
            System.out.println("删除地址异常");
            e.printStackTrace();
        }

    }

    public void updataAddress(Address address)
    {
        Address address1=new Address();
        address1.setId(address.getId());
        int i=addressMapper.delete(address1);
        addressMapper.insert(address);
    }


}
