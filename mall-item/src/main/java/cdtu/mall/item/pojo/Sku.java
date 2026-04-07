package cdtu.mall.item.pojo;


import lombok.Data;

@Data
public class Sku {

    private String id;
    private String skuId;
    private String categoryId1;
    private String categoryId2;
    private String categoryId3;
    private Double price;
    private String url;
    private String title;
    private String subTitle;
    private String specParam;

}
