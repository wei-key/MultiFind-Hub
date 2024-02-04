package com.weikey.multifindhub.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.exception.ThrowUtils;
import com.weikey.multifindhub.model.dto.post.PostQueryRequest;
import com.weikey.multifindhub.model.entity.Post;
import com.weikey.multifindhub.model.vo.PostVO;
import com.weikey.multifindhub.service.PostService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 文章数据源
 */
@Component
public class PostDataSource  implements DataSource<Post> {
    @Resource
    private PostService postService;

    @Override
    public Page<Post> doSearch(String searchText, long pageNum, long pageSize) {
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR);

        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setSearchText(searchText);
        postQueryRequest.setCurrent(pageNum);
        postQueryRequest.setPageSize(pageSize);

        return postService.searchFromEs(postQueryRequest);
    }
}
