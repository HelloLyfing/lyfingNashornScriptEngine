package com.lyfing.demo.bo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author dongxu.lu
 * @date 2022/8/10
 */
@Getter
@Setter
public class UserInfoBO {

    private Long userId;

    private String nickname;

    private String avatar;

    /**
     * 1: 初级
     * 2: 高级
     * 3: VIP
     */
    private Integer level;

}
