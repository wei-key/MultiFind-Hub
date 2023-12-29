package com.weikey.multifindhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片
 *
 * @author wei-key
 * 
 */
@Data
public class Picture implements Serializable {


    /**
     * 标题
     */
    private String title;

    /**
     * 链接
     */
    private String url;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}