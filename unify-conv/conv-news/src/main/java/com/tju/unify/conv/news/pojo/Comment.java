package com.tju.unify.conv.news.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;

@Table(name = "service_comment")
public class Comment {
    @Id
    private String id;
    private String content;
    @Column(name = "time")
    private Date time;
    @Column(name = "dian_zan")
    private int dianZan;
    @Column(name = "user_name")
    private String userName;
}
