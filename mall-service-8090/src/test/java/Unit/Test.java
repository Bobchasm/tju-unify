package Unit;

import cdtu.mall.service.CdtuMallServiceApplication;
import cdtu.mall.service.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CdtuMallServiceApplication.class)
public class Test {
    @Autowired
    private UnitService unitService;

    @org.junit.jupiter.api.Test
    public void test1()
    {
//        System.out.println(unitService.utterance());
    }

}
