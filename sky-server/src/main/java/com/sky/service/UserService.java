package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.result.Result;
import com.sky.vo.UserLoginVO;

/**
 * ClassName: UserService
 *
 * @Author Mobai
 * @Create 2023/11/14 18:23
 * @Version 1.0
 * Description:
 */


public interface UserService {

    UserLoginVO login(UserLoginDTO userLoginDTO);

}
