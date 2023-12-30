package com.weikey.multifindhub.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.exception.ThrowUtils;
import com.weikey.multifindhub.model.dto.user.UserQueryRequest;
import com.weikey.multifindhub.model.vo.UserVO;
import com.weikey.multifindhub.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserDataSource implements DataSource<UserVO> {
    @Resource
    private UserService userService;

    @Override
    public Page<UserVO> doSearch(String searchText, long pageNum, long pageSize) {
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR);

        UserQueryRequest userQueryRequest =  new UserQueryRequest();
        userQueryRequest.setUserName(searchText);
        userQueryRequest.setCurrent(pageNum);
        userQueryRequest.setPageSize(pageSize);
        return userService.listUserVOByPage(userQueryRequest);
    }
}
