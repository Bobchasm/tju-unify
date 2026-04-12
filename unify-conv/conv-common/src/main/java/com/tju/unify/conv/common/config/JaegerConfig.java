//package config;
//
//import brave.Tracing;
//import brave.sampler.Sampler;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import zipkin2.Span;
//import zipkin2.reporter.AsyncReporter;
//import zipkin2.reporter.Sender;
//import zipkin2.reporter.okhttp3.OkHttpSender;
//
//import jakarta.annotation.PostConstruct;
//
///**
// * Jaeger 追踪配置类
// * 配置 Brave Reporter 将追踪数据发送到 Jaeger Agent
// */
//@Configuration
//@ConditionalOnProperty(prefix = "tracing.jaeger", name = "enabled", havingValue = "true", matchIfMissing = true)
//@Slf4j
//public class JaegerConfig {
//
//    @Value("${spring.application.name:unknown-service}")
//    private String serviceName;
//
//    @Value("${tracing.jaeger.agent.host:123.57.102.69}")
//    private String jaegerAgentHost;
//
//    @Value("${tracing.jaeger.agent.port:14268}")
//    private int jaegerAgentPort;
//
//    @Value("${tracing.jaeger.sampler.type:const}")
//    private String samplerType;
//
//    @Value("${tracing.jaeger.sampler.param:1}")
//    private Number samplerParam;
//
//    @PostConstruct
//    public void configureJaeger() {
//        log.info("配置 Jaeger - 服务名: {}, Agent: {}:{}, 采样类型: {}, 采样参数: {}",
//                serviceName, jaegerAgentHost, jaegerAgentPort, samplerType, samplerParam);
//    }
//
//    @Bean
//    public Sender zipkinSender() {
//        // Jaeger Agent 兼容 Zipkin 格式，使用 HTTP 端口（默认 14268）
//        // 注意：Jaeger Agent UDP 端口是 6831，HTTP 端口是 14268
//        String endpoint = String.format("http://%s:%d/api/v2/spans", jaegerAgentHost, jaegerAgentPort);
//        log.info("配置 Zipkin Sender - 端点: {}", endpoint);
//        return OkHttpSender.newBuilder()
//                .endpoint(endpoint)
//                .encoding(zipkin2.codec.Encoding.JSON)
//                .build();
//    }
//
//    @Bean
//    public AsyncReporter<Span> zipkinReporter() {
//        return AsyncReporter.create(zipkinSender());
//    }
//
//    @Bean
//    public Sampler sampler() {
//        if ("const".equalsIgnoreCase(samplerType)) {
//            return samplerParam.intValue() == 1 ? Sampler.ALWAYS_SAMPLE : Sampler.NEVER_SAMPLE;
//        } else if ("probabilistic".equalsIgnoreCase(samplerType)) {
//            return Sampler.create(samplerParam.floatValue());
//        } else {
//            log.warn("未知的采样类型: {}, 使用默认 ALWAYS_SAMPLE", samplerType);
//            return Sampler.ALWAYS_SAMPLE;
//        }
//    }
//
//    @Bean
//    public Tracing braveTracing(AsyncReporter<Span> zipkinReporter) {
//        return Tracing.newBuilder()
//                .localServiceName(serviceName)
//                .sampler(sampler())
//                .spanReporter(zipkinReporter)  // 使用 spanReporter 连接 Zipkin Reporter
//                .build();
//    }
//
//    // Tracer 由 Micrometer Tracing 自动配置，不需要手动创建
//    // Micrometer 会自动使用上面配置的 Tracing
//}