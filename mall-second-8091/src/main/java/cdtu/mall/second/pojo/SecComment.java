package cdtu.mall.second.pojo;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;

@Data
@Table(name = "sec_comment")
public class SecComment {

    @Id
    private String id;
    @Column(name = "dian_zan")
    private int dianZan;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "time")
    private Date time;


}
