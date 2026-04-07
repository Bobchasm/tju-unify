package cdtu.mall.goods.service.Imp;

import cdtu.mall.goods.dao.CategoryDao;
import cdtu.mall.goods.service.CategoryService;
import cdtu.mall.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImp implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public List<Category> findAll() {
        List<Category> categories = categoryDao.findAll();
        return categories;
    }

    @Override
    public List<Category> findListByCategoryName(String keyName) {
        List<Category> list = categoryDao.findListByCategoryName(keyName);
        return list;
    }

    @Override
    public List<Category> findOneByCategoryId(String cid) {
        List<Category> categories = categoryDao.findOneByCategoryId(cid);
        return categories;
    }
}
