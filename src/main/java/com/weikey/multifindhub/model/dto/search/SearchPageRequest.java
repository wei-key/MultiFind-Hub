package com.weikey.multifindhub.model.dto.search;

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
    private String searchText;

    private static final long serialVersionUID = 1L;
}