package cdtu.mall.second.utils;

import cdtu.mall.second.pojo.SecGoods;

import java.util.Comparator;

public class ComparatorHot implements Comparator {

    /**
     * 如果o1小于o2,返回一个负数;如果o1大于o2，返回一个正数;如果他们相等，则返回0;
     */
    @Override
    public int compare(Object o1, Object o2) {
        SecGoods secGoods1=(SecGoods)o1;
        SecGoods secGoods2=(SecGoods)o2;
        //最新
        if(secGoods1.getLook()>secGoods2.getLook())
        {
            return -1;
        }
        else if(secGoods1.getLook()<secGoods2.getLook())
        {
            return 1;
        }
        return 0;
    }
}
