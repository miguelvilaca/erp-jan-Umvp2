package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.util.concurrent.Callable;

@CronapiMetaData(type = "blockly")
@CronappSecurity
public class TelegramUpdatesHandler {

	public static final int TIMEOUT = 300;

	/**
	 *
	 * @param update
	 * @return Var
	 */
	// TelegramUpdatesHandler
	public static Var Executar(Var update) throws Exception {
		return new Callable<Var>() {

			private Var token = Var.VAR_NULL;
			private Var workspaceId = Var.VAR_NULL;
			private Var chatId = Var.VAR_NULL;
			private Var conversationQuery = Var.VAR_NULL;
			private Var watsonConversationVersionData = Var.VAR_NULL;
			private Var watsonConversationUsername = Var.VAR_NULL;
			private Var watsonConversationPassword = Var.VAR_NULL;
			private Var userMessage = Var.VAR_NULL;
			private Var conversation = Var.VAR_NULL;
			private Var watsonContext = Var.VAR_NULL;
			private Var inputData = Var.VAR_NULL;
			private Var watsonMessageOptions = Var.VAR_NULL;
			private Var watsonMessage = Var.VAR_NULL;
			private Var watsonText = Var.VAR_NULL;
			private Var sysNumbers = Var.VAR_NULL;
			private Var sysRa = Var.VAR_NULL;
			private Var jsonRa = Var.VAR_NULL;
			private Var numeroRa = Var.VAR_NULL;

			public Var call() throws Exception {
				token = Var.valueOf("571514660:AAGG-86rVJlCKkN8-Oz1OFPoSrXZ8wJ7_KM");
				workspaceId = Var.valueOf("90df8652-159d-4948-8d9a-bab4bf12a4c7");
				chatId = cronapi.object.Operations.getObjectField(update, Var.valueOf("$.message.chat.id"));
				conversationQuery = cronapi.database.Operations.query(Var.valueOf("app.entity.Conversation"),
						Var.valueOf("select c from Conversation c where c.id = :id"), Var.valueOf("id", chatId));
				watsonConversationVersionData = Var.valueOf("2018-02-16");
				watsonConversationUsername = Var.valueOf("b61c3c7a-cc63-4613-a747-57bdc1aa6f0e");
				watsonConversationPassword = Var.valueOf("SQKxJGCToBB0");
				userMessage = cronapi.object.Operations.getObjectField(update, Var.valueOf("$.message.text"));
				if (cronapi.database.Operations.hasElement(conversationQuery).getObjectAsBoolean()) {
					conversation = cronapi.database.Operations.getActiveData(conversationQuery);
					watsonContext = cronapi.object.Operations.getObjectField(conversation, Var.valueOf("context"));
				}
				if (cronapi.logic.Operations.isNullOrEmpty(userMessage).negate().getObjectAsBoolean()) {
					inputData = cronapi.object.Operations.newObject(
							Var.valueOf("com.ibm.watson.developer_cloud.conversation.v1.model.InputData"),
							Var.valueOf("text", userMessage));
				}
				watsonMessageOptions = cronapi.object.Operations.newObject(
						Var.valueOf("com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions"),
						Var.valueOf("workspaceId", workspaceId), Var.valueOf("input", inputData),
						Var.valueOf("context", watsonContext));
				watsonMessage = Var.valueOf(cronapi.watson.conversation.ConversationOperations.message(
						watsonConversationVersionData.getTypedObject(java.lang.String.class),
						watsonConversationUsername.getTypedObject(java.lang.String.class),
						watsonConversationPassword.getTypedObject(java.lang.String.class),
						Var.VAR_NULL.getTypedObject(java.lang.String.class),
						Var.VAR_NULL.getTypedObject(java.util.Map.class), watsonMessageOptions.getTypedObject(
								com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions.class)));
				watsonContext = cronapi.object.Operations.getObjectField(watsonMessage, Var.valueOf("$.context"));
				watsonText = cronapi.object.Operations.getObjectField(watsonMessage, Var.valueOf("$.output.text[0]"));
				if (Var.valueOf(watsonText.equals(Var.valueOf("_buscarRa"))).getObjectAsBoolean()) {
					sysNumbers = cronapi.object.Operations.getObjectField(watsonMessage,
							Var.valueOf("$.entities[?(@.entity==\'sys-number\')].value"));
					sysRa = cronapi.object.Operations
							.getObjectField(
									cronapi.util.Operations
											.getURLFromOthers(
													Var.valueOf("GET"), Var
															.valueOf(
																	"application/json"),
													Var.valueOf(Var
															.valueOf(
																	"https://fabrica1.lyceum.com.br/api/pessoas/cod_aluno/")
															.toString()
															+ cronapi.object.Operations
																	.getObjectField(sysNumbers, Var.valueOf("this[0]"))
																	.toString()
															+ Var.valueOf("/obtemPessoaAluno").toString()),
													Var.VAR_NULL, Var.VAR_NULL),
									Var.valueOf("this[0]"));
					jsonRa = Var.valueOf(
							Var.valueOf("{\"aluno\":[").toString() + sysRa.toString() + Var.valueOf("]}").toString());
					numeroRa = cronapi.json.Operations.getJsonOrMapField(jsonRa, Var.valueOf("$.aluno[0].valor"));
					if (Var.valueOf(!numeroRa.equals(Var.valueOf(""))).getObjectAsBoolean()) {
						cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("numero_ra"), numeroRa);
						watsonMessage = Var.valueOf(cronapi.watson.conversation.ConversationOperations.message(
								watsonConversationVersionData.getTypedObject(java.lang.String.class),
								watsonConversationUsername.getTypedObject(java.lang.String.class),
								watsonConversationPassword.getTypedObject(java.lang.String.class),
								Var.VAR_NULL.getTypedObject(java.lang.String.class),
								Var.VAR_NULL.getTypedObject(java.util.Map.class), watsonMessageOptions.getTypedObject(
										com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions.class)));
						watsonContext = cronapi.object.Operations.getObjectField(watsonMessage,
								Var.valueOf("$.context"));
						watsonText = cronapi.object.Operations.getObjectField(watsonMessage,
								Var.valueOf("$.output.text[0]"));
					}
				} else if (Var.valueOf(watsonText.equals(Var.valueOf("_obterAgendaSugerida"))).getObjectAsBoolean()) {
					{
					}
				} else {
					{
					}
				}
				cronapi.chatbot.ChatBotOperations.sendWatsonMessageToTelegram(watsonMessage, chatId, token);
				if (cronapi.logic.Operations.isNullOrEmpty(conversation).getObjectAsBoolean()) {
					conversation = cronapi.object.Operations.newObject(Var.valueOf("app.entity.Conversation"),
							Var.valueOf("id", chatId), Var.valueOf("context", watsonContext));
					cronapi.database.Operations.insert(Var.valueOf("app.entity.Conversation"), conversation);
				} else {
					cronapi.object.Operations.setObjectField(conversation, Var.valueOf("context"), watsonContext);
					cronapi.database.Operations.update(Var.valueOf("app.entity.Conversation"), conversation);
				}
				return Var.VAR_NULL;
			}
		}.call();
	}

}
