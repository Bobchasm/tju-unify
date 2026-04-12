package com.tju.unify.conv.api.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String phone;
    private String photo;
    private User user;
}