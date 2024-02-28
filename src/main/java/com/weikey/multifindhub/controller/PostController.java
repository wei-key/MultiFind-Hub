package com.weikey.multifindhub.controller;

import com.weikey.multifindhub.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 帖子接口
 *
 * @author wei-key
 * 
 */
@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

    @Resource
    private PostService postService;

}
