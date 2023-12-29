package com.weikey.multifindhub.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.model.entity.Post;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 聚合搜索结果
 *
 * @author wei-key
 * 
 **/
@Data
public class SearchAllVO implements Serializable {

    private List<UserVO> userList;

    private List<Post> postList;

    private List<Picture> pictureList;

    private static final long serialVersionUID = 1L;
}