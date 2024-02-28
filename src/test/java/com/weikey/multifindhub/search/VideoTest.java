package com.weikey.multifindhub.search;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.exception.BusinessException;
import com.weikey.multifindhub.model.entity.Post;
import com.weikey.multifindhub.model.vo.VideoVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class VideoTest {
    @Test
    public void testPassage() {
        String searchText = "mysql";
        String url1 = "https://www.bilibili.com/";
        String url2 = String.format("https://api.bilibili.com/x/web-interface/search/type?search_type=video&keyword=%s", searchText);
        HttpCookie cookie = HttpRequest.get(url1).execute().getCookie("buvid3");

        String body = null;
        body = HttpRequest.get(url2)
                .cookie(cookie)
                .execute().body();

        Map map = JSONUtil.toBean(body, Map.class);
        Map data = (Map)map.get("data");
        JSONArray videoList = (JSONArray) data.get("result");
        Page<VideoVo> page = new Page<>(1,10);
        List<VideoVo> videoVoList = new ArrayList<>();
        for(Object video:videoList){
            JSONObject tempVideo = (JSONObject)video;
            VideoVo videoVo = new VideoVo();
            videoVo.setUpic(tempVideo.getStr("upic"));
            videoVo.setAuthor(tempVideo.getStr("author"));
            videoVo.setPubdate(tempVideo.getInt("pubdate"));
            videoVo.setArcurl(tempVideo.getStr("arcurl"));
            videoVo.setPic("http:"+tempVideo.getStr("pic"));
            videoVo.setTitle(tempVideo.getStr("title"));
            videoVo.setDescription(tempVideo.getStr("description"));
            videoVoList.add(videoVo);
            if(videoVoList.size()>=10){
                break;
            }
        }
        page.setRecords(videoVoList);
    }

}
