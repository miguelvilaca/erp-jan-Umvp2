package cronapi.chatbot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.messenger4j.Messenger;
import cronapi.chatbot.elements.Carousel;
import cronapi.chatbot.elements.QuickReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramWebhookBot;

import java.io.IOException;
import java.util.*;

@Configuration
@PropertySource(value = {"classpath:/META-INF/chatbot.json"}, factory = ChatBotConfiguration.JsonLoader.class)
public class ChatBotConfiguration {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final TypeReference<List<QuickReply>> TYPE_LIST_QUICKREPLY = new TypeReference<List<QuickReply>>() {
    };
    public static final TypeReference<List<Carousel>> TYPE_LIST_CAROUSEL = new TypeReference<List<Carousel>>() {
    };

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.setVisibility(OBJECT_MAPPER.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SnakeCaseStrategy.SNAKE_CASE);
    }

    @Bean
    public Messenger facebook(@Value("${chatbot.facebook.page}") String pageAccessToken,
                                                      @Value("${chatbot.facebook.app}") final String appSecret,
                                                      @Value("${chatbot.facebook.verify}") final String verifyToken) {
        return Messenger.create(pageAccessToken, appSecret, verifyToken);
    }

    public static class JsonLoader implements PropertySourceFactory {

        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(String s, EncodedResource resource) throws IOException {
            Map readValue = new ObjectMapper().readValue(resource.getInputStream(), Map.class);
            return new MapPropertySource("chatbot", flatten(readValue));

        }

        private final Map<String, Object> flatten(Map<String, Object> source) {
            Map<String, Object> result = new LinkedHashMap<>();
            flatten(result, source, null);
            return result;
        }

        private void flatten(Map<String, Object> result, Map<String, Object> source, String path) {
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                String key = entry.getKey();
                if (StringUtils.hasText(path)) {
                    if (key.startsWith("[")) {
                        key = path + key;
                    } else {
                        key = path + "." + key;
                    }
                }
                Object value = entry.getValue();
                if (value instanceof String) {
                    result.put(key, value);

                } else if (value instanceof Map) {
                    // Need a compound key
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) value;
                    flatten(result, map, key);

                } else if (value instanceof Collection) {
                    // Need a compound key
                    @SuppressWarnings("unchecked")
                    Collection<Object> collection = (Collection<Object>) value;
                    int count = 0;
                    for (Object object : collection) {
                        flatten(result,
                                Collections.singletonMap("[" + (count++) + "]", object),
                                key);
                    }

                } else {
                    result.put(key, value == null ? "" : value);
                }
            }
        }

    }


}
