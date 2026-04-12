package com.tju.elm.user.controller;


import com.tju.elm.api.client.PointClient;
import com.tju.elm.user.mapper.AuthorityMapper;
import com.tju.elm.user.mapper.PersonMapper;
import com.tju.elm.user.mapper.UserMapper;
import com.tju.elm.user.service.PersonService;
import com.tju.elm.user.service.UserModelDetailsService;
import com.tju.elm.user.service.UserService;
import com.tju.elm.user.zoo.pojo.dto.*;
import com.tju.elm.user.zoo.pojo.entity.Authority;
import com.tju.elm.user.zoo.pojo.entity.Person;
import com.tju.elm.user.zoo.pojo.entity.User;
import com.tju.elm.user.zoo.pojo.vo.PersonVO;
import com.tju.elm.user.zoo.pojo.vo.UserVO;
import exception.APIException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import result.HttpResult;
import utils.UserContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "用户管理", description = "提供用户的增删改查操作")
@Slf4j
public class UserRestController {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthorityMapper authorityMapper;
    @Autowired
    private UserModelDetailsService userModelDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PersonService personService;
    @Autowired
    private UserService userService;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private PointClient pointClient;

    @PostMapping("/users")
    @Operation(summary = "新增用户(仅登录账号)", description = "创建一个新的用户")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserVO> createUser(@Valid @RequestBody UserCreateDTO newUser) {
        String username = newUser.getUsername();
        if (username == null || username.trim().isEmpty()) {
            throw new APIException("用户名不能为空");
        }

        // 检查数据库中是否已存在相同用户名
        User existingUser = userService.findByUsername(username);
        if (existingUser != null) {
            throw new APIException("用户名已存在，请更换其他用户名");
        }
        User currentUser = getCurrentUser();
        User user = new User();
        BeanUtils.copyProperties(newUser, user);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setActivated(true);
        user.setCreator(currentUser.getId());
        user.setUpdater(currentUser.getId());
        user.setIsDeleted(false);

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword() != null ? user.getPassword() : "password"));

        // 保存用户
        userService.addUser(user);

        // 分配默认USER角色
        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
            Authority userAuthority = authorityMapper.findByName("USER");
            if (userAuthority != null) {
                userMapper.insertUserAuthority(user.getId(), userAuthority.getName());
            }
        } else {
            // 保存用户指定的角色
            for (Authority authority : user.getAuthorities()) {
                userMapper.insertUserAuthority(user.getId(), authority.getName());
            }
        }

        User user1 = userService.getUserWithAuthorities(user.getUsername());
        UserVO userVO=new UserVO();
        BeanUtils.copyProperties(user1, userVO);
        // 返回包含权限信息的用户对象
        return ResponseEntity.ok(userVO);
    }

    @GetMapping("/user")
    @Operation(summary = "获取当前登录用户", description = "获取当前登录用户的信息")
    public ResponseEntity<UserVO> getActualUser() {
        User currentUser = getCurrentUser();
        UserVO userVO=new UserVO();
        BeanUtils.copyProperties(currentUser, userVO);
        return ResponseEntity.ok(userVO);
    }

    @GetMapping("/user/current")
    @Operation(summary = "获取用户", description = "获取当前登录用户的信息")
    @Cacheable(value = "user_info", key = "#username")
    public HttpResult<User> getUserByName(@RequestParam String username) {
        User user = userService.getUserWithAuthorities(username);
        return HttpResult.success(user);
    }

    @GetMapping("/person")
    @Operation(summary = "获取当前登录用户及其自然人属性", description = "获取当前登录用户及其自然人信息")
    @Cacheable(value = "person_info", key = "T(utils.UserContext).getUsername()")
    public HttpResult<PersonVO> getActualPerson() {
        User currentUser = getCurrentUser();
        PersonVO personVO=new PersonVO();
        BeanUtils.copyProperties(currentUser, personVO);
        Person person = personService.getPersonByUserId(currentUser.getId());
        BeanUtils.copyProperties(person, personVO);
        return HttpResult.success(personVO);
    }

    @GetMapping("/person/id")
    @Operation(summary = "获取用户自然人属性")
    public HttpResult<Person> gainActualPerson(@RequestParam Long userId) {
        return HttpResult.success(personService.getPersonByUserId(userId));
    }

    @GetMapping("/persons")
    @Operation(summary = "获取不同状态的自然人用户", description = "获取不同状态自然人用户，传入0-全部，1-启用，2-禁用")
    public HttpResult<List<PersonVO>> listPersons(Integer status) {
        return HttpResult.success(personService.listPersons(status));
    }

    /**
     * 搜索用户（按关键词+状态筛选）
     */
    @PostMapping("/persons/search")
    @Operation(summary = "搜索用户（用户名/手机号/邮箱）")
    public HttpResult<List<PersonVO>> searchPersons(@RequestBody UserSearchDTO searchDTO) {
        List<PersonVO> searchResult = personService.searchPersons(searchDTO);
        return HttpResult.success(searchResult);
    }


    @PostMapping("/password")
    @Operation(summary = "修改密码", description = "已登录用户可修改自己的密码，管理员可修改任何用户的密码")
    public ResponseEntity<String> updateUserPassword(@Valid @RequestBody LoginDTO loginDto) {
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> "ADMIN".equals(auth.getName()));

        User targetUser = userService.getUserWithAuthorities(loginDto.getUsername());
        if (targetUser == null) {
            throw new APIException("用户不存在");
        }

        // 检查权限：只能修改自己的密码，或者管理员可以修改任何人的密码
        if (currentUser.getUsername().equals(targetUser.getUsername()) || isAdmin) {
            targetUser.setPassword(passwordEncoder.encode(loginDto.getPassword()));
            targetUser.setUpdateTime(LocalDateTime.now());
            targetUser.setUpdater(currentUser.getId());
            userService.updateUser(targetUser);

            // 清除用户缓存
            userModelDetailsService.clearUserCache(targetUser.getUsername());
            return ResponseEntity.ok().body("密码更新成功");
        } else {
            return ResponseEntity.unprocessableEntity().body("权限不足");
        }
    }

    @PostMapping("/persons")
    @Operation(summary = "新增自然人用户", description = "创建一个新的自然人用户")
    @PreAuthorize("hasAuthority('ADMIN')")
    public PersonVO addPerson(@Valid @RequestBody PersonCreateDTO createDTO) {
        String username = createDTO.getUsername();
        if (username == null || username.trim().isEmpty()) {
            throw new APIException("用户名不能为空");
        }

        // 检查数据库中是否已存在相同用户名
        User existingUser = userService.findByUsername(username);
        if (existingUser != null) {
            throw new APIException("用户名已存在，请更换其他用户名");
        }
        User currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setCreator(currentUser.getId());
        user.setCreateTime(now);
        user.setUpdater(currentUser.getId());
        user.setUpdateTime(now);
        user.setIsDeleted(false); // 默认未删除
        user.setActivated(true); // 默认激活（可登录）
        user.setUsername(createDTO.getUsername());
        // 密码：DTO中未传则用默认密码"password"，传了则加密存储
        String rawPassword = createDTO.getPassword() != null ? createDTO.getPassword() : "password";
        user.setPassword(passwordEncoder.encode(rawPassword));
        // 保存User，获取自增ID
        userService.addUser(user);
        Person person = new Person();
        person.setId(user.getId());
        person.setEmail(createDTO.getEmail());
        person.setFirstName(createDTO.getFirstName());
        person.setLastName(createDTO.getLastName());
        person.setGender(createDTO.getGender());
        person.setPhone(createDTO.getPhone());
        person.setPhoto(createDTO.getPhoto());
        person.setUser(user); // 关联User对象（若MyBatis存储时不需要，可只存user_id）
        // 保存Person
        personService.addPerson(person);

        List<String> authorityNames = new ArrayList<>();
        if (createDTO.getAuthorities() != null && !createDTO.getAuthorities().isEmpty()) {

            authorityNames = createDTO.getAuthorities().stream()
                    .map(Authority::getName)
                    .collect(Collectors.toList());
        } else {
            // DTO未指定权限，默认分配USER角色
            authorityNames.add("USER");
        }

        for (String authName : authorityNames) {
            Authority authority = authorityMapper.findByName(authName);
            if (authority == null) {
                throw new RuntimeException("权限不存在：" + authName);
            }
            userMapper.insertUserAuthority(user.getId(), authName);
        }

        User userWithAuthorities = userService.getUserWithAuthorities(user.getUsername());
        if (userWithAuthorities == null) {
            throw new APIException("新增用户后查询失败");
        }

        // 6. 构建响应DTO（对齐接口文档）
        PersonVO responseVO = convertToResponseVO(person, userWithAuthorities);

        // 7. 返回响应（200 OK + 响应体）
        return responseVO;
    }

    @PostMapping("/register")
    @Operation(summary = "新增用户(仅允许顾客注册)", description = "创建一个新的用户")
    @Transactional
    public HttpResult<PersonVO> addUser(@Valid @RequestBody PersonCreateDTO newUser) {
        String username = newUser.getUsername();
        if (username == null || username.trim().isEmpty()) {
            throw new APIException("用户名不能为空");
        }

        // 检查数据库中是否已存在相同用户名
        User existingUser = userService.findByUsername(username);
        if (existingUser != null) {
            throw new APIException("用户名已存在，请更换其他用户名");
        }
        User user = new User();
        BeanUtils.copyProperties(newUser, user);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setActivated(true);
        user.setIsDeleted(false);

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword() != null ? user.getPassword() : "password"));
        Person person = new Person();
        BeanUtils.copyProperties(newUser, person);


        // 保存用户
        userService.addUser(user);
        person.setId(user.getId());
        personService.addPerson(person);

         //分配默认USER角色
        Authority userAuthority = authorityMapper.findByName("USER");
        if (userAuthority != null) {
            userMapper.insertUserAuthority(user.getId(), userAuthority.getName());
        }else{
            throw new APIException("USER权限不存在");
        }
//        if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
//            Authority userAuthority = authorityMapper.findByName("USER");
//            if (userAuthority != null) {
//                userMapper.insertUserAuthority(user.getId(), userAuthority.getName());
//            }
//        } else {
//            // 保存用户指定的角色
//            for (Authority authority : user.getAuthorities()) {
//                userMapper.insertUserAuthority(user.getId(), authority.getName());
//            }
//        }

        // 创建积分账户
        Long pointAccountId = pointClient.createAccount(user.getId()).getData();

        log.info("为新用户 {} 创建积分账户成功，账户ID: {}", user.getId(), pointAccountId);

        User user1 = userService.getUserWithAuthorities(user.getUsername());
        log.info("user1:{}",user1);
        PersonVO personVO = new PersonVO();

        BeanUtils.copyProperties(newUser, personVO);
        BeanUtils.copyProperties(user1, personVO);
        // 返回包含权限信息的用户对象
        return HttpResult.success(personVO);
    }

    @PutMapping("/{username}/status")
    @Operation(summary = "启用/禁用用户")
    @CacheEvict(value = "user_info", key = "#username")
    public HttpResult<String> toggleUserStatus(
            @PathVariable String username,
            @RequestParam Boolean activated) { // true-启用，false-禁用
        userService.toggleUserActivated(username, activated);
        String message = activated ? "用户已启用" : "用户已禁用";
        return HttpResult.success(message);
    }

    @DeleteMapping("/user/{username}")
    @Operation(summary = "删除用户", description = "逻辑删除用户（标记isDeleted=true），仅管理员可操作")
    @PreAuthorize("hasAuthority('ADMIN')")
    @CacheEvict(value = "user_info", key = "#username")
    public HttpResult deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return HttpResult.success();
    }

    @PutMapping("/person/info")
    @Operation(summary = "修改个人信息", description = "支持修改邮箱、姓名、头像（需先调用文件上传接口获取URL）、手机号、性别")
    @CacheEvict(value = "person_info", allEntries = true)
    public HttpResult<Person> updatePersonInfo(@Valid @RequestBody PersonUpdateDTO updateDTO) {
        Person updatedPerson = personService.updatePerson(updateDTO);
        return HttpResult.success(updatedPerson);
    }

    @GetMapping("/personInfo")
    @Operation(summary = "根据id获取自然人属性", description = "根据id获取自然人属性")
    public HttpResult<Person> getPersonInfo(Long id) {
        return HttpResult.success(personMapper.getPersonByUserId(id));
    }

    // 获取当前登录用户
    private User getCurrentUser() {
        String username = UserContext.getUsername();
        return userService.getUserWithAuthorities(username);
    }

    private PersonVO convertToResponseVO(Person person, User user) {
        PersonVO responseVO = new PersonVO();
        // 1. 填充Person相关字段
        responseVO.setId(person.getId());
        responseVO.setFirstName(person.getFirstName());
        responseVO.setLastName(person.getLastName());
        responseVO.setEmail(person.getEmail());
        responseVO.setPhone(person.getPhone());
        responseVO.setGender(person.getGender());
        responseVO.setPhoto(person.getPhoto());

        // 2. 填充User相关字段（含审计字段和权限）
        responseVO.setUsername(user.getUsername());
        responseVO.setCreator(user.getCreator());
        responseVO.setCreateTime(user.getCreateTime());
        responseVO.setUpdater(user.getUpdater());
        responseVO.setUpdateTime(user.getUpdateTime());
        responseVO.setIsDeleted(user.getIsDeleted());

        // 3. 填充权限列表
        if (user.getAuthorities() != null && !user.getAuthorities().isEmpty()) {
            responseVO.setAuthorities(user.getAuthorities());
        }
        return responseVO;
    }


    /**
     * 查所有激活的商家用户信息
     * @return
     */
    @GetMapping("/business/active")
    @Operation(summary = "查所有激活的商家（管理端商铺管理第一页）")
    public List<BusinessInfoDTO> getAllActiveBusinesses() {
        return userService.getAllActiveBusinesses();
    }

    @GetMapping("/user/exist")
    @Operation(summary = "判断用户是否存在")
    public HttpResult<Integer> hasUser(@RequestParam Long userId) {
        return HttpResult.success(userMapper.countUserById(userId));
    }

    @PostMapping("/user/ids")
    @Operation(summary = "获取所有id中的用户列表")
    public HttpResult<List<User>> getUserListByIds(@RequestBody Set<Long> userIds) {
        return HttpResult.success(userService.getUserByIds(userIds));
    }

    @GetMapping("/user/id")
    @Operation(summary = "获取所有id中的用户列表")
    public HttpResult<User> gainUserById(@RequestParam Long userId) {
        return HttpResult.success(userService.getUserById(userId));
    }

    @GetMapping("/user/countUser")
    @Operation(summary = "获取总用户数", description = "获取总用户数")
    public HttpResult<Integer> countUser(){
        return HttpResult.success(userMapper.count());
    }
}
