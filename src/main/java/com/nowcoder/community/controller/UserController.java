package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error", "文件还未上传！");
            return "site/setting";
        }

        // 获取后缀
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件格式不正确！");
            return "site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }

        // 更新当前用户头像路径
        // web访问路径
        // http:localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUsers();
        String headerUrl = domain + contextPath + "/user" + "/header/" + fileName;
        userService.updateHeadUrl(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){

        // 获取服务器存放路径
        String filePath = uploadPath + "/" + fileName;
        // 文件后缀名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应类型
        response.setContentType("image/" + suffix);

        try (
                FileInputStream fis = new FileInputStream(filePath);
                ServletOutputStream os = response.getOutputStream()
        ){
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = fis.read(buffer)) != -1){
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }

    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePasswordByOld(String oldPassword, String newPassword, String newPasswordConfim,
                                      Model model, @CookieValue("ticket") String ticket){

        Map<String,Object> map=userService.updatePassword(oldPassword, newPassword, newPasswordConfim);
        if(map==null||map.isEmpty()){
            userService.logout(ticket);
            return "redirect:/login";
        }
        model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
        model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
        model.addAttribute("newPasswordConfimMsg",map.get("newPasswordConfimMsg"));

        return "site/setting";

    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){

        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }

        model.addAttribute("user", user);
        // 获赞数量
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);

        // 关注
        model.addAttribute("userFolloweeCount", followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        model.addAttribute("userFollowerCount", followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        boolean hasFollow = false;
        // 判断登陆情况
        if (hostHolder.getUsers() != null){
            hasFollow = followService.hasFollow(hostHolder.getUsers().getId(), ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("userFollowerStatus", hasFollow);

        return "site/profile";
    }
}
