package com.weikey.multifindhub.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.exception.ThrowUtils;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.service.PictureService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PictureDataSource  implements DataSource<Picture> {

    @Resource
    private PictureService pictureService;

    @Override
    public Page<Picture> doSearch(String searchText, long pageNum, long pageSize) {
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR);

        return pictureService.searchPicturesByPage(searchText, pageNum, pageSize);
    }
}
