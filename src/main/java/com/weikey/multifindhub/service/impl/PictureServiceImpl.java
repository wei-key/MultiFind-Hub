package com.weikey.multifindhub.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.exception.BusinessException;
import com.weikey.multifindhub.exception.ThrowUtils;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.service.PictureService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PictureServiceImpl implements PictureService {
    /**
     * 搜索图片，并分页
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<Picture> searchPicturesByPage(String searchText, long pageNum, long pageSize) {
        // 参数校验
        if (StrUtil.isBlank(searchText) || pageNum <= 0 || pageSize <= 0 || pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String url = String.format("https://cn.bing.com/images/search?q=%s&form=HDRSC3&first=%d", searchText, (pageNum - 1) * pageSize + 1);

        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 通过class选择器查找
        Elements elements = doc.select(".iuscp.isv"); // html中写的是【class="iuscp isv"】，这里却要写为【.iuscp.isv】中间的空格改为了点
        Gson gson = new Gson();

        List<Picture> pictureList = new ArrayList<>();

        for (Element element : elements) {
            // 图片地址
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = gson.fromJson(m, Map.class);
            String murl = (String) map.get("murl");
            // 图片标题
            String title = element.select(".inflnk").attr("aria-label");

            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictureList.add(picture);

            if (pictureList.size() >= pageSize) {
                break;
            }
        }

        Page<Picture> page = new Page<>(pageNum, pageSize, elements.size());
        page.setRecords(pictureList);
        return page;
    }
}
