package blockly;

import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.chatbot.ChatBotOperations;
import cronapi.chatbot.methods.StartChatBot;
import cronapi.chatbot.methods.TelegramOptions;
import cronapi.chatbot.methods.WatsonAssistantOptions;
import cronapi.rest.security.CronappSecurity;

import java.util.concurrent.Callable;

@CronapiMetaData(type = "blockly")
@CronappSecurity
public class RunAtStartup {

    public static final int TIMEOUT = 300;

    /**
     * @return Var
     */
    // RunAtStartup
    public static Var Executar() throws Exception {
        return new Callable<Var>() {

            public Var call() throws Exception {
                StartChatBot startChatBot = new StartChatBot();
                startChatBot.setTelegramOptions(new TelegramOptions());
                startChatBot.setWatsonAssistantOptions(new WatsonAssistantOptions());

                startChatBot.getTelegramOptions().setBotToken("571514660:AAGG-86rVJlCKkN8-Oz1OFPoSrXZ8wJ7_KM");
                startChatBot.getWatsonAssistantOptions().setWorkspaceId("90df8652-159d-4948-8d9a-bab4bf12a4c7");
                startChatBot.getWatsonAssistantOptions().setUserName("b61c3c7a-cc63-4613-a747-57bdc1aa6f0e");
                startChatBot.getWatsonAssistantOptions().setPassword("SQKxJGCToBB0");

                ChatBotOperations.startChatBot(startChatBot);
                return Var.VAR_NULL;
            }
        }.call();
    }

}
