package com.weikey.multifindhub.controller;

import com.weikey.multifindhub.annotation.RequestKeyParam;
import com.weikey.multifindhub.annotation.RequestLock;
import com.weikey.multifindhub.common.BaseResponse;
import com.weikey.multifindhub.common.ResultUtils;
import com.weikey.multifindhub.manager.SearchFacade;
import com.weikey.multifindhub.model.dto.search.SearchPageRequest;
import com.weikey.multifindhub.model.vo.SearchAllVO;
import com.weikey.multifindhub.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    @RequestLock(prefix = "MultiFind:RequestLock", timeout = 2)
    public BaseResponse<SearchAllVO> searchAll(HttpServletRequest request, @RequestBody SearchPageRequest searchPageRequest) {
        return ResultUtils.success(searchFacade.searchAll(searchPageRequest));
    }

    /**
     * 聚合搜索（串行）
     *
     * @param searchPageRequest
     * @return
     */
    @Deprecated
    @PostMapping("/all/sync")
    public BaseResponse<SearchAllVO> searchAllSync(@RequestBody SearchPageRequest searchPageRequest) {
        return ResultUtils.success(searchFacade.searchAllSync(searchPageRequest));
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
