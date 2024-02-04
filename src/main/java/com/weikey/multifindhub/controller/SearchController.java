package com.weikey.multifindhub.controller;

import com.weikey.multifindhub.common.BaseResponse;
import com.weikey.multifindhub.common.ResultUtils;
import com.weikey.multifindhub.manager.SearchFacade;
import com.weikey.multifindhub.model.dto.search.SearchPageRequest;
import com.weikey.multifindhub.model.vo.SearchAllVO;
import com.weikey.multifindhub.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    private SearchFacade searchFacade;

    @Resource
    private PostService postService;

    /**
     * 聚合搜索
     *
     * @param searchPageRequest
     * @return
     */
    @PostMapping("/all")
    public BaseResponse<SearchAllVO> searchAll(@RequestBody SearchPageRequest searchPageRequest) {
        return ResultUtils.success(searchFacade.searchAll(searchPageRequest));
    }

    /**
     * 获取搜索建议
     *
     * @param prefix 搜索词前缀
     * @return
     */
    @GetMapping ("/get/search/suggestion")
    public BaseResponse<List<String>> getSearchSuggestion(String prefix) {
        return ResultUtils.success(postService.getSearchSuggestion(prefix));
    }
}
