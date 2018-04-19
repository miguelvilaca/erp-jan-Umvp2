package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.util.concurrent.Callable;

@CronapiMetaData(type = "blockly")
@CronappSecurity
public class RunLongPoolingBot {

	public static final int TIMEOUT = 300;

	/**
	 *
	 * @return Var
	 */
	// RunLongPoolingBot
	public static Var Executar() throws Exception {
		return new Callable<Var>() {

			private Var token = Var.VAR_NULL;
			private Var update = Var.VAR_NULL;

			public Var call() throws Exception {
				token = Var.valueOf("571514660:AAGG-86rVJlCKkN8-Oz1OFPoSrXZ8wJ7_KM");
				Var.valueOf(cronapi.telegram.bots.TelegramBotOperations
						.startBotSession(
								cronapi.object.Operations
										.newObject(Var.valueOf("cronapi.telegram.bots.methods.GetUpdates"),
												Var.valueOf("token", token))
										.getTypedObject(cronapi.telegram.bots.methods.GetUpdates.class),
								(sender_update) -> {
									update = Var.valueOf(sender_update);
									blockly.TelegramUpdatesHandler.Executar(update);
								}));
				return Var.VAR_NULL;
			}
		}.call();
	}

}
