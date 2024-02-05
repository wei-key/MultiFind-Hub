package com.weikey.multifindhub.suggest;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.weikey.multifindhub.service.PostService;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class SuggestTest {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private PostService postService;

    @Test
    public void testPassage() {
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("song-suggest",
                SuggestBuilders.completionSuggestion("suggest").prefix("你好"));

        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withSuggestBuilder(suggestBuilder).build();
        SearchHits<MusicEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, MusicEsDTO.class);

        Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry = searchHits.getSuggest().getSuggestion("song-suggest").getEntries().get(0);

        List<CompletionSuggestion.Entry.Option<MusicEsDTO>> options = (List<CompletionSuggestion.Entry.Option<MusicEsDTO>>) entry.getOptions();
        ArrayList<String> list = new ArrayList<>();
        if (options.isEmpty()) {
            System.out.println("null");
            return;
        }
        options.forEach(option -> list.add(option.getSearchHit().getContent().getSuggest().get(0)));
        list.forEach(System.out::println);

    }

    @Test
    public void testPinyin() {
        String letter= PinyinUtil.getFirstLetter("你好"," ");//nihao
        System.out.println(letter);

    }


}
