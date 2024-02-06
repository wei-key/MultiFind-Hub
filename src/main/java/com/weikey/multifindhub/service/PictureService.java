package com.weikey.multifindhub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.model.entity.Picture;

import java.io.IOException;
import java.util.List;

/**
 * 图片服务
 */
public interface PictureService {

    Page<Picture> searchPicturesByPage(String searchText, long pageNum, long pageSize);
}
