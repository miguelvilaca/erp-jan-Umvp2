package cronapi.chatbot;

import com.google.gson.Gson;
import cronapi.chatbot.elements.Carousel;
import cronapi.chatbot.elements.Location;
import cronapi.chatbot.elements.QuickReply;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiConstants;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramMessenger extends DefaultAbsSender implements Messenger {
    private final String token;
    private final String url;

    @Autowired
    public TelegramMessenger(@Value("${chatbot.telegram.token}") final String token,
                             @Value("${chatbot.telegram.url}") final String url) {
        super(new DefaultBotOptions());
        this.token = token;
        this.url = url;
        setWebhook();
    }

    private void setWebhook() {
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
            String requestUrl = getOptions().getBaseUrl() + token + "/" + SetWebhook.PATH;

            HttpPost httppost = new HttpPost(requestUrl);
            httppost.setConfig(getOptions().getRequestConfig());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody(SetWebhook.URL_FIELD, url);

            HttpEntity multipart = builder.build();
            httppost.setEntity(multipart);
            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                HttpEntity ht = response.getEntity();
                BufferedHttpEntity buf = new BufferedHttpEntity(ht);
                String responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(responseContent);
                if (!jsonObject.getBoolean(ApiConstants.RESPONSE_FIELD_OK)) {
                    throw new ChatBotException("Error setting webhook");
                }
            }
        } catch (JSONException e) {
            throw new ChatBotException("Error deserializing setWebhook method response", e);
        } catch (IOException e) {
            throw new ChatBotException("Error executing setWebook method", e);
        }
    }

    private InlineKeyboardMarkup quickReplyToInlineKeyboardMarkup(List<QuickReply> quickReplyList) {
        List<InlineKeyboardButton> inlineKeyboardRow = new ArrayList<>();

        for (QuickReply quickReply : quickReplyList) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(quickReply.getText());
            Object data = quickReply.getData();
            String url = quickReply.getUrl();
            if (data != null) {
                if (data instanceof String)
                    inlineKeyboardButton.setCallbackData(data.toString());
                else
                    inlineKeyboardButton.setCallbackData(new Gson().toJson(data));
            } else if (url != null) {
                inlineKeyboardButton.setUrl(url);
            }
            inlineKeyboardRow.add(inlineKeyboardButton);
        }

        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
        inlineKeyboard.add(inlineKeyboardRow);
        replyMarkup.setKeyboard(inlineKeyboard);
        return replyMarkup;
    }

    @Override
    public String getPlatform() {
        return "Telegram";
    }

    @Override
    public void sendLocation(String conversationId, Location location) {
        try {
            SendLocation sendLocation = new SendLocation();
            sendLocation.setLatitude(location.getLatitude());
            sendLocation.setLongitude(location.getLongitude());
            sendLocation.setChatId(conversationId);
            execute(sendLocation);
        } catch (TelegramApiException e) {
            throw new ChatBotException(e);
        }
    }

    @Override
    public void sendCarousel(String conversationId, List<Carousel> carouselList) {
        try {
            for (Carousel carousel : carouselList) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(conversationId);
                sendPhoto.setCaption(carousel.getMessage());
                sendPhoto.setPhoto(carousel.getImage());

                List<QuickReply> quickReplyList = carousel.getQuickReply();

                if (quickReplyList != null) {
                    sendPhoto.setReplyMarkup(quickReplyToInlineKeyboardMarkup(quickReplyList));
                }

                sendPhoto(sendPhoto);
            }
        } catch (TelegramApiException e) {
            throw new ChatBotException(e);
        }
    }

    @Override
    public void sendQuickReply(String conversationId, String text, List<QuickReply> quickReply) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(text);
            sendMessage.setChatId(conversationId);
            sendMessage.setReplyMarkup(quickReplyToInlineKeyboardMarkup(quickReply));
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new ChatBotException(e);
        }
    }

    @Override
    public void sendTextMessage(String conversationId, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText(text);
            sendMessage.setChatId(conversationId);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new ChatBotException(e);
        }
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
