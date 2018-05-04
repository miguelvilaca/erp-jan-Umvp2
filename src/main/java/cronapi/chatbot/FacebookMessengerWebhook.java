package cronapi.chatbot;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerVerificationException;
import com.github.messenger4j.webhook.event.PostbackEvent;
import com.github.messenger4j.webhook.event.QuickReplyMessageEvent;
import com.github.messenger4j.webhook.event.TextMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.logging.Logger;

import static com.github.messenger4j.Messenger.*;
import static java.util.Optional.of;

@RestController
@RequestMapping("/api/cronapi/chatbot/facebook")
public class FacebookMessengerWebhook {

    private final Logger LOGGER = Logger.getLogger(FacebookMessengerWebhook.class.getName());
    private final Messenger messenger;
    private final FacebookMessenger facebookMessenger;
    private final WatsonAssistant assistant;

    @Autowired
    public FacebookMessengerWebhook(final Messenger messenger, final FacebookMessenger facebookMessenger, final WatsonAssistant assistant) {
        this.messenger = messenger;
        this.facebookMessenger = facebookMessenger;
        this.assistant = assistant;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> verifyWebhook(@RequestParam(MODE_REQUEST_PARAM_NAME) final String mode,
                                                @RequestParam(VERIFY_TOKEN_REQUEST_PARAM_NAME) final String verifyToken,
                                                @RequestParam(CHALLENGE_REQUEST_PARAM_NAME) final String challenge) {

        LOGGER.fine(() -> String.format("Received Facebook verification request - mode: %s | verifyToken: %s | " +
                "challenge: %s", mode, verifyToken, challenge));

        try {
            this.messenger.verifyWebhook(mode, verifyToken);
            return ResponseEntity.ok(challenge);
        } catch (MessengerVerificationException e) {
            LOGGER.warning(() -> String.format("Facebook verification failed: %s", e.getMessage()));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> handleCallback(@RequestBody final String payload,
                                               @RequestHeader(SIGNATURE_HEADER_NAME) final String signature) {

        LOGGER.fine(() -> String.format("Received Facebook callback - payload: %s | signature: %s",
                payload, signature));
        try {
            this.messenger.onReceiveEvents(payload, of(signature), event -> {
                if (event.isQuickReplyMessageEvent()) {
                    handleQuickReplyMessageEvent(event.asQuickReplyMessageEvent());
                } else if (event.isPostbackEvent()) {
                    handlePostbackEvent(event.asPostbackEvent());
                } else if (event.isTextMessageEvent()) {
                    handleTextMessageEvent(event.asTextMessageEvent());
                }
            });
            LOGGER.fine(() -> "Processed Facebook callback payload successfully");
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (MessengerVerificationException e) {
            LOGGER.warning(() -> String.format("Processing of Facebook callback payload failed: %s",
                    e.getMessage()));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private void handlePostbackEvent(PostbackEvent event) {
        LOGGER.fine(() -> String.format("Received PostbackEvent: %s", event));
        final String senderId = event.senderId();
        final Instant timestamp = event.timestamp();

        LOGGER.info(() -> String.format("Received Facebook post back from user '%s' at '%s'",
                senderId, timestamp));

        if (event.payload().isPresent())
            assistant.handleTextMessage(event.senderId(), event.payload().get(), facebookMessenger);
    }

    private void handleQuickReplyMessageEvent(QuickReplyMessageEvent event) {
        LOGGER.fine(() -> String.format("Received QuickReplyMessageEvent: %s", event));
        final String messageId = event.messageId();
        final String messageText = event.text();
        final String senderId = event.senderId();
        final Instant timestamp = event.timestamp();

        LOGGER.info(() -> String.format("Received Facebook quick reply '%s' with text '%s' from user '%s' at '%s'",
                messageId, messageText, senderId, timestamp));

        assistant.handleTextMessage(event.senderId(), event.payload(), facebookMessenger);
    }

    private void handleTextMessageEvent(TextMessageEvent event) {
        LOGGER.fine(() -> String.format("Received TextMessageEvent: %s", event));

        final String messageId = event.messageId();
        final String messageText = event.text();
        final String senderId = event.senderId();
        final Instant timestamp = event.timestamp();

        LOGGER.info(() -> String.format("Received Facebook message '%s' with text '%s' from user '%s' at '%s'",
                messageId, messageText, senderId, timestamp));

        assistant.handleTextMessage(event.senderId(), event.text(), facebookMessenger);
    }

}
