package com.weikey.multifindhub.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.common.BaseResponse;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.common.ResultUtils;
import com.weikey.multifindhub.exception.ThrowUtils;
import com.weikey.multifindhub.model.dto.picture.PictureQueryRequest;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 图片接口
 *
 */
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    /**
     * 分页获取列表（封装类）
     *
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        // 参数校验
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        String searchText = pictureQueryRequest.getSearchText();
        Page<Picture> picturePage = pictureService.searchPicturesByPage(searchText, current, size);
        return ResultUtils.success(picturePage);
    }


}
