package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.util.concurrent.Callable;

@CronapiMetaData(type = "blockly")
@CronappSecurity
public class BuscarRa {

	public static final int TIMEOUT = 300;

	/**
	 *
	 * @param watsonMessage
	 * @return Var
	 */
	// BuscarRa
	public static Var Executar(Var watsonMessage) throws Exception {
		return new Callable<Var>() {

			private Var watsonContext = Var.VAR_NULL;
			private Var sysNumbers = Var.VAR_NULL;
			private Var sysRa = Var.VAR_NULL;
			private Var jsonRa = Var.VAR_NULL;
			private Var codigoAluno = Var.VAR_NULL;

			public Var call() throws Exception {
				watsonContext = cronapi.object.Operations.getObjectField(watsonMessage, Var.valueOf("context"));
				sysNumbers = cronapi.object.Operations.getObjectField(watsonMessage,
						Var.valueOf("$.entities[?(@.entity==\'sys-number\')].value"));
				sysRa = cronapi.object.Operations.getObjectField(
						cronapi.util.Operations.getURLFromOthers(Var.valueOf("GET"), Var.valueOf("application/json"),
								Var.valueOf(
										Var.valueOf("https://fabrica1.lyceum.com.br/api/pessoas/cod_aluno/").toString()
												+ cronapi.object.Operations
														.getObjectField(sysNumbers, Var.valueOf("this[0]")).toString()
												+ Var.valueOf("/obtemPessoaAluno").toString()),
								Var.VAR_NULL, Var.VAR_NULL),
						Var.valueOf("this[0]"));
				jsonRa = Var.valueOf(
						Var.valueOf("{\"aluno\":[").toString() + sysRa.toString() + Var.valueOf("]}").toString());
				codigoAluno = cronapi.json.Operations.getJsonOrMapField(jsonRa, Var.valueOf("$.aluno[0].valor"));
				if (cronapi.logic.Operations.isNullOrEmpty(codigoAluno).negate().getObjectAsBoolean()) {
					cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("numeroRA"),
							cronapi.object.Operations.getObjectField(sysNumbers, Var.valueOf("this[0]")));
					cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("codigoAluno"), codigoAluno);
				}
				return Var.VAR_NULL;
			}
		}.call();
	}

}
