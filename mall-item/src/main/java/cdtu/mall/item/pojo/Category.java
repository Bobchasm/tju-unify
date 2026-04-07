package cdtu.mall.item.pojo;

import lombok.Data;

@Data
public class Category {

    private String id;
    private String categoryId;
    private String categoryName;
    private String parentId;
    private Integer categoryLevel;
    private String img;
    private String href;
    private Integer categoryStatus;

}
