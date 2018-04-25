package cronapi.chatbot.methods;

public class StartChatBot {
    private TelegramOptions telegramOptions;
    private WatsonAssistantOptions watsonAssistantOptions;

    public TelegramOptions getTelegramOptions() {
        return telegramOptions;
    }

    public void setTelegramOptions(TelegramOptions telegramOptions) {
        this.telegramOptions = telegramOptions;
    }

    public WatsonAssistantOptions getWatsonAssistantOptions() {
        return watsonAssistantOptions;
    }

    public void setWatsonAssistantOptions(WatsonAssistantOptions watsonAssistantOptions) {
        this.watsonAssistantOptions = watsonAssistantOptions;
    }
}
