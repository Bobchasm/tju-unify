package cdtu.mall.service.pojo;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Table(name = "school_news")
public class SchoolNews {

    @Id
    private String id;

    private String content;

    private String time;

    private String title;

    private String unit;

    @Column(name = "flag")
    private Long flag;

    private String origin;

    private String url;
}
