package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.util.concurrent.Callable;

@CronapiMetaData(type = "blockly")
@CronappSecurity
public class BuscarFalta {

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
			private Var codigoAlunoBruto = Var.VAR_NULL;
			private Var numeroRa = Var.VAR_NULL;
			private Var posicaoDoPonto = Var.VAR_NULL;
			private Var codigoAluno = Var.VAR_NULL;
			private Var jsonFalta = Var.VAR_NULL;
			private Var quickReplyFaltas = Var.VAR_NULL;

			public Var call() throws Exception {
				watsonContext = cronapi.object.Operations.getObjectField(watsonMessage, Var.valueOf("context"));
				codigoAlunoBruto = cronapi.conversion.Operations.toString(
						cronapi.json.Operations.getJsonOrMapField(watsonContext, Var.valueOf("$.codigoAluno._object")));
				numeroRa = cronapi.json.Operations.getJsonOrMapField(watsonContext, Var.valueOf("$.numeroRA._object"));
				posicaoDoPonto = Var.valueOf(
						codigoAlunoBruto.getObjectAsString().indexOf(Var.valueOf(".").getObjectAsString()) + 1);
				codigoAluno = cronapi.text.Operations.getLettersFromStartToFromStart(codigoAlunoBruto, Var.valueOf(0),
						(cronapi.math.Operations.subtract(posicaoDoPonto, Var.valueOf(1))));
				jsonFalta = cronapi.util.Operations
						.getURLFromOthers(
								Var.valueOf("GET"), Var
										.valueOf(
												"application/json"),
								Var.valueOf(
										Var.valueOf("https://fabrica1.lyceum.com.br/api/pessoas/codPessoa/").toString()
												+ codigoAluno.toString()
												+ Var.valueOf(Var.valueOf("/codAluno/").toString() + numeroRa.toString()
														+ Var.valueOf("/frequencia").toString()).toString()),
								Var.VAR_NULL, Var.VAR_NULL);
				if (cronapi.logic.Operations.isNullOrEmpty(jsonFalta).negate().getObjectAsBoolean()) {
					quickReplyFaltas = cronapi.list.Operations.newList();
					cronapi.list.Operations
							.addLast(quickReplyFaltas,
									cronapi.map.Operations
											.createObjectMapWith(Var.valueOf("message",
													Var.valueOf(cronapi.object.Operations
															.getObjectField(jsonFalta, Var.valueOf("$.[*]"))
															.toString()))));
					System.out.println(quickReplyFaltas.getObjectAsString());
				}
				cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("quick_reply"), quickReplyFaltas);
				return Var.VAR_NULL;
			}
		}.call();
	}

}
