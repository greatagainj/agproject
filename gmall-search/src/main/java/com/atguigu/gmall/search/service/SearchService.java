package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParam;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVO;
import com.atguigu.gmall.search.pojo.SearchResponseVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public SearchResponseVO search(SearchParam searchParam) throws IOException {

        // 构建dsl语句
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);

        SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(response);

        SearchResponseVO  responseVO = this.parseSearchResult(response);

        responseVO.setPageNum(searchParam.getPageNum());
        responseVO.setPageSize(searchParam.getPageSize());

        return responseVO;

    }

    private SearchResponseVO parseSearchResult(SearchResponse response) {
        SearchResponseVO responseVO = new SearchResponseVO();

        // 获取总搜索记录数
        SearchHits hits = response.getHits();
        long totalHits = hits.getTotalHits();
        responseVO.setTotal(totalHits);





        //解析品牌聚合结果集
        SearchResponseAttrVO brand = new SearchResponseAttrVO();
        brand.setName("品牌");
          //获取品牌的聚合结果集
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        List<String> brandValues = brandIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map = new HashMap<>();
            // 获取品牌ID
            map.put("id", bucket.getKeyAsString());
            // 获取品牌名称，从子聚合查询
            Map<String, Aggregation> brandIdSubMap = bucket.getAggregations().asMap();
            ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandIdSubMap.get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            map.put("name", brandName);

            return JSONObject.toJSONString(map);
        }).collect(Collectors.toList());
        brand.setValue(brandValues);
        responseVO.setBrand(brand);




        //解析分类聚合结果集
        SearchResponseAttrVO category = new SearchResponseAttrVO();
        category.setName("分类");
        //获取分类的聚合结果集
        Map<String, Aggregation> categoryAggregationMap = response.getAggregations().asMap();
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<String> categoryValues = categoryIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map1 = new HashMap<>();
            // 获取分类ID
            map1.put("id", bucket.getKeyAsString());
            // 获取分类名称，从子聚合查询
            Map<String, Aggregation> categoryIdSubMap = bucket.getAggregations().asMap();
            ParsedStringTerms categoryNameAgg = (ParsedStringTerms) categoryIdSubMap.get("categoryNameAgg");
            String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();
            map1.put("name", categoryName);

            return JSONObject.toJSONString(map1);
        }).collect(Collectors.toList());
        category.setValue(categoryValues);
        responseVO.setCatelog(category);


        // 解析商品查询结果集
        SearchHit[] subHits = hits.getHits();
        List<Goods> goodsList = new ArrayList<>();
        for (SearchHit subHit : subHits) {
            Goods goods = JSONObject.parseObject(subHit.getSourceAsString(), Goods.class);

            // 设置标题为高亮结果集
            String title = subHit.getHighlightFields().get("title").getFragments()[0].toString();
            goods.setTitle(title);

            goodsList.add(goods);
        }
        responseVO.setProducts(goodsList);

        // 解析规格参数
        // 获取嵌套聚合对象
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        // 规格参数id聚合对象
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrAgg.getAggregations().get("attrIdAgg");
        List<Terms.Bucket> buckets = (List<Terms.Bucket>) attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            List<SearchResponseAttrVO> searchResponseAttrVO = buckets.stream().map(bucket -> {
                SearchResponseAttrVO responseAttrVO = new SearchResponseAttrVO();
                // 设置规格参数id
                responseAttrVO.setProductAttributeId(bucket.getKeyAsNumber().longValue());
                // 设置规格参数名称（例:内存）
                List<? extends Terms.Bucket> nameBuckets = ((ParsedStringTerms) (bucket.getAggregations().get("attrNameAgg"))).getBuckets();
                responseAttrVO.setName(nameBuckets.get(0).getKeyAsString());
                // 设置规格参数Value(例:8GB 16GB 32GB)
                List<? extends Terms.Bucket> valueBuckets = ((ParsedStringTerms) (bucket.getAggregations().get("attrValueAgg"))).getBuckets();
                List<String> values = valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                responseAttrVO.setValue(values);
                return responseAttrVO;
            }).collect(Collectors.toList());
            responseVO.setAttrs(searchResponseAttrVO);
        }


        return responseVO;
    }

    private SearchRequest buildQueryDsl(SearchParam searchParam) {

        // 查询条件(关键字)
        String keyword = searchParam.getKeyword();

        // 过滤条件(品牌id)
        String brand[] = searchParam.getBrand();

        // 过滤条件(产品3级分类)
        String category[] = searchParam.getCatelog3();

        // 规格属性
        String props[] = searchParam.getProps();

        // 价格区间
        Integer priceFrom = searchParam.getPriceFrom();
        Integer priceTo = searchParam.getPriceTo();

        if (StringUtils.isBlank(keyword)) {
            return null;
        }

        // 查询条件构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 查询条件和过滤条件
        //1"bool":
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1"must": 构建查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        //1.2"filter": 构建过滤条件
        if (brand != null && brand.length !=0) {
            //1.2.1 构建过滤条件（品牌过滤） //"terms"           // "brandId":
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brand));
        }
        if (category != null && category.length != 0) {
            //1.2.2 构建过滤条件（3级分类过滤） //"terms"        // "categoryId":
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", category));
        }
            //1.2.3 构建嵌套过滤条件（规格属性） //"bool"
            if (props != null && props.length != 0) {
                //多个prop属性
                for (String prop : props) {
                    // 构建嵌套查询"bool":
                    BoolQueryBuilder propQueryBuilder = QueryBuilders.boolQuery(); //line32

                    // 规格参数 例：内存:16GB-8GB / attrId:attrValue(以”-“分隔)
                    String[] split = StringUtils.split(prop, ":");
                    if (split == null || split.length != 2) {
                        continue;
                    }
                    // 处理attrValues
                    String[] attrValues = StringUtils.split(split[1], "-");
                    // 构建嵌套查询子查询"bool":
                    BoolQueryBuilder nestedQueryBuilder = QueryBuilders.boolQuery(); //line38
                    //构建嵌套的子查询条件
                    //1.2.3.1 "term": {"attrs.attrId": "37"}
                    nestedQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    //1.2.3.2 "term": {"attrs.attrValue": "枭龙855"}
                    nestedQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                    //"must":
                    propQueryBuilder.must(QueryBuilders.nestedQuery("attrs", nestedQueryBuilder, ScoreMode.None));//line39
                    boolQueryBuilder.filter(propQueryBuilder);
                }
            }

        // 1.2.4 价格区间过滤
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
        if (priceFrom != null){
            rangeQueryBuilder.gte(priceFrom);
        }
        if (priceTo != null){
            rangeQueryBuilder.lte(priceTo);
        }
        boolQueryBuilder.filter(rangeQueryBuilder);

        sourceBuilder.query(boolQueryBuilder);

        // 2构建分页
        Integer pageNum = searchParam.getPageNum();
        Integer pageSize = searchParam.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        //3构建排序
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] orders = StringUtils.split(order, ":");
            if (orders != null && orders.length == 2) {
                String field = null;
                switch (orders[0]) {
                    case "1" : field = "sale"; break;
                    case "2" : field = "price"; break;
                }
                sourceBuilder.sort(field,StringUtils.equals("asc", orders[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        }

        // 4构建高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<em>").postTags("</em>"));

        // 5构建聚合
        // 5.1 品牌聚合
        // 5.1.1 品牌聚合
        TermsAggregationBuilder brandAggregationBuilder = AggregationBuilders.terms("brandIdAgg").field("brandId");
        // 5.1.2 品牌子聚合
        brandAggregationBuilder.subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"));
        sourceBuilder.aggregation(brandAggregationBuilder);

        //5.2 分类聚合
        // 5.2.1 分类聚合
        TermsAggregationBuilder cateAggregationBuilder = AggregationBuilders.terms("categoryIdAgg").field("categoryId");
        // 5.2.2 分类子聚合
        cateAggregationBuilder.subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"));
        sourceBuilder.aggregation(cateAggregationBuilder);

        //5.3 搜索的规格属性聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

    System.out.println(sourceBuilder.toString());

    // 查询结果 过滤
        sourceBuilder.fetchSource(new String[] {"skuId", "pic", "title", "price"}, null);

        // 查询参数
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        searchRequest.source(sourceBuilder);
        return searchRequest;

    }
}
