package cn.edu.zjut.back.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {
    private String token;
    
    @JsonProperty("user")
    private UserVO user;
    
    // 为前端兼容性提供 userInfo 别名
    @JsonProperty("userInfo")
    public UserVO getUserInfo() {
        return user;
    }

    public LoginVO(String token, UserVO user) {
        this.token = token;
        this.user = user;
    }
}
