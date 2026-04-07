package cdtu.mall.search.pojo;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import org.springframework.data.annotation.Id;
/**
 * 理解：当用户输入关键字可以根据标题、副标题、分类、shop进行检索，而我聚合返回的也只需要price,title,img,id,
 */
@Document(indexName = "mall")
public class Goods {
        @Id
        private String  id; // skuId
        // 所有需要被搜索的信息，包含标题，分类，甚至(品牌---数据库暂时没考虑)
        @Field(type = FieldType.Text, analyzer = "ik_max_word")
        private String title;
        @Field(type = FieldType.Text, analyzer = "ik_max_word")
        private String shopName;//商铺名称
        @Field(type = FieldType.Text, analyzer = "ik_max_word")
        private String cname1;//3个分类字符串并集
        @Field(type = FieldType.Text, analyzer = "ik_max_word")
        private String cname2;//3个分类字符串并集
        @Field(type = FieldType.Text, analyzer = "ik_max_word")
        private String cname3;//3个分类字符串并集
        @Field(type = FieldType.Keyword, index = false)
        private String subTitle;// 卖点
        //这3个分类id在我处理分类时可以根据三级分类找到匹配的商品
        private String cid1;// 1级分类id
        private String cid2;// 2级分类id
        private String cid3;// 3级分类id
        private Double price;// 价格
        private String img;//商品轮播图
        private String paramImg;
        private String shopImg;
        private String shopFan;
        private String point;
        private String goodsNum;
        private String specParam;

    public String getSpecParam() {
        return specParam;
    }

    public void setSpecParam(String specParam) {
        this.specParam = specParam;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getCname1() {
        return cname1;
    }

    public void setCname1(String cname1) {
        this.cname1 = cname1;
    }

    public String getCname2() {
        return cname2;
    }

    public void setCname2(String cname2) {
        this.cname2 = cname2;
    }

    public String getCname3() {
        return cname3;
    }

    public void setCname3(String cname3) {
        this.cname3 = cname3;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getCid1() {
        return cid1;
    }

    public void setCid1(String cid1) {
        this.cid1 = cid1;
    }

    public String getCid2() {
        return cid2;
    }

    public void setCid2(String cid2) {
        this.cid2 = cid2;
    }

    public String getCid3() {
        return cid3;
    }

    public void setCid3(String cid3) {
        this.cid3 = cid3;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getParamImg() {
        return paramImg;
    }

    public void setParamImg(String paramImg) {
        this.paramImg = paramImg;
    }

    public String getShopImg() {
        return shopImg;
    }

    public void setShopImg(String shopImg) {
        this.shopImg = shopImg;
    }

    public String getShopFan() {
        return shopFan;
    }

    public void setShopFan(String shopFan) {
        this.shopFan = shopFan;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getGoodsNum() {
        return goodsNum;
    }

    public void setGoodsNum(String goodsNum) {
        this.goodsNum = goodsNum;
    }
}
