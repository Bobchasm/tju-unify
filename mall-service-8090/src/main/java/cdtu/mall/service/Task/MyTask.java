package cdtu.mall.service.Task;

import cdtu.mall.service.pojo.SchoolNews;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

//@Component
//public class MyTask implements PageProcessor {
//    private String url = "http://www.cdtu.edu.cn/xwzx1/xyxw.htm";
//    @Autowired
//    private MyPipeLine myPipeLine;
//
//    String url1="";
//    @Override
//    public void process(Page page) {
//        //新闻分类
//        List<Selectable> list = page.getHtml().xpath("//*[@id=\"page\"]/div/div[2]/dl/dd/a").links().nodes();
//        List<Selectable> newsList = page.getHtml().xpath("//*[@id=\"page\"]/div/div[3]/div[1]/ul/li/a").links().nodes();
//
//        System.out.println(list);
//        if (list.size() == 0) {
//            System.out.println(1);
//            //此时是详情页面，直接获取信息
//            this.saveJobInfo(page);
//        } else {
//            //此时是列表页面，需要获取到链接进入到详情页面
//            for (Selectable selectable : list)
//            {
//                String categoryUrl = selectable.toString();
//                page.addTargetRequest(categoryUrl);
//                if (StringUtils.isNotBlank(categoryUrl)) {
//                    //通过url进入详情页面并加入到任务中
////                    page.addTargetRequest(jobInfoUrl);
//                    for (Selectable s2 : newsList)
//                    {
//                        String detailUrl = s2.toString();
//                        if(StringUtils.isNoneBlank(detailUrl))
//                        {
//                            page.addTargetRequest(detailUrl);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void saveJobInfo(Page page) {
//        String url2 = page.getRequest().getUrl();
//        SchoolNews schoolNews=new SchoolNews();
//        Html html = page.getHtml();
//        String title = html.xpath("//*[@id=\"detail\"]/div/h1/text()").toString();
//        String time = html.xpath("//*[@id=\"detail\"]/div/div[2]/span[1]/text()").toString();
//
//        time=time.substring(4,time.length()-1);
//
//        //栏目
//        String lm = html.xpath("//*[@id=\"detail\"]/div/div[2]/span[2]/a/text()").toString();
//
//        String origin = html.xpath("//*[@id=\"detail\"]/div/div[2]/span[3]/text()").toString();
//        origin=origin.substring(4,origin.length()-1);
//        String unit = html.xpath("//*[@id=\"detail\"]/div/div[2]/span[4]/text()").toString();
//        unit=unit.substring(4,unit.length()-1);
//
//        String content="";
//        List<Selectable> ps = html.xpath("//*[@id=\"page\"]/div/div[1]/div/div[1]/div//p").nodes();
//
//        for(int i=0;i<ps.size();i++)
//        {
//            String xpath="//*[@id=\"page\"]/div/div[1]/div/div[1]/div/p["+i+"]/text()";
//            String s = html.xpath(xpath).toString();
//            if(s!=null&&s!="")
//            content=content+"\n"+s;
//        }
//        Long flag=0l;
//        if(lm.equals("校园新闻"))
//            flag=1l;
//        if(lm.equals("成工要闻"))
//            flag=0l;
//        if(lm.equals("校园文化"))
//            flag=2l;
//        if(lm.equals("教育信息"))
//            flag=3l;
//        if(lm.equals("成工自媒体"))
//            flag=4l;
//
//
//        schoolNews.setUrl(url2);
//        schoolNews.setOrigin(origin);
//        schoolNews.setTime(time);
//        schoolNews.setTitle(title);
//        schoolNews.setFlag(flag);
//        schoolNews.setUnit(unit);
//        schoolNews.setContent(content);
//        page.putField("snewsInfo", schoolNews);
//    }
//    //通过site配置爬虫的配置信息
//    private Site site = Site.me()
//            .setCharset("utf8")
//            .setTimeOut(10 * 1000)
//            .setRetrySleepTime(3000)
//            .setRetryTimes(3);
//
//    @Override
//    public Site getSite() {
//        return site;
//    }
//
//    //开启定时任务，程序运行后间隔3秒执行爬虫程序，每隔100秒再次爬取数据，以便更新数据
//    @Scheduled(initialDelay = 300, fixedDelay = 100)
//    public void process() {
//        Spider.create(new MyTask())
//                .addUrl(url)
//                //设置线程数为10
//                .thread(10)
//                //利用布隆过滤器设置网页去重
//                .setScheduler(new QueueScheduler()
//                        .setDuplicateRemover(new BloomFilterDuplicateRemover(1000000)))
//                //通过设置自定义pipeline将爬取到的数据存入mysql中
//                .addPipeline(myPipeLine)
//                .run();
//    }
//}
