package cronapi.chatbot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.message.TemplateMessage;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.message.quickreply.TextQuickReply;
import com.github.messenger4j.send.message.template.GenericTemplate;
import com.github.messenger4j.send.message.template.button.Button;
import com.github.messenger4j.send.message.template.button.PostbackButton;
import com.github.messenger4j.send.message.template.button.UrlButton;
import com.github.messenger4j.send.message.template.common.DefaultAction;
import com.github.messenger4j.send.message.template.common.Element;
import cronapi.chatbot.elements.Carousel;
import cronapi.chatbot.elements.Location;
import cronapi.chatbot.elements.QuickReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Component
public class FacebookMessenger implements cronapi.chatbot.Messenger {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    private final Messenger messenger;

    @Autowired
    public FacebookMessenger(final Messenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public String getPlatform() {
        return "Facebook";
    }

    @Override
    public void sendLocation(String conversationId, Location location) {
        List<Element> elements = new ArrayList<>();
        String title = location.getTitle();
        if (title == null)
            title = "Localização";
        String latitude = location.getLatitude().toString();
        String longitude = location.getLongitude().toString();
        String image = "https://maps.googleapis.com/maps/api/staticmap?size=764x400&center="
                + latitude + "," + longitude + "&zoom=25&markers=" + latitude + "," + longitude;
        String item = "http://maps.apple.com/maps?q=" + latitude + "," + longitude + "&z=16";
        try {
            elements.add(Element.create(
                    title,
                    empty(),
                    of(new URL(image)),
                    of(DefaultAction.create(new URL(item))),
                    empty()
            ));
            GenericTemplate genericTemplate = GenericTemplate.create(elements);
            TemplateMessage templateMessage = TemplateMessage.create(genericTemplate);
            this.messenger.send(MessagePayload.create(conversationId, MessagingType.RESPONSE, templateMessage));
        } catch (MalformedURLException | MessengerIOException | MessengerApiException e) {
            throw new ChatBotException(e);
        }
    }

    @Override
    public void sendCarousel(String conversationId, List<Carousel> carouselList) {
        try {
            List<Element> elements = new ArrayList<>();
            for (Carousel carousel : carouselList) {
                List<Button> buttons = new ArrayList<>();
                for (QuickReply quickReply : carousel.getQuickReply()) {
                    if (quickReply.getUrl() != null) {
                        buttons.add(UrlButton.create(quickReply.getText(), new URL(quickReply.getUrl())));
                    } else {
                        Object data = quickReply.getData();
                        String payload;

                        if (data instanceof String)
                            payload = data.toString();
                        else
                            payload = OBJECT_MAPPER.writeValueAsString(data);

                        buttons.add(PostbackButton.create(quickReply.getText(), payload));
                    }
                    if (buttons.size() == 3) break;
                }

                elements.add(Element.create(
                        carousel.getMessage(),
                        empty(),
                        of(new URL(carousel.getImage())),
                        empty(),
                        of(buttons)
                ));
            }
            GenericTemplate genericTemplate = GenericTemplate.create(elements);
            TemplateMessage templateMessage = TemplateMessage.create(genericTemplate);
            this.messenger.send(MessagePayload.create(conversationId, MessagingType.RESPONSE, templateMessage));
        } catch (MalformedURLException | MessengerIOException | MessengerApiException | JsonProcessingException e) {
            throw new ChatBotException(e);
        }
    }

    @Override
    public void sendQuickReply(String conversationId, String text, List<QuickReply> quickReplyList) {
        try {
            List<com.github.messenger4j.send.message.quickreply.QuickReply> facebookQuickReplyList = new ArrayList<>();

            for (QuickReply quickReply : quickReplyList) {
                Object data = quickReply.getData();
                String payload;

                if (data instanceof String)
                    payload = data.toString();
                else
                    payload = OBJECT_MAPPER.writeValueAsString(data);
                facebookQuickReplyList.add(TextQuickReply.create(quickReply.getText(), payload));
            }

            TextMessage message = TextMessage.create(text, of(facebookQuickReplyList), empty());

            messenger.send(MessagePayload.create(conversationId, MessagingType.RESPONSE, message));
        } catch (MessengerApiException | MessengerIOException | JsonProcessingException e) {
            throw new ChatBotException(e);
        }
    }

    @Override
    public void sendTextMessage(String conversationId, String text) {
        try {
            final TextMessage textMessage = TextMessage.create(text);
            final MessagePayload messagePayload = MessagePayload.create(conversationId, MessagingType.RESPONSE, textMessage);
            this.messenger.send(messagePayload);
        } catch (MessengerApiException | MessengerIOException e) {
            throw new ChatBotException(e);
        }
    }
}
