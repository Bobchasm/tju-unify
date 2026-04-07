package cdtu.mall.second.pojo;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;

@Data
@Table(name = "second")
public class SecGoods {
    @Id
    private String id;
    @Column(name = "mesg")
    private String mesg;
    @Column(name = "qq")
    private String qq;
    @Column(name = "phone")
    private String phone;
    @Column(name = "price")
    private String price;
    @Column(name = "title")
    private String title;
    @Column(name = "img")
    private String img;
    @Column(name = "look")
    private int look;
    @Column(name = "comment_id")
    private String commentId;
    @Column(name ="time")
    private Date time;
    @Column(name = "category")
    private int category;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "name")
    private String name;
}
