package com.weikey.multifindhub.manager;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.datasource.*;
import com.weikey.multifindhub.exception.BusinessException;
import com.weikey.multifindhub.exception.ThrowUtils;
import com.weikey.multifindhub.model.dto.search.SearchPageRequest;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.model.entity.Post;
import com.weikey.multifindhub.model.enums.SearchDataTypeEnum;
import com.weikey.multifindhub.model.vo.PostVO;
import com.weikey.multifindhub.model.vo.SearchAllVO;
import com.weikey.multifindhub.model.vo.UserVO;
import com.weikey.multifindhub.model.vo.VideoVo;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private VideoDataSource videoDataSource;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    // 自定义线程池（IO密集型）
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(40, 80, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(300));


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

        // type 为空，搜索所有类型的数据
        if (StrUtil.isBlank(type)) {
            // 多线程并发
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() ->
                    userDataSource.doSearch(searchText, 1L, 10L), executor);

            CompletableFuture<Page<Post>> postTask = CompletableFuture.supplyAsync(() ->
                    postDataSource.doSearch(searchText, 1L, 10L), executor);

            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() ->
                    pictureDataSource.doSearch(searchText, 1L, 10L), executor);

            CompletableFuture<Page<VideoVo>> videoTask = CompletableFuture.supplyAsync(() ->
                    videoDataSource.doSearch(searchText, 1L, 10L), executor);

            CompletableFuture.allOf(userTask, postTask, pictureTask, videoTask).join();

            try {
                searchAllVO.setUserList(userTask.get().getRecords());
                searchAllVO.setPostList(postTask.get().getRecords());
                searchAllVO.setPictureList(pictureTask.get().getRecords());
                searchAllVO.setVideoList(videoTask.get().getRecords());
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

    /**
     * 聚合搜索（串行）
     *
     * @param searchPageRequest
     * @return
     */
    @Deprecated
    public SearchAllVO searchAllSync(SearchPageRequest searchPageRequest) {
        // 参数校验
        ThrowUtils.throwIf(searchPageRequest == null, ErrorCode.PARAMS_ERROR);
        String searchText = searchPageRequest.getSearchText();
        String type = searchPageRequest.getType();

        SearchAllVO searchAllVO = new SearchAllVO();

        // type 为空，搜索所有类型的数据
        if (StrUtil.isBlank(type)) {
            searchAllVO.setUserList(userDataSource.doSearch(searchText, 1L, 10L).getRecords());
            searchAllVO.setPostList(postDataSource.doSearch(searchText, 1L, 10L).getRecords());
            searchAllVO.setPictureList(pictureDataSource.doSearch(searchText, 1L, 10L).getRecords());
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
