package cronapi.chatbot;

import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.chatbot.methods.StartChatBot;
import cronapi.telegram.bots.SendOperations;
import cronapi.telegram.bots.methods.SendMessage;
import cronapi.telegram.bots.models.InlineKeyboardButton;
import cronapi.telegram.bots.models.InlineKeyboardMarkup;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@CronapiMetaData
public class ChatBotOperations {
    private static final Map<String, ChatBotSession> sessions = new HashMap<>();

    @CronapiMetaData
    public static String startChatBot(StartChatBot param)
    {
        String id = UUID.randomUUID().toString();
        ChatBotSession session = new ChatBotSession();
        session.setTelegramOptions(param.getTelegramOptions());
        session.setWatsonAssistantOptions(param.getWatsonAssistantOptions());

        synchronized (sessions) {
            sessions.put(id, session);
        }

        session.start();

        return id;
    }

    @CronapiMetaData
    public static void stopChatBot(String id)
    {
        ChatBotSession session;

        synchronized (sessions) {
            session = sessions.remove(id);
        }

        session.interrupt();
    }

    @CronapiMetaData
    public static void sendWatsonMessageToTelegram(Var watsonResponseIn, Var telegramChatIdIn, Var telegramTokenIn)
            throws Exception {

        MessageResponse watsonResponse = watsonResponseIn.getTypedObject(MessageResponse.class);
        String telegramChatId = telegramChatIdIn.toString();
        String telegramToken = telegramTokenIn.toString();

        SendMessage sendMessage = null;

        for (String outputText : watsonResponse.getOutput().getText()) {
            if (sendMessage != null)
                SendOperations.sendMessage(sendMessage);

            sendMessage = new SendMessage();
            sendMessage.setText(outputText);
            sendMessage.setToken(telegramToken);
            sendMessage.setChatId(telegramChatId);

        }

        if (sendMessage != null) {
            if (watsonResponse.getContext().containsKey("quick_reply")) {
                List<InlineKeyboardButton> inlineKeyboardRow = new ArrayList<>();

                List<Map<String, String>> quickReplyList = (List<Map<String, String>>) watsonResponse.getContext()
                        .get("quick_reply");
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

                watsonResponse.getContext().remove("quick_reply");
            }

            SendOperations.sendMessage(sendMessage);
        }

        //blockly.TelegramUpdatesHandler.Executar(update);
        //new JsonParser().parse()
    }
}
