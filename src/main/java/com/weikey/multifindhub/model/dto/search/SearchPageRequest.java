package com.weikey.multifindhub.model.dto.search;

import com.weikey.multifindhub.annotation.RequestKeyParam;
import com.weikey.multifindhub.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author wei-key
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchPageRequest extends PageRequest implements Serializable {

    /**
     * 搜索关键词
     */
    @RequestKeyParam
    private String searchText;

    /**
     * 搜索数据类型
     */
    @RequestKeyParam
    private String type;

    private static final long serialVersionUID = 1L;
}