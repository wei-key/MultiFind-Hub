package com.weikey.multifindhub.job.cycle;

import com.weikey.multifindhub.model.dto.post.PostEsDTO;
import com.weikey.multifindhub.esdao.PostEsDao;
import com.weikey.multifindhub.mapper.PostMapper;
import com.weikey.multifindhub.model.entity.Post;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 增量同步帖子到 es
 *
 * @author wei-key
 * 
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class IncSyncPostToEs {

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostEsDao postEsDao;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000) // fixedRate设置多久执行一次，单位为毫秒
    public void run() {
        // 查询近 5 分钟内发生变更的数据，包括删除
        Date fiveMinutesAgoDate = new Date(System.currentTimeMillis() - 5 * 60 * 1000L);
        List<Post> postList = postMapper.listPostWithDelete(fiveMinutesAgoDate);
        if (CollectionUtils.isEmpty(postList)) {
            log.info("no inc post");
            return;
        }
        // 将实体 Post 转化为 PostEsDTO
        List<PostEsDTO> postEsDTOList = postList.stream()
                .map(PostEsDTO::objToDto)
                .collect(Collectors.toList());
        // 将 PostEsDTO 保存（save）到 ES
        final int pageSize = 500;
        int total = postEsDTOList.size();
        log.info("IncSyncPostToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            postEsDao.saveAll(postEsDTOList.subList(i, end));
        }
        log.info("IncSyncPostToEs end, total {}", total);
    }
}
