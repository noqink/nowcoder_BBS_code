#### 10.29.2020
#### redis实现用户统计
      UV: ip排重统计(每次访问都统计) Hyperloglog
      DAU: userId排重统计(访问一次统计) Bitmap
#### quartz实现热帖
#### 修改了头像上传功能 文件转存qiniu

#### 10.26.2020
#### ES搜索 高亮显示 (jquery版本更换 差点以为消息队列阻塞)
#### 删除LoginTicketRequired注解 加入Spring security进行权限管理

#### 10.22.2020
#### kafka实现评论、关注、点赞通知
#### 显示系统通知

#### 10.21.2020
#### 关注列表 粉丝列表 
#### 优化登陆模块: 
     用redis存储验证码(之前session)
     用redis存储登录凭证
     (之前service层利用mysql存储ticket凭证 业务逻辑仍是service存储ticket controller给cookie存取ticket 拦截器对请求判断ticket状态)
     用redis存储用户信息

#### 10.16.2020 
#### 点赞 关注 redis

#### 10.11.2020 
#### 新增私信 未读数 发送私信 (非netty实现 简单的数据库游戏)
#### 统一日志管理

#### 10.10.2020 
#### 新增显示评论 回帖 回复评论 

#### 10.10.2020 
#### Trie前缀树实现敏感词过滤 发布帖子实现 帖子详情
#### Spring security后续添加 新增@LoginRequired注解实现拦截

#### 10.9.2020 
#### 登录登出功能实现 [登录凭证暂存于mysql的login_ticket表中 后续将引入redis]
#### 添加拦截器 利用ThreadLocal存取user实现页面登陆信息显示 代替session对象实现线程隔离
#### 头像上传功能 后期重构 文件转存服务器
#### 登录状态下修改密码 

#### 10.7.2020 补全注册 kaptcha生成验证码 (thymeleaf真坑)

#### 10.6.2020 偷懒 打无限火力去了 login明天再补吧

#### 10.5.2020 增加主页显示 发送邮件 

技术架构
* Spring Boot
* Spring、Spring MVC、Mybatis
* Redis、Kafka、Elasticsearch
* Spring Security、Spring Actuator
预计两个星期后上线
