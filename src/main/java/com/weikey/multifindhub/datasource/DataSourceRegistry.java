package com.weikey.multifindhub.datasource;

import com.weikey.multifindhub.model.enums.SearchDataTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * DataSource注册器
 */
@Component
public class DataSourceRegistry {

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private VideoDataSource videoDataSource;

    private Map<String, DataSource> typeDataSourceMap;

    @PostConstruct
    public void init() {
        typeDataSourceMap = new HashMap<>();
        typeDataSourceMap.put(SearchDataTypeEnum.USER.getValue(), userDataSource);
        typeDataSourceMap.put(SearchDataTypeEnum.POST.getValue(), postDataSource);
        typeDataSourceMap.put(SearchDataTypeEnum.PICTURE.getValue(), pictureDataSource);
        typeDataSourceMap.put(SearchDataTypeEnum.VIDEO.getValue(), videoDataSource);
    }

    public DataSource getDataSource(String name) {
        return typeDataSourceMap.get(name);
    }
}
