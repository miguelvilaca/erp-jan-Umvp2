package cronapi.chatbot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.ibm.watson.developer_cloud.conversation.v1.model.Context;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import cronapi.Var;
import cronapi.watson.conversation.ConversationOperations;
import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class WatsonAssistant {
    private static final Logger LOGGER = Logger.getLogger(WatsonAssistant.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONNECTION_STRING = "jdbc:h2:~/chatbot.h2;MVCC=TRUE";
    private static final String DDL_SESSION = "CREATE TABLE IF NOT EXISTS SESSION\n" +
            "(\n" +
            "    ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,\n" +
            "    WORKSPACE VARCHAR NOT NULL,\n" +
            "    PLATFORM VARCHAR NOT NULL,\n" +
            "    CHAT VARCHAR NOT NULL,\n" +
            "    CONTEXT VARCHAR NOT NULL,\n" +
            ")";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_WORKSPACE = "WORKSPACE";
    private static final String COLUMN_PLATFORM = "PLATFORM";
    private static final String COLUMN_CHAT = "CHAT";
    private static final String COLUMN_CONTEXT = "CONTEXT";
    private static final String QRY_SESSION = "SELECT ID, CONTEXT FROM SESSION " +
            "WHERE WORKSPACE = ? AND PLATFORM = ? AND CHAT = ? " +
            "FOR UPDATE";
    private static final String INS_SESSION = "INSERT INTO SESSION(WORKSPACE, PLATFORM, CHAT, CONTEXT) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPD_SESSION = "UPDATE SESSION SET CONTEXT = ? WHERE ID = ?";

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.setVisibility(OBJECT_MAPPER.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SnakeCaseStrategy.SNAKE_CASE);

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new ChatBotException(e);
        }
    }

    private final JdbcConnectionPool connectionPool;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final String workspace;
    private final String username;
    private final String password;
    private final String error;

    public WatsonAssistant(@Value("${chatbot.watson.workspace}") final String workspace,
                           @Value("${chatbot.watson.username}") final String username,
                           @Value("${chatbot.watson.password}") final String password,
                           @Value("${chatbot.watson.error}") final String error) {

        this.workspace = workspace;
        this.username = username;
        this.password = password;
        this.error = error;

        this.connectionPool = JdbcConnectionPool.create(CONNECTION_STRING, "sa", "sa");
        this.connectionPool.setMaxConnections(256);
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(DDL_SESSION)) {
                statement.execute();
            }
        } catch (SQLException e) {
            throw new ChatBotException(e);
        }
    }

    public static void removeFromContext(MessageResponse watsonMessage, String key) {
        Context context = watsonMessage.getContext();
        context.remove(key);
        watsonMessage.setContext(context);
    }

    private Connection getConnection() {
        try {
            return connectionPool.getConnection();
        } catch (SQLException e) {
            throw new ChatBotException(e);
        }
    }

    private Context getContext(String workspace, String platform, String chat) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QRY_SESSION)) {
                statement.setString(1, workspace);
                statement.setString(2, platform);
                statement.setString(3, chat);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return OBJECT_MAPPER.readValue(resultSet.getString(COLUMN_CONTEXT), Context.class);
                    }
                }
            }
            return null;
        } catch (SQLException | IOException e) {
            throw new ChatBotException(e);
        }
    }

    private void updateContext(String workspace, String platform, String chat, Context context) {

        try (Connection connection = getConnection()) {
            Integer id = null;
            String contextString = OBJECT_MAPPER.writeValueAsString(context);
            try (PreparedStatement statement = connection.prepareStatement(QRY_SESSION)) {
                statement.setString(1, workspace);
                statement.setString(2, platform);
                statement.setString(3, chat);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        id = resultSet.getInt(COLUMN_ID);
                    }
                }
            }

            if (id == null) {
                try (PreparedStatement statement = connection.prepareStatement(INS_SESSION)) {
                    statement.setString(1, workspace);
                    statement.setString(2, platform);
                    statement.setString(3, chat);
                    statement.setString(4, contextString);
                    statement.execute();
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(UPD_SESSION)) {
                    statement.setString(1, contextString);
                    statement.setInt(2, id);
                    statement.execute();
                }
            }
        } catch (SQLException | IOException e) {
            throw new ChatBotException(e);
        }
    }

    public void handleTextMessage(String conversationId, String text, Messenger messenger) {
        executor.execute(() -> {
            Map incomingContext = null;
            String inputText;

            if (text.startsWith("{")) {
                try {
                    Map incomingMap = OBJECT_MAPPER.readValue(text, Map.class);
                    if (incomingMap.containsKey("context")) {
                        incomingContext = (Map) incomingMap.get("context");
                    }

                    if (incomingMap.containsKey("message")) {
                        inputText = incomingMap.get("message").toString();
                    } else {
                        inputText = text;
                    }
                } catch (IOException e) {
                    throw new ChatBotException(e);
                }
            } else {
                inputText = text;
            }

            InputData input = new InputData.Builder()
                    .text(inputText)
                    .build();

            MessageOptions watsonMessageOptions = new MessageOptions.Builder()
                    .context(getContext(workspace, messenger.getPlatform(), conversationId))
                    .workspaceId(workspace)
                    .input(input)
                    .build();

            if (incomingContext != null) {
                for (Object incomingContextKey : incomingContext.keySet()) {
                    watsonMessageOptions.context().put(incomingContextKey.toString(),
                            incomingContext.get(incomingContextKey));
                }
            }

            MessageResponse watsonMessage = ConversationOperations.message("2017-05-26", //watsonAssistantOptions.getVersionDate(),
                    username,
                    password,
                    null,
                    null,
                    watsonMessageOptions);

            messenger.sendWatsonMessage(conversationId, watsonMessage);

            if (watsonMessage.getContext().containsKey("blockly")) {
                try {
                    String blocklyClassName = watsonMessage.getContext().get("blockly").toString();
                    Class blocklyClass = Class.forName(blocklyClassName);
                    Method executeMethod = blocklyClass.getMethod("Executar", Var.class);
                    executeMethod.invoke(null, Var.valueOf(watsonMessage));

                    removeFromContext(watsonMessage, "blockly");

                    watsonMessageOptions = new MessageOptions.Builder()
                            .context(watsonMessage.getContext())
                            .workspaceId(workspace)
                            .input(new InputData.Builder().text(blocklyClassName + ".Done").build())
                            .build();

                    watsonMessage = ConversationOperations.message("2017-05-26",
                            username,
                            password,
                            null,
                            null,
                            watsonMessageOptions);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    for (String contextKey : watsonMessage.getContext().keySet()) {
                        removeFromContext(watsonMessage, contextKey);
                    }

                    watsonMessageOptions = new MessageOptions.Builder()
                            .context(watsonMessage.getContext())
                            .workspaceId(workspace)
                            .input(new InputData.Builder().text(error).build())
                            .build();

                    watsonMessage = ConversationOperations.message("2017-05-26", //watsonAssistantOptions.getVersionDate(),
                            username,
                            password,
                            null,
                            null,
                            watsonMessageOptions);
                }

                messenger.sendWatsonMessage(conversationId, watsonMessage);
            }

            updateContext(workspace, messenger.getPlatform(), conversationId, watsonMessage.getContext());
        });
    }
}
