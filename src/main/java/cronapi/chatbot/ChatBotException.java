package cronapi.chatbot;

public class ChatBotException extends RuntimeException {
    public ChatBotException() {
        super();
    }

    public ChatBotException(String message) {
        super(message);
    }

    public ChatBotException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatBotException(Throwable cause) {
        super(cause);
    }
}
