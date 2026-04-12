package com.tju.unify.conv.news.pojo;


import lombok.Data;


@Data
public class Activity {

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
