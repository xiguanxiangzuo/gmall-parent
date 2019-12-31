package com.xgxz.gmall.vo.ums;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author 习惯向左
 * @create 2019-12-22 14:40
 */
@Data
public class LoginResponseVo {

    private Long memberLevelId;

    private String username;

    private String nickname;

    private String phone;

    private String accessToken;  // 访问令牌
}
