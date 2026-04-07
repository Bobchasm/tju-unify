package cdtu.mall.search.service.imp;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.goods.service.Imp.CategoryServiceImp;
import cdtu.mall.goods.service.Imp.ImgServiceImp;
import cdtu.mall.goods.service.Imp.ShopServiceImp;
import cdtu.mall.goods.service.Imp.SkusServiceImp;
import cdtu.mall.item.pojo.Category;
import cdtu.mall.item.pojo.Images;
import cdtu.mall.item.pojo.Shop;
import cdtu.mall.item.pojo.Sku;
import cdtu.mall.item.response.RespGood;
import cdtu.mall.item.response.SearchInfo;
import cdtu.mall.search.pojo.Goods;
import cdtu.mall.search.pojo.SearchRequest;
import cdtu.mall.search.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImp implements SearchService {

    @Autowired
    private SkusServiceImp skuServiceImp;
    @Autowired
    private ShopServiceImp shopServiceImp;
    @Autowired
    private ImgServiceImp imgServiceImp;
    @Autowired
    private CategoryServiceImp categoryServiceImp;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<Goods> buildGoodsList(int pageNum, int pageSize )
    {
        List<Goods> goodsList=new ArrayList<>();
        List<Sku> skus = skuServiceImp.findAll(pageNum, pageSize, "");
        for(Sku sku:skus)
        {
            Goods goods=new Goods();
            //获取每个商品的三级分类名称
            String cname1="";
            String cname2="";
            String cname3="";
            String shopName="";
            String shopImg="";
            String shopFan="";
            String point="";
            String goodsNum="";
            String specParam;
            List<String> img=new ArrayList<>();//商品轮播主图
            List<String> paramImg=new ArrayList<>();//商品详情图
//            String img="https://img14.360buyimg.com/focus/s140x140_jfs/t17737/296/1785917115/15150/57cc21cb/5ad81b4cNa47d6aef.jpg";//主图
            List<Category> categoryList1 = categoryServiceImp.findOneByCategoryId(sku.getCategoryId1());
            for(Category category:categoryList1)
            {
                cname1=category.getCategoryName()+" ";
            }
            List<Category> categoryList2 = categoryServiceImp.findOneByCategoryId(sku.getCategoryId2());
            for(Category category:categoryList2)
            {
                cname2=category.getCategoryName()+" ";
            }
            List<Category> categoryList3 = categoryServiceImp.findOneByCategoryId(sku.getCategoryId3());
            for(Category category:categoryList3)
            {
                cname3=category.getCategoryName()+" ";
            }
            //获取商品的商铺名
            List<Shop> shops = shopServiceImp.findOneBySkuId(sku.getSkuId());
            Shop shop=shops.get(0);
            shopName=shop.getShopName();
            shopImg=shop.getShopImg();
            shopFan=shop.getFan();
            goodsNum=shop.getGoodsNum();
            //获取商品主图
            List<Images> images = imgServiceImp.findAllBySkuId(sku.getSkuId());
            int flag=0;
            for(Images images1:images)
            {
                if((images1.getIsMaster().equals("-1")))
                {
                    paramImg.add(images1.getUrl());
                }
//                img.add(images1.getUrl());
                if((images1.getIsMaster()).equals("1"))
                {
                    flag=1;
                    img.add(0,images1.getUrl());
                }
                else{
                    img.add(images1.getUrl());
                }
            }
            if(flag==0)
            {
                img.add(0,"https://img14.360buyimg.com/focus/s140x140_jfs/t17737/296/1785917115/15150/57cc21cb/5ad81b4cNa47d6aef.jpg");
            }

            String s1="";
            String s2="";
            //图片转换为json字符串
            try{
                ObjectMapper mapper=new ObjectMapper();
                 s1 = mapper.writeValueAsString(paramImg);
                s2 = mapper.writeValueAsString(img);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            //获取goods
            goods.setCid1(sku.getCategoryId2());
            goods.setCid2(sku.getCategoryId2());
            goods.setCid3(sku.getCategoryId3());
            goods.setCname1(cname1);
            goods.setCname2(cname2);
            goods.setCname3(cname3);
            goods.setImg(s2);
            goods.setParamImg(s1);
            goods.setId(sku.getSkuId());
            goods.setTitle(sku.getTitle());
            goods.setSubTitle(sku.getSubTitle());
            goods.setPrice(sku.getPrice());
            goods.setShopName(shopName);
            goods.setShopFan(shopFan);
            goods.setShopImg(shopImg);
            goods.setGoodsNum(goodsNum);
            goods.setSpecParam(sku.getSpecParam());
//            goods.setPoint("");
            goodsList.add(goods);
            System.out.println("1跳");
        }
        System.out.println("100");
        return goodsList;
    }

    @Override
    public PageResult<Goods> search(SearchRequest searchRequest,int flag) {
        String key = searchRequest.getKey();
        Criteria criteria = new Criteria("title").matches(key)
                .or(new Criteria("shopName").matches(key))
                .or(new Criteria("cname1").matches(key))
                .or(new Criteria("cname2").matches(key))
                .or(new Criteria("cname3").matches(key));

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));

        if (flag == 2) {
            query.addSort(Sort.by(Sort.Direction.ASC, "price"));
        } else if (flag == 3) {
            query.addSort(Sort.by(Sort.Direction.DESC, "price"));
        } else if (flag == 4) {
            query.addSort(Sort.by(Sort.Direction.ASC, "goodsNum"));
        }

        SearchHits<Goods> hits = elasticsearchOperations.search(query, Goods.class);
        List<Goods> content = hits.getSearchHits().stream().map(SearchHit::getContent).toList();
        long total = hits.getTotalHits();
        int totalPages = (int) Math.ceil(total / (double) searchRequest.getSize());
        return new PageResult<>((int) total, totalPages, content);
    }

    @Override
    public Goods findBySkuId(String skuId) {
//        Sku sku = skuServiceImp.findBySkuId(skuId);
//        List<Images> images = imgServiceImp.findAllBySkuId(skuId);
//        Shop shop = shopServiceImp.findOneBySkuId(skuId).get(0);
//        RespGood respGood=new RespGood();
//        respGood.setImgs(images);
//        respGood.setSku(sku);
//        respGood.setShop(shop);
        CriteriaQuery query = new CriteriaQuery(new Criteria("id").is(skuId));
        SearchHits<Goods> hits = elasticsearchOperations.search(query, Goods.class);
        if (hits.isEmpty()) {
            return null;
        }
        return hits.getSearchHit(0).getContent();
    }

    @Override
    public PageResult<SearchInfo> searchInfo(SearchRequest searchRequest) {
        PageResult<Goods> pageResult = this.search(searchRequest,0);
        PageResult<SearchInfo> searchInfoPageResult=new PageResult<>();
        List<SearchInfo> infoList=new ArrayList<>();
        for(Goods goods:pageResult.getItems())
        {
            SearchInfo searchInfo=new SearchInfo();
            searchInfo.setCname1(goods.getCname1());
            searchInfo.setCname2(goods.getCname2());
            searchInfo.setCname3(goods.getCname3());
            int lastIndexOf = goods.getTitle().lastIndexOf(" ");
            if(lastIndexOf<0)
            {
                searchInfo.setKey(" ");
            }
            if(lastIndexOf>=0)
            {
                searchInfo.setKey(goods.getTitle().substring(lastIndexOf));
            }
            infoList.add(searchInfo);
        }
        searchInfoPageResult.setItems(infoList);
        searchInfoPageResult.setTotalPage(pageResult.getTotalPage());
        searchInfoPageResult.setTotal(pageResult.getTotal());
        return searchInfoPageResult;
    }
}
