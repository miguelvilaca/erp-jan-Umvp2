package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.util.concurrent.Callable;

@CronapiMetaData(type = "blockly")
@CronappSecurity
public class RunAtStartup {

	public static final int TIMEOUT = 300;

	/**
	 *
	 * @return Var
	 */
	// RunAtStartup
	public static Var Executar() throws Exception {
		return new Callable<Var>() {

			public Var call() throws Exception {
				blockly.RunLongPoolingBot.Executar();
				return Var.VAR_NULL;
			}
		}.call();
	}

}
