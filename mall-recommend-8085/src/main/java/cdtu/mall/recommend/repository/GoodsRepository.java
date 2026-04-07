package cdtu.mall.recommend.repository;

import cdtu.mall.recommend.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepository extends ElasticsearchRepository<Goods,String> {
}
