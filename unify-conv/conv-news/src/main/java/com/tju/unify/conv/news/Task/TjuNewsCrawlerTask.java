package com.tju.unify.conv.news.Task;

import com.tju.unify.conv.news.config.SchoolNewsConfig;
import com.tju.unify.conv.news.pojo.SchoolNews;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Html;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Slf4j
@Component
public class TjuNewsCrawlerTask implements PageProcessor {

    @Autowired
    private MyPipeLine myPipeLine;

    @Autowired
    private SchoolNewsConfig schoolNewsConfig;

    @Value("${tju.news.crawler.enabled:true}")
    private boolean crawlerEnabled;

    private final Site site = Site.me()
            .setCharset("utf-8")
            .setDomain("news.tju.edu.cn")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .setTimeOut(30_000)
            .setRetrySleepTime(3000)
            .setRetryTimes(3);


    private static final Map<String, Long> CATEGORY_FLAG = new HashMap<>();

    static {
        CATEGORY_FLAG.put("综合新闻", 1L);
        CATEGORY_FLAG.put("学术科研", 2L);
        CATEGORY_FLAG.put("人才培养", 3L);
        CATEGORY_FLAG.put("校园时讯", 4L);
        CATEGORY_FLAG.put("新思想", 5L);
        CATEGORY_FLAG.put("封面新闻", 6L);
    }

    @Override
    public void process(Page page) {
        String requestUrl = page.getRequest().getUrl();
        if (requestUrl.contains("/info/") && requestUrl.endsWith(".htm")) {
            parseDetail(page);
            return;
        }
        parseList(page);
    }

    private void parseList(Page page) {
        List<String> hrefs = page.getHtml().xpath("//ul[@class='nylist-b']//h4/a[@class='l2']/@href").all();
        if (hrefs.isEmpty()) {
            hrefs = page.getHtml().xpath("//ul[@class='nylist-b']//a[@class='l2']/@href").all();
        }
        String baseUrl = page.getUrl().toString();
        for (String href : hrefs) {
            if (StringUtils.isBlank(href)) {
                continue;
            }
            try {
                String full = URI.create(baseUrl).resolve(href.trim()).toString();
                if (full.contains("/info/") && full.startsWith("http")) {
                    page.addTargetRequest(full);
                }
            } catch (IllegalArgumentException e) {
                log.warn("跳过无效链接: {} (基准: {})", href, baseUrl);
            }
        }
    }

    private void parseDetail(Page page) {
        try {
            Html html = page.getHtml();
            String title = html.xpath("//div[@class='arc-tit']/h1/text()").toString();
            if (StringUtils.isBlank(title)) {
                title = html.xpath("//div[@class='arc-tit']/h1/allText()").toString();
            }
            if (StringUtils.isBlank(title)) {
                page.setSkip(true);
                return;
            }
            String timeStr = html.xpath("//div[@class='arc-info']//time/text()").toString();
            if (timeStr != null) {
                timeStr = timeStr.trim();
            }
            String categoryName = html.xpath("//div[@class='ny-ba']//h3/text()").toString();
            if (categoryName != null) {
                categoryName = categoryName.trim().replaceAll("\\s+", "");
            }
            Long flag = CATEGORY_FLAG.getOrDefault(categoryName, 1L);

            String origin = "";
            List<String> infoSpans = html.xpath("//div[@class='arc-info']//span/text()").all();
            for (String span : infoSpans) {
                if (span != null && span.contains("来源")) {
                    origin = span.replaceFirst("^来源[：:]\\s*", "").trim();
                    break;
                }
            }

            List<String> texts = html.xpath("//div[@class='v_news_content']//p//text()").all();
            if (texts.isEmpty()) {
                texts = html.xpath("//div[@id='vsb_content_500']//p//text()").all();
            }
            String content = texts.stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.joining("\n"));

            SchoolNews schoolNews = new SchoolNews();
            schoolNews.setUrl(page.getRequest().getUrl());
            schoolNews.setTitle(title.trim());
            schoolNews.setTime(timeStr != null ? timeStr : "");
            schoolNews.setFlag(flag);
            schoolNews.setOrigin(origin);
            schoolNews.setUnit("天津大学新闻网");
            schoolNews.setContent(content);

            page.putField("snewsInfo", schoolNews);
        } catch (Exception e) {
            log.warn("解析详情页失败，已跳过: {}", page.getRequest().getUrl(), e);
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }


    public void triggerCrawler() {
        if (!crawlerEnabled || schoolNewsConfig.getCategory() == null || schoolNewsConfig.getCategory().isEmpty()) {
            log.warn("爬虫未启用或分类配置为空，跳过爬取");
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                String[] seeds = schoolNewsConfig.getCategory().toArray(new String[0]);
                Spider.create(this)
                        .addUrl(seeds)
                        .thread(5)
                        .setScheduler(new QueueScheduler())
                        .addPipeline(myPipeLine)
                        .run();
                log.info("天津大学新闻爬虫执行完成");
            } catch (Exception e) {
                log.error("天津大学新闻爬虫执行失败", e);
            }
        });
    }
}
