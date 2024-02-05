package com.weikey.multifindhub.suggest;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.weikey.multifindhub.model.dto.post.PostEsDTO;
import com.weikey.multifindhub.model.entity.Picture;
import com.weikey.multifindhub.model.entity.Post;
import com.weikey.multifindhub.service.PostService;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class HighLightTest {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void testPassage() {
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", "java");
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("title").preTags("<tag1>").postTags("</tag1>");


        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withHighlightBuilder(highlightBuilder)
                .withQuery(matchQueryBuilder).build();
        List<SearchHit<PostEsDTO>> searchHits = elasticsearchRestTemplate
                .search(searchQuery, PostEsDTO.class).getSearchHits();

        searchHits.forEach(searchHit -> {
            searchHit.getHighlightField("content").forEach(System.out::println);
            System.out.println("=============================================");
        });

    }
}
