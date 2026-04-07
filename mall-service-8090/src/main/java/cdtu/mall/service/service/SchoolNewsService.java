package cdtu.mall.service.service;

import cdtu.mall.service.mapper.SchoolNewsMapper;
import cdtu.mall.service.pojo.SchoolNews;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SchoolNewsService {

    @Autowired
    private SchoolNewsMapper schoolNewsMapper;

    public void save(SchoolNews schoolNews)
    {
        String id = UUID.randomUUID().toString();
        schoolNews.setId(id);
        schoolNewsMapper.insert(schoolNews);
    }

    public List<SchoolNews> getAll()
    {
        List<SchoolNews> schoolNews = schoolNewsMapper.selectAll();
        return schoolNews;
    }

    public void deleteById(String id)
    {
        schoolNewsMapper.deleteByPrimaryKey(id);
    }

    public List<SchoolNews> getByFlag(Long flag,int page)
    {
        Example example=new Example(SchoolNews.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("flag",flag);
//        criteria.andLike("skuName","%"+type+"%");
        PageHelper.startPage(page,10);
        List<SchoolNews> items =new ArrayList<>();
        for(SchoolNews schoolNews:this.schoolNewsMapper.selectByExample(example))
        {
           if(!schoolNews.getContent().equals(""))
           {
               items.add(schoolNews);
           }
        }
//        PageInfo<Items> ItemPageInfo = new PageInfo<>(items);
//        SchoolNews schoolNews=new SchoolNews();
//        schoolNews.setFlag(flag);
//        List<SchoolNews> newsList = schoolNewsMapper.select(schoolNews);
        return items;
    }

    public SchoolNews getById(String id) {
        SchoolNews schoolNews = schoolNewsMapper.selectByPrimaryKey(id);
        return schoolNews;
    }
}
