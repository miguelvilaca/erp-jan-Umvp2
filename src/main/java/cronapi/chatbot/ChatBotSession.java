//package cronapi.chatbot;
//
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.PropertyNamingStrategy;
//import com.google.gson.Gson;
//import com.ibm.watson.developer_cloud.conversation.v1.model.Context;
//import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
//import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
//import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
//import cronapi.Var;
//import cronapi.chatbot.methods.FacebookOptions;
//import cronapi.chatbot.methods.TelegramOptions;
//import cronapi.chatbot.methods.WatsonAssistantOptions;
//import cronapi.watson.conversation.ConversationOperations;
//import org.h2.jdbcx.JdbcConnectionPool;
//
//import java.io.IOException;
//import java.lang.reflect.Method;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//public class ChatBotSession extends Thread {
//    private static final Logger LOGGER = Logger.getLogger(ChatBotSession.class.getName());
//    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//    private static final String CONNECTION_STRING = "jdbc:h2:~/chatbot.h2;MVCC=TRUE";
//    private static final String PLATFORM_TELEGRAM = "telegram";
//    private static final String DDL_SESSION = "CREATE TABLE IF NOT EXISTS SESSION\n" +
//            "(\n" +
//            "    ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,\n" +
//            "    WORKSPACE VARCHAR NOT NULL,\n" +
//            "    PLATFORM VARCHAR NOT NULL,\n" +
//            "    CHAT VARCHAR NOT NULL,\n" +
//            "    CONTEXT VARCHAR NOT NULL,\n" +
//            ")";
//    private static final String COLUMN_ID = "ID";
//    private static final String COLUMN_WORKSPACE = "WORKSPACE";
//    private static final String COLUMN_PLATFORM = "PLATFORM";
//    private static final String COLUMN_CHAT = "CHAT";
//    private static final String COLUMN_CONTEXT = "CONTEXT";
//    private static final String QRY_SESSION = "SELECT ID, CONTEXT FROM SESSION " +
//            "WHERE WORKSPACE = ? AND PLATFORM = ? AND CHAT = ? " +
//            "FOR UPDATE";
//    private static final String INS_SESSION = "INSERT INTO SESSION(WORKSPACE, PLATFORM, CHAT, CONTEXT) " +
//            "VALUES (?, ?, ?, ?)";
//    private static final String UPD_SESSION = "UPDATE SESSION SET CONTEXT = ? WHERE ID = ?";
//
//    private static final JdbcConnectionPool CONNECTION_POOL;
//
//    static {
//        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        OBJECT_MAPPER.setVisibility(OBJECT_MAPPER.getSerializationConfig()
//                .getDefaultVisibilityChecker()
//                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
//                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
//                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
//                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
//        OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SnakeCaseStrategy.SNAKE_CASE);
//
//        try {
//            Class.forName("org.h2.Driver");
//            CONNECTION_POOL = JdbcConnectionPool.create(CONNECTION_STRING, "sa", "sa");
//            CONNECTION_POOL.setMaxConnections(256);
//            try (Connection connection = getConnection()) {
//                try (PreparedStatement statement = connection.prepareStatement(DDL_SESSION)) {
//                    statement.execute();
//                }
//            }
//        } catch (ClassNotFoundException | SQLException e) {
//            throw new ChatBotException(e);
//        }
//    }
//
//    private final ExecutorService executor = Executors.newCachedThreadPool();
//    private TelegramOptions telegramOptions;
//    private WatsonAssistantOptions watsonAssistantOptions;
//    private FacebookOptions facebookOptions;
//    private GetUpdates getUpdates = new GetUpdates();
//    private int lastReceivedUpdate;
//
//    private static Connection getConnection() {
//        try {
//            return CONNECTION_POOL.getConnection();
//        } catch (SQLException e) {
//            throw new ChatBotException(e);
//        }
//    }
//
//    private static Context getContext(String workspace, String platform, String chat) {
//        try (Connection connection = getConnection()) {
//            try (PreparedStatement statement = connection.prepareStatement(QRY_SESSION)) {
//                statement.setString(1, workspace);
//                statement.setString(2, platform);
//                statement.setString(3, chat);
//                try (ResultSet resultSet = statement.executeQuery()) {
//                    if (resultSet.next()) {
//                        return OBJECT_MAPPER.readValue(resultSet.getString(COLUMN_CONTEXT), Context.class);
//                    }
//                }
//            }
//            return null;
//        } catch (SQLException | IOException e) {
//            throw new ChatBotException(e);
//        }
//    }
//
//    private static void updateContext(String workspace, String platform, String chat, Context context) {
//
//        try (Connection connection = getConnection()) {
//            Integer id = null;
//            String contextString = OBJECT_MAPPER.writeValueAsString(context);
//            try (PreparedStatement statement = connection.prepareStatement(QRY_SESSION)) {
//                statement.setString(1, workspace);
//                statement.setString(2, platform);
//                statement.setString(3, chat);
//                try (ResultSet resultSet = statement.executeQuery()) {
//                    if (resultSet.next()) {
//                        id = resultSet.getInt(COLUMN_ID);
//                    }
//                }
//            }
//
//            if (id == null) {
//                try (PreparedStatement statement = connection.prepareStatement(INS_SESSION)) {
//                    statement.setString(1, workspace);
//                    statement.setString(2, platform);
//                    statement.setString(3, chat);
//                    statement.setString(4, contextString);
//                    statement.execute();
//                }
//            } else {
//                try (PreparedStatement statement = connection.prepareStatement(UPD_SESSION)) {
//                    statement.setString(1, contextString);
//                    statement.setInt(2, id);
//                    statement.execute();
//                }
//            }
//        } catch (SQLException | IOException e) {
//            throw new ChatBotException(e);
//        }
//    }
//
//    public TelegramOptions getTelegramOptions() {
//        return telegramOptions;
//    }
//
//    public void setTelegramOptions(TelegramOptions telegramOptions) {
//        this.telegramOptions = telegramOptions;
//    }
//
//    public WatsonAssistantOptions getWatsonAssistantOptions() {
//        return watsonAssistantOptions;
//    }
//
//    public void setWatsonAssistantOptions(WatsonAssistantOptions watsonAssistantOptions) {
//        this.watsonAssistantOptions = watsonAssistantOptions;
//    }
//
//    public FacebookOptions getFacebookOptions() {
//        return facebookOptions;
//    }
//
//    public void setFacebookOptions(FacebookOptions facebookOptions) {
//        this.facebookOptions = facebookOptions;
//    }
//
//    @Override
//    public void run() {
//        setPriority(Thread.MIN_PRIORITY);
//        if (telegramOptions != null)
//            executor.execute(this::runTelegramBot);
////        if (facebookOptions != null)
////            executor.execute(this::runFacebookBot);
//        try {
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
//        } catch (InterruptedException e) {
//            interrupt();
//        }
//    }
//
////    private void runFacebookBot() {
////        Messenger messenger = Messenger.create(
////                facebookOptions.getPageAccessToken(),
////                facebookOptions.getAppSecret(),
////                facebookOptions.getVerifyToken()
////        );
////
////        try {
////            messenger.verifyWebhook("subscribe", facebookOptions.getVerifyToken());
////        } catch (MessengerVerificationException e) {
////            e.printStackTrace();
////        }
////    }
//
//    private void runTelegramBot() {
//        getUpdates = new GetUpdates();
//        getUpdates.setToken(telegramOptions.getBotToken());
//        while (!executor.isShutdown()) {
//            getUpdates.setOffset(lastReceivedUpdate + 1);
//            List<Update> updates = doGetUpdates();
//            updates.removeIf(x -> x.getUpdateId() < lastReceivedUpdate);
//            lastReceivedUpdate = updates.parallelStream()
//                    .map(Update::getUpdateId)
//                    .max(Integer::compareTo)
//                    .orElse(0);
//
//            updates.forEach(update -> executor.execute(() -> {
//                try {
//                    handleTelegramMessage(update);
//                } catch (Exception e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                }
//            }));
//        }
//    }
//
//    private List<Update> doGetUpdates() {
//        List<Update> updates;
//        try {
//            updates = getUpdates.execute();
//        } catch (IOException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//            updates = new ArrayList<>();
//        }
//        return updates;
//    }
//
//    private void removeFromContext(MessageResponse watsonMessage, String key) {
//        Context context = watsonMessage.getContext();
//        context.remove(key);
//        watsonMessage.setContext(context);
//    }
//
//    private void handleTelegramMessage(Update update) throws Exception {
//        String chat = null;
//        String message = null;
//
//        if (update.getCallbackQuery() != null
//                && update.getCallbackQuery().getMessage() != null) {
//            message = update.getCallbackQuery().getData();
//            chat = String.valueOf(update.getCallbackQuery().getMessage().getChat().getId());
//        }
//        if (chat == null)
//            chat = String.valueOf(update.getMessage().getChat().getId());
//        if (message == null)
//            message = update.getMessage().getText();
//
//        Map incomingContext = null;
//
//        if (message.startsWith("{"))
//        {
//            Map incomingMap = new Gson().fromJson(message, Map.class);
//            if (incomingMap.containsKey("context"))
//            {
//                incomingContext = (Map) incomingMap.get("context");
//            }
//
//            if (incomingMap.containsKey("message"))
//            {
//                message = incomingMap.get("message").toString();
//            }
//        }
//
//        InputData input = new InputData.Builder()
//                .text(message)
//                .build();
//
//        MessageOptions watsonMessageOptions = new MessageOptions.Builder()
//                .context(getContext(watsonAssistantOptions.getWorkspaceId(), PLATFORM_TELEGRAM, chat))
//                .workspaceId(watsonAssistantOptions.getWorkspaceId())
//                .input(input)
//                .build();
//
//        if (incomingContext != null)
//        {
//            for (Object incomingContextKey : incomingContext.keySet())
//            {
//                watsonMessageOptions.context().put(incomingContextKey.toString(),
//                        incomingContext.get(incomingContextKey));
//            }
//        }
//
//        MessageResponse watsonMessage = ConversationOperations.message("2017-05-26", //watsonAssistantOptions.getVersionDate(),
//                watsonAssistantOptions.getUserName(),
//                watsonAssistantOptions.getPassword(),
//                null,
//                null,
//                watsonMessageOptions);
//
//        watsonToTelegram(watsonMessage, chat);
//
//        if (watsonMessage.getContext().containsKey("blockly")) {
//            try {
//                String blocklyClassName = watsonMessage.getContext().get("blockly").toString();
//                Class blocklyClass = Class.forName(blocklyClassName);
//                Method executeMethod = blocklyClass.getMethod("Executar", Var.class);
//                executeMethod.invoke(null, Var.valueOf(watsonMessage));
//
//                removeFromContext(watsonMessage, "blockly");
//
//                watsonMessageOptions = new MessageOptions.Builder()
//                        .context(watsonMessage.getContext())
//                        .workspaceId(watsonAssistantOptions.getWorkspaceId())
//                        .input(new InputData.Builder().text(blocklyClassName + ".Done").build())
//                        .build();
//
//                watsonMessage = ConversationOperations.message("2017-05-26", //watsonAssistantOptions.getVersionDate(),
//                        watsonAssistantOptions.getUserName(),
//                        watsonAssistantOptions.getPassword(),
//                        null,
//                        null,
//                        watsonMessageOptions);
//            } catch (Exception e) {
//                LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                for (String contextKey : watsonMessage.getContext().keySet()) {
//                    removeFromContext(watsonMessage, contextKey);
//                }
//
//                watsonMessageOptions = new MessageOptions.Builder()
//                        .context(watsonMessage.getContext())
//                        .workspaceId(watsonAssistantOptions.getWorkspaceId())
//                        .input(new InputData.Builder().text("8dd7fb88-bb44-4ec3-9524-193bc11ad008").build())
//                        .build();
//                watsonMessage = ConversationOperations.message("2017-05-26", //watsonAssistantOptions.getVersionDate(),
//                        watsonAssistantOptions.getUserName(),
//                        watsonAssistantOptions.getPassword(),
//                        null,
//                        null,
//                        watsonMessageOptions);
//            }
//
//            watsonToTelegram(watsonMessage, chat);
//        }
//
//        updateContext(watsonAssistantOptions.getWorkspaceId(), PLATFORM_TELEGRAM, chat, watsonMessage.getContext());
//    }
//
//    private InlineKeyboardMarkup quickReplyToInlineKeyboardMarkup(List<Map> quickReplyList) {
//        List<InlineKeyboardButton> inlineKeyboardRow = new ArrayList<>();
//
//        for (Map quickReply : quickReplyList) {
//            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
//            inlineKeyboardButton.setText(quickReply.get("text").toString());
//            if (quickReply.containsKey("data")) {
//                Object data = quickReply.get("data");
//                if (data instanceof String)
//                    inlineKeyboardButton.setCallbackData(data.toString());
//                else
//                    inlineKeyboardButton.setCallbackData(new Gson().toJson(data));
//            }
//            else if (quickReply.containsKey("url"))
//                inlineKeyboardButton.setUrl(quickReply.get("url").toString());
//            inlineKeyboardRow.add(inlineKeyboardButton);
//        }
//
//        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
//        inlineKeyboard.add(inlineKeyboardRow);
//        replyMarkup.setInlineKeyboard(inlineKeyboard);
//        return replyMarkup;
//    }
//
//    private void watsonToTelegram(MessageResponse watsonMessage, String chat) throws IOException {
//        SendMessage sendMessage = null;
//
//        for (String outputText : watsonMessage.getOutput().getText()) {
//            if (sendMessage != null)
//                SendOperations.sendMessage(sendMessage);
//
//            sendMessage = new SendMessage();
//            sendMessage.setText(outputText);
//            sendMessage.setToken(telegramOptions.getBotToken());
//            sendMessage.setChatId(chat);
//        }
//
//        if (sendMessage != null) {
//            if (watsonMessage.getContext().containsKey("quick_reply")) {
//                List<Map> quickReply = (List<Map>) watsonMessage.getContext()
//                        .get("quick_reply");
//                InlineKeyboardMarkup replyMarkup = quickReplyToInlineKeyboardMarkup(quickReply);
//                sendMessage.setReplyMarkup(replyMarkup);
//
//                removeFromContext(watsonMessage, "quick_reply");
//            }
//            SendOperations.sendMessage(sendMessage);
//        }
//
//        if (watsonMessage.getContext().containsKey("carousel")) {
//            List<Map> carouselList = (List<Map>) watsonMessage.getContext()
//                    .get("carousel");
//            for (Map<String, Object> carousel : carouselList) {
//                String image = carousel.get("image").toString();
//                String message = carousel.get("message").toString();
//                //TODO Usar file_id
//                SendPhoto sendPhoto = new SendPhoto();
//                sendPhoto.setToken(telegramOptions.getBotToken());
//                sendPhoto.setChatId(chat);
//                sendPhoto.setCaption(message);
//                sendPhoto.setPhoto(image);
//
//                if (carousel.containsKey("quick_reply")) {
//                    List<Map> quickReply = (List<Map>) carousel.get("quick_reply");
//                    sendPhoto.setReplyMarkup(quickReplyToInlineKeyboardMarkup(quickReply));
//                }
//
//                sendPhoto.execute();
//            }
//            removeFromContext(watsonMessage, "carousel");
//        }
//
//        if (watsonMessage.getContext().containsKey("location")) {
//            Map location = (Map) watsonMessage.getContext().get("location");
//
//            SendLocation
//                    sendLocation = new SendLocation();
//            sendLocation.setToken(telegramOptions.getBotToken());
//            sendLocation.setChatId(chat);
//            sendLocation.setLatitude(Float.valueOf(location.get("latitude").toString()));
//            sendLocation.setLongitude(Float.valueOf(location.get("longitude").toString()));
//
//            sendLocation.execute();
//
//            removeFromContext(watsonMessage, "location");
//        }
//    }
//}
