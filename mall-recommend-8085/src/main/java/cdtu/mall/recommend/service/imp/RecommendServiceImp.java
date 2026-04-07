package cdtu.mall.recommend.service.imp;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.recommend.pojo.Goods;
import cdtu.mall.recommend.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImp implements RecommendService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private static int pageSize=10;
    @Override
    public PageResult<Goods> searchAll(int pageNum) {
        Query query = Query.findAll();
        query.setPageable(PageRequest.of(pageNum, pageSize));

        SearchHits<Goods> hits = elasticsearchOperations.search(query, Goods.class);
        List<Goods> content = hits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
        long total = hits.getTotalHits();
        int totalPages = (int) Math.ceil(total / (double) pageSize);
        return new PageResult<>((int) total, totalPages, content);
    }
}
