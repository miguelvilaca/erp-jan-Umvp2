package cronapi.chatbot;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.telegram.bots.TelegramBotOperations;
import cronapi.telegram.bots.methods.SendMessage;
import cronapi.telegram.bots.models.InlineKeyboardButton;
import cronapi.telegram.bots.models.InlineKeyboardMarkup;
import cronapi.telegram.bots.models.ReplyMarkup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CronapiMetaData
public class ChatBotOperations {

    @CronapiMetaData
    public static void sendWatsonMessageToTelegram(Var watsonResponseIn, Var telegramChatIdIn,
                                                   Var telegramTokenIn) throws IOException {

        MessageResponse watsonResponse = watsonResponseIn.getTypedObject(MessageResponse.class);
        String telegramChatId = telegramChatIdIn.toString();
        String telegramToken = telegramTokenIn.toString();

        SendMessage sendMessage = null;

        for (String outputText : watsonResponse.getOutput().getText()) {
            if (sendMessage != null)
                TelegramBotOperations.sendMessage(sendMessage);

            sendMessage = new SendMessage();
            sendMessage.setText(outputText);
            sendMessage.setToken(telegramToken);
            sendMessage.setChatId(telegramChatId);

        }

        if (sendMessage != null) {
            if (watsonResponse.getContext().containsKey("quick_reply")) {
                List<InlineKeyboardButton> inlineKeyboardRow = new ArrayList<>();

                List<Map<String, String>> quickReplyList =
                        (List<Map<String, String>>) watsonResponse.getContext().get("quick_reply");
                for (Map<String, String> quickReply : quickReplyList) {
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText(quickReply.get("text"));
                    inlineKeyboardButton.setCallbackData(quickReply.get("data"));
                    inlineKeyboardRow.add(inlineKeyboardButton);
                }

                InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
                inlineKeyboard.add(inlineKeyboardRow);
                replyMarkup.setInlineKeyboard(inlineKeyboard);
                sendMessage.setReplyMarkup(replyMarkup);
            }

            TelegramBotOperations.sendMessage(sendMessage);
        }
        //new JsonParser().parse()
    }
}
