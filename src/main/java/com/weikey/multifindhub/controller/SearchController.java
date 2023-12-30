package com.weikey.multifindhub.controller;

import com.weikey.multifindhub.common.BaseResponse;
import com.weikey.multifindhub.common.ResultUtils;
import com.weikey.multifindhub.manager.SearchFacade;
import com.weikey.multifindhub.model.dto.search.SearchPageRequest;
import com.weikey.multifindhub.model.vo.SearchAllVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
