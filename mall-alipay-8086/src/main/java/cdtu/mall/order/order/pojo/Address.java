package cdtu.mall.order.order.pojo;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Table(name = "address")
public class Address {

    @Id
    private String  id;
    @Column(name = "room")
    private String romm;
    @Column(name = "address")
    private String address;
    @Column(name = "user_id")
    private Long userId;
    private String name;
    private String phone;
}
