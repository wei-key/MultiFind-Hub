package com.weikey.multifindhub.datasource;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.weikey.multifindhub.common.ErrorCode;
import com.weikey.multifindhub.exception.BusinessException;
import com.weikey.multifindhub.model.vo.VideoVo;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wei-key
 */
@Service
@Slf4j
public class VideoDataSource implements DataSource {

    @Override
    public Page<VideoVo> doSearch(String searchText, long pageNum, long pageSize) {

        String url1 = "https://www.bilibili.com/";
        String url2 = String.format("https://api.bilibili.com/x/web-interface/search/type?search_type=video&keyword=%s",searchText);
        HttpCookie cookie = HttpRequest.get(url1).execute().getCookie("buvid3");

        String body = null;
        // 重试机制
        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
                // 有异常则重试
                .retryIfException()
                // 设置最大执行次数3次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3)).build();
        try {
            body = retryer.call(() -> HttpRequest.get(url2)
                    .cookie(cookie)
                    .execute().body());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map map = JSONUtil.toBean(body, Map.class);
        Map data = (Map)map.get("data");
        JSONArray videoList = (JSONArray) data.get("result");
        Page<VideoVo> page = new Page<>(pageNum,pageSize);
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
            if(videoVoList.size()>=pageSize){
                break;
            }
        }
        page.setRecords(videoVoList);
        return page;
    }
}
