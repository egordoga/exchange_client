package ua.exchange_client.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(new Jackson2HalModule()).build();
        return new RestTemplate(Collections.singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
    }
}
