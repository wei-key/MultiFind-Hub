package com.weikey.multifindhub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.weikey.multifindhub.model.dto.post.PostQueryRequest;
import com.weikey.multifindhub.model.vo.PostVO;
import com.weikey.multifindhub.model.entity.Post;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子服务
 *
 * @author wei-key
 * 
 */
public interface PostService extends IService<Post> {

    /**
     * 从 ES 查询
     *
     * @param postQueryRequest
     * @return
     */
    Page<Post> searchFromEs(PostQueryRequest postQueryRequest);

    /**
     * 获取搜索建议
     *
     * @param prefix 搜索词前缀
     * @return
     */
    List<String> getSearchSuggestion(String prefix);
}
