package cdtu.mall.item.response;

import lombok.Data;

import java.util.List;

@Data
public class RespCategory<T> {
    private String categoryName1;
    private String categoryName2;
    private String categoryName3;
    private String categoryId1;
    private String categoryId2;
    private String categoryId3;
    private String categoryImg3;
    private List<T> items;

}
