package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.lang.Math;
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
			private Var sysNumber = Var.VAR_NULL;
			private Var pacienteQuery = Var.VAR_NULL;
			private Var paciente = Var.VAR_NULL;
			private Var unidadeQuery = Var.VAR_NULL;
			private Var unidade = Var.VAR_NULL;
			private Var especialidades = Var.VAR_NULL;
			private Var especialidade = Var.VAR_NULL;
			private Var agendas = Var.VAR_NULL;
			private Var agenda = Var.VAR_NULL;

			public Var call() throws Exception {
				token = Var.valueOf("571514660:AAGG-86rVJlCKkN8-Oz1OFPoSrXZ8wJ7_KM");
				workspaceId = Var.valueOf("90df8652-159d-4948-8d9a-bab4bf12a4c7");
				chatId = cronapi.object.Operations.getObjectField(update, Var.valueOf("$.message.chat.id"));
				conversationQuery = cronapi.database.Operations.query(Var.valueOf("app.entity.Conversation"),
						Var.valueOf("select c from Conversation c where c.id = :id"), Var.valueOf("id", chatId));
				watsonConversationVersionData = Var.valueOf("2018-04-19");
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
				if (Var.valueOf(watsonText.equals(Var.valueOf("_obterDadosCartaoHygia"))).getObjectAsBoolean()) {
					sysNumbers = cronapi.object.Operations.getObjectField(watsonMessage,
							Var.valueOf("$.entities[?(@.entity==\'sys-number\')].value"));
					sysNumber = cronapi.object.Operations.getObjectField(sysNumbers, Var.valueOf("this[0]"));
					pacienteQuery = cronapi.database.Operations.query(Var.valueOf("diagram.hygia.entity.HyPacientes"),
							Var.valueOf("select h from HyPacientes h where h.numpac = :numpac"),
							Var.valueOf("numpac", sysNumber));
					if (cronapi.database.Operations.hasElement(pacienteQuery).getObjectAsBoolean()) {
						paciente = cronapi.database.Operations.getActiveData(pacienteQuery);
						unidadeQuery = cronapi.database.Operations.query(Var.valueOf("diagram.hygia.entity.HyUnidsau"),
								Var.valueOf("select u from HyUnidsau u where u.codunidsau = :codunidsau"),
								Var.valueOf("codunidsau", cronapi.object.Operations.getObjectField(paciente,
										Var.valueOf("usCodunidsau"))));
						if (cronapi.database.Operations.hasElement(unidadeQuery).getObjectAsBoolean()) {
							unidade = cronapi.database.Operations.getActiveData(unidadeQuery);
							cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("unidade_preferencial"),
									cronapi.object.Operations.getObjectField(unidade, Var.valueOf("nome")));
						}
						cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("nome_paciente"),
								cronapi.object.Operations.getObjectField(paciente, Var.valueOf("nome")));
						cronapi.map.Operations
								.setMapFieldByKey(
										watsonContext, Var
												.valueOf(
														"data_aleatoria"),
										cronapi.dateTime.Operations
												.formatDateTime(
														cronapi.dateTime.Operations
																.incMinute(cronapi.dateTime.Operations.getNow(),
																		cronapi.math.Operations.randomInt(
																				Var.valueOf(100), Var.valueOf(7200))),
														Var.valueOf("dd/MM \'às\' HH\':00\'")));
					}
					inputData = cronapi.object.Operations.newObject(
							Var.valueOf("com.ibm.watson.developer_cloud.conversation.v1.model.InputData"),
							Var.valueOf("text", Var.valueOf("")));
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
					watsonText = cronapi.object.Operations.getObjectField(watsonMessage,
							Var.valueOf("$.output.text[0]"));
				} else if (Var.valueOf(watsonText.equals(Var.valueOf("_obterAgendaSugerida"))).getObjectAsBoolean()) {
					especialidades = cronapi.object.Operations.getObjectField(watsonMessage,
							Var.valueOf("$.entities[?(@.entity==\'MEDICOS\')].value"));
					especialidade = cronapi.object.Operations.getObjectField(especialidades, Var.valueOf("this[0]"));
					agendas = cronapi.database.Operations.query(Var.valueOf("diagram.hygia.entity.HyVwAgprofisdisp"),
							Var.valueOf(
									"select a.horaini, a.horafim, a.nomeprofis, a.nomeus, a.data from HyVwAgprofisdisp a where a.nomeespec = :nomeespec AND a.data >= :data"),
							Var.valueOf("nomeespec", especialidade),
							Var.valueOf("data", cronapi.dateTime.Operations.getNow()));
					if (cronapi.database.Operations.hasElement(agendas).getObjectAsBoolean()) {
						agenda = cronapi.database.Operations.getActiveData(agendas);
						cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("unidade_preferencial"),
								cronapi.object.Operations.getObjectField(unidade, Var.valueOf("nome")));
					}
				}
				if (cronapi.logic.Operations.isNullOrEmpty(conversation).getObjectAsBoolean()) {
					conversation = cronapi.object.Operations.newObject(Var.valueOf("app.entity.Conversation"),
							Var.valueOf("id", chatId), Var.valueOf("context", watsonContext));
					cronapi.database.Operations.insert(Var.valueOf("app.entity.Conversation"), conversation);
				} else {
					cronapi.object.Operations.setObjectField(conversation, Var.valueOf("context"), watsonContext);
					cronapi.database.Operations.update(Var.valueOf("app.entity.Conversation"), conversation);
				}
				Var.valueOf(cronapi.telegram.bots.TelegramBotOperations.sendMessage(cronapi.object.Operations
						.newObject(Var.valueOf("cronapi.telegram.bots.methods.SendMessage"),
								Var.valueOf("chatId", chatId), Var.valueOf("text", watsonText),
								Var.valueOf("token", token))
						.getTypedObject(cronapi.telegram.bots.methods.SendMessage.class)));
				return Var.VAR_NULL;
			}
		}.call();
	}

}
