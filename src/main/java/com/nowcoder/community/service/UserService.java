package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class UserService implements CommunityConstant {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null){
            // 初始化
            user = initCache(id);
        }

        return user;
    }

    public User findUserByUsername(String username){
        return userMapper.selectByName(username);
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null){
            throw new IllegalArgumentException("User参数不能为空");
        }

        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg","该邮箱已存在");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,6));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/id/code
        // 设置了mybatis.configuration.useGeneratedKeys=true,user主键id会自动赋值自增
        String url = domain + contextPath + "/activation/"  + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String process = templateEngine.process("/mail/activation", context);

        mailClient.sendMail(user.getEmail(), "激活邮件", process);

        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        // 查询 激活处理
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg","账号不存在！");
            return map;
        }
        if (user.getStatus() == 0){
            map.put("usernameMsg","账号未激活！");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确！");
            return map;
        }

        // 登陆成功 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * expiredSeconds));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // redis会自动序列化json格式字符串
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket =(LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);

    }

    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeadUrl(int userId, String headerUrl){
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public Map<String, Object> updatePassword(String oldPassword, String newPassword, String newPasswordConfim){
        Map<String, Object> map = new HashMap<>();

        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","旧密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(newPasswordConfim)){
            map.put("newPasswordConfimMsg","确认密码不能为空！");
            return map;
        }

        if(oldPassword.equals(newPassword)){
            map.put("newPasswordMsg","新密码不能和原密码相同！");
            return map;
        }

        if(!newPassword.equals(newPasswordConfim)){
            map.put("newPasswordConfimMsg","确认密码与新密码不同！");
            return map;
        }

        User user = hostHolder.getUsers();
        oldPassword=CommunityUtil.md5(oldPassword+user.getSalt());
        if(!oldPassword.equals(user.getPassword())){
            map.put("oldPasswordMsg","密码错误！");
            return map;
        }


        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword+user.getSalt()));
        clearCache(user.getId());
        return map;
    }

    // 缓存管理用户信息:
    // 1. 优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2. 取不到时初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 36000, TimeUnit.SECONDS);
        return user;
    }

    // 3. 数据变更时清楚缓存
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = userMapper.selectById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                        default:
                            return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
