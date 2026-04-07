package cdtu.mall.search.controller;

import cdtu.mall.common.response.PageResult;
import cdtu.mall.item.response.RespGood;
import cdtu.mall.item.response.SearchInfo;
import cdtu.mall.search.pojo.Goods;
import cdtu.mall.search.pojo.SearchRequest;
import cdtu.mall.search.service.imp.SearchServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchController {

    @Autowired
    private SearchServiceImp searchServiceImp;

    @RequestMapping("page")
    public ResponseEntity<PageResult<Goods>> search(
            @RequestParam("key") String key,
            @RequestParam("page")int page,
            @RequestParam(value = "filter",required = false) int flag /*0,不做处理，1为最新上架、2为价格最低、3为价格最高*/)
    {
        SearchRequest searchRequest=new SearchRequest();
        searchRequest.setKey(key);
        searchRequest.setPage(page);
        PageResult<Goods> result=this.searchServiceImp.search(searchRequest,flag);
        if(result==null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);

    }

    @RequestMapping("/detail")
    public Goods findBySkuId(@RequestParam("id")String skuId)
    {
        Goods goods= searchServiceImp.findBySkuId(skuId);
        return goods;
    }

    @RequestMapping("/info")
    public PageResult<SearchInfo> searchInfo(@RequestParam("key") String key, @RequestParam("page") int page)
    {
        SearchRequest searchRequest=new SearchRequest();
        searchRequest.setKey(key);
        searchRequest.setPage(page);
        PageResult<SearchInfo> infoPageResult = searchServiceImp.searchInfo(searchRequest);
        return infoPageResult;
    }



}
