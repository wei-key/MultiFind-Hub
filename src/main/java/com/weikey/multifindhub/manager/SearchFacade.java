package com.weikey.multifindhub.manager;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.datasource.*;
import com.weikey.multifindhub.exception.BusinessException;
import com.weikey.multifindhub.exception.ThrowUtils;
import com.weikey.multifindhub.model.dto.search.SearchPageRequest;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.model.enums.SearchDataTypeEnum;
import com.weikey.multifindhub.model.vo.PostVO;
import com.weikey.multifindhub.model.vo.SearchAllVO;
import com.weikey.multifindhub.model.vo.UserVO;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * 搜索门面
 */
@Component
public class SearchFacade {

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    /**
     * 聚合搜索
     *
     * @param searchPageRequest
     * @return
     */
    public SearchAllVO searchAll(SearchPageRequest searchPageRequest) {
        // 参数校验
        ThrowUtils.throwIf(searchPageRequest == null, ErrorCode.PARAMS_ERROR);
        String searchText = searchPageRequest.getSearchText();
        String type = searchPageRequest.getType();

        SearchAllVO searchAllVO = new SearchAllVO();

        // 将RequestAttributes对象设置为子线程共享
        // 下文postDataSource.doSearch中需要获取RequestAttributes，但是此方法是在【子线程】中执行的，只有设置为子线程共享后才可以获取到
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(sra, true);

        // type 为空，搜索所有类型的数据
        if (StrUtil.isBlank(type)) {
            // 多线程并发
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() ->
                    userDataSource.doSearch(searchText, 1L, 10L));

            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() ->
                    postDataSource.doSearch(searchText, 1L, 10L));

            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() ->
                    pictureDataSource.doSearch(searchText, 1L, 10L));

            CompletableFuture.allOf(userTask, postTask, pictureTask).join();

            try {
                searchAllVO.setUserList(userTask.get().getRecords());
                searchAllVO.setPostList(postTask.get().getRecords());
                searchAllVO.setPictureList(pictureTask.get().getRecords());
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "搜索出现异常");
            }
        } else {
            SearchDataTypeEnum typeEnum = SearchDataTypeEnum.getEnumByValue(type);
            // type 值不合法，报错
            ThrowUtils.throwIf(typeEnum == null, ErrorCode.PARAMS_ERROR);

            // type 值合法，搜索对应类型的数据
            DataSource dataSource = dataSourceRegistry.getDataSource(typeEnum.getValue());

            Page page = dataSource.doSearch(searchText, 1L, 10L);
            searchAllVO.setDataList(page.getRecords());
        }

        return searchAllVO;
    }
}
