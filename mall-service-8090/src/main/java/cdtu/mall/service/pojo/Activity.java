package cdtu.mall.service.pojo;


import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Table(name = "activity")
public class Activity {

    @Id
    private String id;
    private String name;
    private String time;
    private String content;
    private String address;
    @Column(name = "comment_id")
    private String commentId;
    @Column(name = "img")
    private String img;
    @Column(name = "organizer")
    private String organizer;
    private int look;

}
