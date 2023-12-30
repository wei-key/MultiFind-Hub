package com.weikey.multifindhub.service;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.model.dto.post.PostQueryRequest;
import com.weikey.multifindhub.model.entity.Post;
import javax.annotation.Resource;

import com.weikey.multifindhub.model.vo.PostVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子服务测试
 *
 * @author wei-key
 * 
 */
@SpringBootTest
class PostServiceTest {

    @Resource
    private PostService postService;

    @Test
    void searchFromEs() {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setUserId(1L);
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        Assertions.assertNotNull(postPage);
    }


}