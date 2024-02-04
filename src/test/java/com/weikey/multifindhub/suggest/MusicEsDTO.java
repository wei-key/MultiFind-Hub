package com.weikey.multifindhub.suggest;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.weikey.multifindhub.model.entity.Post;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * ES 包装类
 *
 * @author wei-key
 * 
 **/
@Document(indexName = "music")
@Data
public class MusicEsDTO implements Serializable {

    private List<String> suggest;

    private static final long serialVersionUID = 1L;
}
