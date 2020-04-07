package com.atguigu.gmall.search.repositoty;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
