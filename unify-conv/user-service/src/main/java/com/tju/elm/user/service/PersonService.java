package com.tju.elm.user.service;


import com.tju.elm.user.zoo.pojo.dto.PersonUpdateDTO;
import com.tju.elm.user.zoo.pojo.dto.UserSearchDTO;
import com.tju.elm.user.zoo.pojo.entity.Person;
import com.tju.elm.user.zoo.pojo.vo.PersonVO;
import jakarta.validation.Valid;

import java.util.List;

public interface PersonService {
    Person getPersonByUserId(Long id);
    void addPerson(Person person);

    Person updatePerson(@Valid PersonUpdateDTO updateDTO);

    List<PersonVO> listPersons(Integer status);

    List<PersonVO> searchPersons(UserSearchDTO searchDTO);
}
