package cdtu.mall.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "unit")
public class UnitConfig {

    private String api_key;
    private String secret_key;
    private String url;
}
