package cdtu.mall.service.pojo;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Table(name = "lose")
public class Lose {

    @Id
    private String id;
    private String name;//物品名称
    private String msg;
    private String time;
    private int flag;
    private String address;
    @Column(name = "img")
    private String img;
    @Column(name = "phone")
    private String phone;
    @Column(name = "user_name")
    private String userName;
}
