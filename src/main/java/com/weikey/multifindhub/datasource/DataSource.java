package com.weikey.multifindhub.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 数据源规范
 * @param <T> 数据类型
 */
public interface DataSource <T> {

    /**
     * 能够根据关键词搜索，并且支持分页搜索
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<T> doSearch(String searchText, long pageNum, long pageSize);
}
