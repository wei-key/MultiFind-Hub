package com.weikey.multifindhub.service.impl;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.service.PictureService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PictureServiceImplTest {

    @Resource
    PictureService pictureService;

    @Test
    void searchPicturesByPage() {
        Page<Picture> page = pictureService.searchPicturesByPage("坤坤", 1, 10);

        List<Picture> records = page.getRecords();
        long total = page.getTotal();
        long size = page.getSize();
        long current = page.getCurrent();

        System.out.println(records);
        System.out.println(total);
        System.out.println(size);
        System.out.println(current);
    }
}