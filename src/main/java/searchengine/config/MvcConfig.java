package searchengine.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import searchengine.utility.HandlerInterceptorLog;

@Configuration
@EnableWebMvc
@ComponentScan("searchengine.controllers")
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptorLog());
    }

    // Если значение null, то оно не присваивается, можно использовать аннотацию @JsonInclude(JsonInclude.Include.NON_NULL) к определенной переменной
//    @Override
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
//        converters.add(jsonConverter);
//    }



}
