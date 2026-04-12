package com.tju.elm.user.service.impl;



import com.tju.elm.user.mapper.PersonMapper;
import com.tju.elm.user.mapper.UserMapper;
import com.tju.elm.user.service.PersonService;
import com.tju.elm.user.zoo.pojo.dto.PersonUpdateDTO;
import com.tju.elm.user.zoo.pojo.dto.UserSearchDTO;
import com.tju.elm.user.zoo.pojo.entity.Person;
import com.tju.elm.user.zoo.pojo.entity.User;
import com.tju.elm.user.zoo.pojo.vo.PersonVO;
import com.tju.elm.user.zoo.utils.SecurityUtils;
import exception.APIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.UserContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PersonServiceImpl implements PersonService {
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public Person getPersonByUserId(Long id) {
        return personMapper.getPersonByUserId(id);
    }

    @Override
    public void addPerson(Person person) {
        personMapper.insert(person);
    }

    @Override
    public Person updatePerson(PersonUpdateDTO updateDTO) {
        // 优先从UserContext获取（网关传递的username header）
        String currentUsername = UserContext.getUsername();
        
        // 如果UserContext中没有，再尝试从SecurityContext获取（本地直接访问的情况）
        if (currentUsername == null || currentUsername.isEmpty()) {
            throw new APIException("当前用户未登录");
        }
        
        log.info("updatePerson - 当前用户名: {}", currentUsername);
        
        User currentUser = userMapper.findByUsernameWithAuthorities(currentUsername);
        if (currentUser == null) {
            throw new APIException("当前用户不存在");
        }
        Long currentUserId = currentUser.getId();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> "ADMIN".equals(auth.getName()));
        if (currentUserId==updateDTO.getId() || isAdmin){
            if (personMapper.getPersonByUserId(updateDTO.getId()) == null) {
                throw new APIException("该用户信息不存在！");
            }
            User user = new User();
            LocalDateTime now = LocalDateTime.now();
            user.setUpdateTime(now);
            user.setUpdater(currentUserId);
            user.setId(currentUserId);

            userMapper.update(user);
            Person p = new Person();
            BeanUtils.copyProperties(updateDTO,p);
            personMapper.updateById(p);
            Person person =personMapper.getPersonByUserId(updateDTO.getId());
            return person;
        }else{
            throw new APIException("当前用户无权限修改该用户信息！");
        }
    }

    @Override
    public List<PersonVO> listPersons(Integer status) {
        List<PersonVO> personVOS = new ArrayList<>();
        List<Person> persons = personMapper.list();
        for (Person person : persons) {
            PersonVO personVO = new PersonVO();
            if(status==0){
                BeanUtils.copyProperties(person, personVO);
                User user = userMapper.findByUserIdWithAuthorities(person.getId());
                BeanUtils.copyProperties(user, personVO);
                personVOS.add(personVO);
            }else if(status==1){
                User user = userMapper.findByUserIdWithAuthorities(person.getId());
                if(user==null){
                    continue;
                }
                if(user.getActivated()){
                    System.out.println("useraaa:{}"+user);
                    BeanUtils.copyProperties(user, personVO);
                    BeanUtils.copyProperties(person, personVO);
                    personVOS.add(personVO);
                }
            }else if(status==2){
                User user = userMapper.findByUserIdWithAuthorities(person.getId());
                if(user==null){
                    continue;
                }
                if(!user.getActivated()){
                    BeanUtils.copyProperties(user, personVO);
                    BeanUtils.copyProperties(person, personVO);
                    personVOS.add(personVO);
                }
            }
        }
        return personVOS;
    }

    @Override
    public List<PersonVO> searchPersons(UserSearchDTO searchDTO) {
        String keyword = searchDTO.getKeyword();
        Integer status = searchDTO.getStatus();

        // 1. 先按关键词搜索（模糊匹配用户名、手机号、邮箱）
        List<PersonVO> personList = personMapper.searchByKeyword(keyword);
        if (personList.isEmpty()) return new ArrayList<>();

        // 2. 关联Person数据+按状态筛选
        List<PersonVO> result = new ArrayList<>();
        for (PersonVO user : personList) {
            // 状态筛选
            if (status == 1 && !user.getActivated()) continue;
            if (status == 2 && user.getActivated()) continue;
            result.add(user);
        }
        return result;
    }
}
