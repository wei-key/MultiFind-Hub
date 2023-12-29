package com.weikey.multifindhub.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.common.BaseResponse;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.common.ResultUtils;
import com.weikey.multifindhub.exception.BusinessException;
import com.weikey.multifindhub.exception.ThrowUtils;
import com.weikey.multifindhub.model.dto.search.SearchPageRequest;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.model.entity.Post;
import com.weikey.multifindhub.model.vo.SearchAllVO;
import com.weikey.multifindhub.model.vo.UserVO;
import com.weikey.multifindhub.service.PictureService;
import com.weikey.multifindhub.service.PostService;
import com.weikey.multifindhub.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * 聚合搜索接口
 *
 * @author wei-key
 *
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private PictureService pictureService;

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;


//    /**
//     * 聚合搜索（串行写法）
//     *
//     * @param searchPageRequest
//     * @return
//     */
//    @PostMapping("/all")
//    public BaseResponse<SearchAllVO> searchAll(@RequestBody SearchPageRequest searchPageRequest) {
//        // 参数校验
//        ThrowUtils.throwIf(searchPageRequest == null, ErrorCode.PARAMS_ERROR);
//        String searchText = searchPageRequest.getSearchText();
//        long current = searchPageRequest.getCurrent();
//        long pageSize = searchPageRequest.getPageSize();
//
//        Page<UserVO> userVOPage = userService.searchUsersByPage(searchText, current, pageSize);
//        Page<Post> postPage = postService.searchPostsByPage(searchText, current, pageSize);
//        Page<Picture> picturePage = pictureService.searchPicturesByPage(searchText, current, pageSize);
//
//        SearchAllVO searchAllVO = new SearchAllVO();
//        searchAllVO.setUserList(userVOPage.getRecords());
//        searchAllVO.setPostList(postPage.getRecords());
//        searchAllVO.setPictureList(picturePage.getRecords());
//        return ResultUtils.success(searchAllVO);
//    }

    /**
     * 聚合搜索
     *
     * @param searchPageRequest
     * @return
     */
    @PostMapping("/all")
    public BaseResponse<SearchAllVO> searchAll(@RequestBody SearchPageRequest searchPageRequest) {
        // 参数校验
        ThrowUtils.throwIf(searchPageRequest == null, ErrorCode.PARAMS_ERROR);
        String searchText = searchPageRequest.getSearchText();
        long current = searchPageRequest.getCurrent();
        long pageSize = searchPageRequest.getPageSize();
        if (StrUtil.isBlank(searchText) || current <= 0 || current > 200 || pageSize <= 0 || pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 多线程并发
        CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> userService.searchUsersByPage(searchText, current, pageSize));

        CompletableFuture<Page<Post>> postTask = CompletableFuture.supplyAsync(() -> postService.searchPostsByPage(searchText, current, pageSize));

        CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> pictureService.searchPicturesByPage(searchText, current, pageSize));

        CompletableFuture.allOf(userTask, postTask, pictureTask).join();

        SearchAllVO searchAllVO = null;
        try {
            searchAllVO = new SearchAllVO();
            searchAllVO.setUserList(userTask.get().getRecords());
            searchAllVO.setPostList(postTask.get().getRecords());
            searchAllVO.setPictureList(pictureTask.get().getRecords());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "搜索出现异常");
        }

        return ResultUtils.success(searchAllVO);
    }
}
