package cdtu.mall.service.pojo;

import lombok.Data;

import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Table(name = "run")
public class Run {
    @Id
    private String id;
    private String name;
    private String phone;
    private String content;
    private String time;
    private String address;
}
