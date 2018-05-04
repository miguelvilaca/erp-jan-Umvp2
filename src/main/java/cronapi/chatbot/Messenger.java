package cronapi.chatbot;

import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import cronapi.chatbot.elements.Carousel;
import cronapi.chatbot.elements.Location;
import cronapi.chatbot.elements.QuickReply;

import java.io.IOException;
import java.util.List;

import static cronapi.chatbot.ChatBotConfiguration.*;

public interface Messenger {

    String getPlatform();

    default void sendWatsonMessage(String conversationId, MessageResponse messageResponse) {
        List<String> outputTextList = messageResponse.getOutput().getText();
        for (int i = 0; i < outputTextList.size(); i++) {
            String outputText = outputTextList.get(i);
            if (i == outputTextList.size() - 1
                    && (messageResponse.getContext().containsKey("location")
                    || messageResponse.getContext().containsKey("carousel")
                    || messageResponse.getContext().containsKey("quick_reply"))) {
                try {
                    if (messageResponse.getContext().containsKey("location")) {
                        String locationJson = OBJECT_MAPPER.writeValueAsString(messageResponse.getContext().get("location"));
                        WatsonAssistant.removeFromContext(messageResponse, "location");
                        Location location = OBJECT_MAPPER.readValue(locationJson, Location.class);
                        sendLocation(conversationId, location);
                    }

                    if (messageResponse.getContext().containsKey("carousel")) {
                        String carousel = OBJECT_MAPPER.writeValueAsString(messageResponse.getContext().get("carousel"));
                        WatsonAssistant.removeFromContext(messageResponse, "carousel");
                        List<Carousel> carouselList = OBJECT_MAPPER.readValue(carousel, TYPE_LIST_CAROUSEL);
                        sendCarousel(conversationId, carouselList);
                    }

                    if (messageResponse.getContext().containsKey("quick_reply")) {
                        String quickReply = OBJECT_MAPPER.writeValueAsString(messageResponse.getContext().get("quick_reply"));
                        WatsonAssistant.removeFromContext(messageResponse, "quick_reply");
                        List<QuickReply> quickReplyList = OBJECT_MAPPER.readValue(quickReply, TYPE_LIST_QUICKREPLY);
                        if (quickReplyList.isEmpty()) {
                            sendTextMessage(conversationId, outputText);
                        } else {
                            sendQuickReply(conversationId, outputText, quickReplyList);
                        }
                    } else {
                        sendTextMessage(conversationId, outputText);
                    }
                } catch (IOException e) {
                    throw new ChatBotException(e);
                }
            } else {
                sendTextMessage(conversationId, outputText);
            }
        }
    }

    void sendLocation(String conversationId, Location location);

    void sendCarousel(String conversationId, List<Carousel> carousel);

    void sendQuickReply(String conversationId, String text, List<QuickReply> quickReply);

    void sendTextMessage(String conversationId, String text);
}
