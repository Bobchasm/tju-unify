package cdtu.mall.faceLogin;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FaceloginApplicationTests {

    @Test
    public void test2()
    {
        Long x=134L;
        System.out.println(x.toString());
    }

    @Test
    public void test() {
        int cockNum;//公鸡数量
        int henNum;//母鸡数量
        int chickenNum;//小鸡数量
        int totalNum=100;//一共100只鸡
        int money=100;//100元钱
        /**
         * 提高算法质量
         * 1、取每只鸡最大数量
         * 公鸡：33
         * 母鸡：33
         * 小鸡：100
         */
        int flag=0;
        for(cockNum=1;cockNum<=33;cockNum++)
        {
            if(flag==1)
                break;
            for(henNum=1;henNum<=33;henNum++)
            {
                if(flag==1)
                    break;
                for(chickenNum=3;chickenNum<=99;chickenNum=chickenNum+3)
                {
                    if((cockNum*3+henNum*3+chickenNum/3)==100&&(chickenNum+henNum+cockNum)==100)
                    {
                        System.out.println("公鸡:"+cockNum);
                        System.out.println("母鸡:"+henNum);
                        System.out.println("消极"+chickenNum);
                        flag=1;
                        break;
                    }
                }
            }
        }
    }

}
