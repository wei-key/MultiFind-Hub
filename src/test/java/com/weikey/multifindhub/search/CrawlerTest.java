package com.weikey.multifindhub.search;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.model.entity.Post;
import com.weikey.multifindhub.service.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class CrawlerTest {

    @Resource
    private PostService postService;

    @Test
    public void testPassage() {
        Gson gson = new Gson();

        String url = "https://www.code-nav.cn/api/post/search/page/vo";
//        String json = "{\n" +
//                "  \"current\": 1,\n" +
//                "  \"pageSize\": 8,\n" +
//                "  \"sortField\": \"createTime\",\n" +
//                "  \"sortOrder\": \"descend\",\n" +
//                "  \"category\": \"文章\",\n" +
//                "  \"reviewStatus\": 1\n" +
//                "}";
        String json = "{\n" +
                "  \"current\": 1,\n" +
                "  \"pageSize\": 8,\n" +
                "  \"sortField\": \"_score\",\n" +
                "  \"sortOrder\": \"descend\",\n" +
                "  \"searchText\": \"redis\",\n" +
                "  \"category\": \"文章\",\n" +
                "  \"reviewStatus\": 1\n" +
                "}";
                String result = HttpRequest.post(url)
                .body(json)
                .execute()
                .body();
        // 解析json
        Map<String, Object> map = gson.fromJson(result, Map.class);
        Map<String, Object> data = (Map<String, Object>) map.get("data"); // gson将json对象解析为map（LinkedTreeMap）
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records"); // gson将json数组解析为List（ArrayList）

        List<Post> postList = new ArrayList<>();

        for (Map<String, Object> record : records) {

            System.out.println("数字：" + record.get("viewNum").getClass());
            System.out.println("str：" + record.get("category").getClass());

            String title = (String) record.get("title");
            String content = (String) record.get("content");
//            String tags = record.get("tags").toString();

            List<String> tagList = (List<String>) record.get("tags");
            String tags = tagList.stream().map(tag -> "\"" + tag + "\"").collect(Collectors.toList()).toString();

            Post post = new Post();
            post.setTitle(title);
            post.setContent(content);
            post.setTags(tags);
            post.setUserId(1L);
            postList.add(post);
        }

        // 入库
        boolean b = postService.saveBatch(postList);
        Assertions.assertTrue(b);
    }

    @Test
    public void testPicture() {
        String url = "https://cn.bing.com/images/search?q=%E5%B0%8F%E9%BB%91%E5%AD%90&form=HDRSC3&first=1";

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
        }

        System.out.println(pictureList);
    }
}
