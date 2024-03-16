package com.weikey.multifindhub.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.weikey.multifindhub.model.dto.post.PostEsDTO;
import com.weikey.multifindhub.model.dto.post.PostQueryRequest;
import com.weikey.multifindhub.constant.CommonConstant;
import com.weikey.multifindhub.mapper.PostMapper;
import com.weikey.multifindhub.model.entity.Post;
import com.weikey.multifindhub.service.PostService;
import com.weikey.multifindhub.service.UserService;
import com.weikey.multifindhub.utils.SqlUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.stereotype.Service;

/**
 * 帖子服务实现
 *
 * @author wei-key
 * 
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final static Gson GSON = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        List<String> orTagList = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        // es 起始页为 0
        long current = postQueryRequest.getCurrent() - 1;
        long pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        // 逻辑删除
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        // 必须包含所有标签
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 包含任何一个标签即可
        if (CollectionUtils.isNotEmpty(orTagList)) {
            BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String tag : orTagList) {
                orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
            }
            orTagBoolQueryBuilder.minimumShouldMatch(1);
            boolQueryBuilder.filter(orTagBoolQueryBuilder);
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("description", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按标题检索
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序：默认根据score排序，如果sortField非空，则改为根据sortField排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().preTags("<mark style=\"background-color: #ffcc00\">").postTags("</mark>")
                .field("title").field("content");
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).withHighlightBuilder(highlightBuilder).build();
        SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);

        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        Page<Post> page = new Page<>();
        List<Post> resourceList = new ArrayList<>();
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();

            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            // db查出的数据可能与ES中的不同：比ES的少了一些（db中删除后，ES还未同步）；或者 内容有变化
            resourceList = baseMapper.selectBatchIds(postIdList);

            // 将数据替换为高亮显示后的
            if (StrUtil.isNotBlank(title)) {
                HashMap<Long, String> idHighLightedTitleMap = new HashMap<>();
                // 取出高亮数据
                searchHitList.forEach(searchHit -> {
                    List<String> list = searchHit.getHighlightField("title");
                    if (!list.isEmpty()) {
                        idHighLightedTitleMap.put(searchHit.getContent().getId(), list.get(0));
                    }
                });
                // 替换
                resourceList.forEach(resource -> {
                    String highLightedTitle = idHighLightedTitleMap.get(resource.getId());
                    if (highLightedTitle != null) {
                        resource.setTitle(highLightedTitle);
                    }
                });
            }
            if (StrUtil.isNotBlank(content)) {
                HashMap<Long, String> idHighLightedContentMap = new HashMap<>();
                // 取出高亮数据
                searchHitList.forEach(searchHit -> {
                    List<String> list = searchHit.getHighlightField("content");
                    if (!list.isEmpty()) {
                        idHighLightedContentMap.put(searchHit.getContent().getId(), list.get(0));
                    }
                });
                // 替换
                resourceList.forEach(resource -> {
                    String highLightedContent = idHighLightedContentMap.get(resource.getId());
                    if (highLightedContent != null) {
                        resource.setContent(highLightedContent);
                    }
                });
            }
            if (StrUtil.isNotBlank(searchText)) {
                // 取出高亮数据
                HashMap<Long, String> idHighLightedContentMap = new HashMap<>();
                HashMap<Long, String> idHighLightedTitleMap = new HashMap<>();
                // 取出高亮数据
                searchHitList.forEach(searchHit -> {
                    List<String> list = searchHit.getHighlightField("title");
                    if (!list.isEmpty()) {
                        idHighLightedTitleMap.put(searchHit.getContent().getId(), list.get(0));
                    }
                    List<String> list1 = searchHit.getHighlightField("content");
                    if (!list1.isEmpty()) {
                        idHighLightedContentMap.put(searchHit.getContent().getId(), list1.get(0));
                    }
                });
                // 替换
                resourceList.forEach(resource -> {
                    String highLightedContent = idHighLightedContentMap.get(resource.getId());
                    if (highLightedContent != null) {
                        resource.setContent(highLightedContent);
                    }
                    String highLightedTitle = idHighLightedTitleMap.get(resource.getId());
                    if (highLightedTitle != null) {
                        resource.setTitle(highLightedTitle);
                    }
                });
            }

        }
        page.setRecords(resourceList);
        page.setTotal(resourceList.size());
        page.setSize(postQueryRequest.getPageSize());
        page.setCurrent(postQueryRequest.getCurrent());

        return page;
    }

    /**
     * 获取搜索建议
     *
     * @param prefix 搜索词前缀
     * @return
     */
    @Override
    public List<String> getSearchSuggestion(String prefix) {
        ArrayList<String> list = new ArrayList<>();
        if (StrUtil.isBlank(prefix)) {
            return list;
        }
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("title-suggest",
                SuggestBuilders.completionSuggestion("titleSuggestion").prefix(prefix));
        // 查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withSuggestBuilder(suggestBuilder).build();
        SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);
        // 取出结果
        Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry =
                searchHits.getSuggest().getSuggestion("title-suggest").getEntries().get(0);
        List<CompletionSuggestion.Entry.Option<PostEsDTO>> options =
                (List<CompletionSuggestion.Entry.Option<PostEsDTO>>) entry.getOptions();

        if (!options.isEmpty()) {
            options.forEach(option -> list.add(option.getSearchHit().getContent().getTitleSuggestion().get(0)));
        }
        return list;
    }

    /**
     * 获取查询包装类
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.like("title", searchText).or().like("content", searchText);
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<Post> listPostVOByPage(PostQueryRequest postQueryRequest, HttpServletRequest request) {
        long current = postQueryRequest.getCurrent();
        long pageSize = postQueryRequest.getPageSize();
        Page<Post> postPage = this.page(new Page<>(current, pageSize),
                this.getQueryWrapper(postQueryRequest));
        return postPage;
    }
}




