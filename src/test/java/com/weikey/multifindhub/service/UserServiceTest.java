package com.weikey.multifindhub.service;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.model.vo.UserVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户服务测试
 *
 * @author wei-key
 * 
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void userRegister() {
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "123456";
        try {
            long result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
            userAccount = "yu";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
        } catch (Exception e) {

        }
    }

    @Test
    void searchUsersByPage() {
        Page<UserVO> page = userService.searchUsersByPage("李四", 1, 10);

        List<UserVO> records = page.getRecords();
        long total = page.getTotal();
        long size = page.getSize();
        long current = page.getCurrent();
        System.out.println(records);
        System.out.println(total);
        System.out.println(size);
        System.out.println(current);

    }

}
