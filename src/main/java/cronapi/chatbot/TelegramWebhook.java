package cronapi.chatbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.api.objects.Update;

@RestController
@RequestMapping("/api/cronapi/chatbot/telegram")
public class TelegramWebhook {

    private final Messenger messenger;
    private final WatsonAssistant assistant;

    @Autowired
    public TelegramWebhook(final TelegramMessenger messenger, final WatsonAssistant assistant) {
        this.messenger = messenger;
        this.assistant = assistant;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> handleCallback(@RequestBody Update update) {
        String chat = null;
        String message = null;

        if (update.getCallbackQuery() != null
                && update.getCallbackQuery().getMessage() != null) {
            message = update.getCallbackQuery().getData();
            chat = String.valueOf(update.getCallbackQuery().getMessage().getChat().getId());
        }
        if (chat == null)
            chat = String.valueOf(update.getMessage().getChat().getId());
        if (message == null)
            message = update.getMessage().getText();

        assistant.handleTextMessage(chat, message, messenger);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> verifyWebhook() {
        return ResponseEntity.ok("");
    }
}
