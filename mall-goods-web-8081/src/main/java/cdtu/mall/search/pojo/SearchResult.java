package cdtu.mall.search.pojo;


import cdtu.mall.common.response.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 分类：页面显示了分类名称，但背后肯定要保存id信息。所以至少要有id和name
 *
 * 我们新建一个类，继承PageResult，然后扩展两个新的属性：分类集合和品牌集合：
 */
public class SearchResult extends PageResult<Goods> {

    public List<Map<String, Object>> getCategories() {
        return categories;
    }

    public void setCategories(List<Map<String, Object>> categories) {
        this.categories = categories;
    }

    private List<Map<String,Object>> categories;

//        private List<Map<String,Object>> specs;

}
