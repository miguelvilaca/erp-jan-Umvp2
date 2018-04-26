package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.util.Iterator;
import java.util.concurrent.Callable;

@CronapiMetaData(type = "blockly")
@CronappSecurity
public class BuscarPendencia {

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
			private Var jsonCobranca = Var.VAR_NULL;
			private Var listaPendencias = Var.VAR_NULL;
			private Var carousel = Var.VAR_NULL;
			private Var pendencia = Var.VAR_NULL;

			public Var call() throws Exception {
				watsonContext = cronapi.object.Operations.getObjectField(watsonMessage, Var.valueOf("context"));
				codigoAlunoBruto = cronapi.conversion.Operations.toString(
						cronapi.json.Operations.getJsonOrMapField(watsonContext, Var.valueOf("$.codigoAluno._object")));
				numeroRa = cronapi.json.Operations.getJsonOrMapField(watsonContext, Var.valueOf("$.numeroRA._object"));
				posicaoDoPonto = Var.valueOf(
						codigoAlunoBruto.getObjectAsString().indexOf(Var.valueOf(".").getObjectAsString()) + 1);
				codigoAluno = cronapi.text.Operations.getLettersFromStartToFromStart(codigoAlunoBruto, Var.valueOf(0),
						(cronapi.math.Operations.subtract(posicaoDoPonto, Var.valueOf(1))));
				jsonCobranca = cronapi.util.Operations.getURLFromOthers(
						Var.valueOf("GET"), Var
								.valueOf(
										"application/json"),
						Var.valueOf(Var.valueOf("https://fabrica1.lyceum.com.br/api/financeiro/").toString()
								+ codigoAluno.toString()
								+ Var.valueOf(Var.valueOf("/alunos/").toString() + numeroRa.toString()
										+ Var.valueOf("/cobrancas").toString()).toString()),
						Var.VAR_NULL, Var.VAR_NULL);
				if (cronapi.logic.Operations.isNullOrEmpty(jsonCobranca).negate().getObjectAsBoolean()) {
					listaPendencias = cronapi.json.Operations.toJson(jsonCobranca);
					carousel = cronapi.list.Operations.newList();
					for (Iterator it_pendencia = listaPendencias.iterator(); it_pendencia.hasNext();) {
						pendencia = Var.valueOf(it_pendencia.next());
						cronapi.list.Operations
								.addLast(carousel,
										cronapi.map.Operations
												.createObjectMapWith(
														Var.valueOf("image",
																Var.valueOf(
																		"https://pronatec2019.com.br/wp-content/uploads/2018/01/Cursos-PRONATEC-UNIP-2019.jpg")),
														Var.valueOf("message",
																Var.valueOf(Var.valueOf("Data Vencimento:").toString()
																		+ cronapi.object.Operations
																				.getObjectField(pendencia,
																						Var.valueOf(
																								"$.dataDeVencimento"))
																				.toString()
																		+ Var.valueOf("\nValor:").toString()
																		+ cronapi.object.Operations
																				.getObjectField(pendencia,
																						Var.valueOf("$.valor"))
																				.toString()))));
					} // end for
					cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("carousel"), carousel);
				}
				return Var.VAR_NULL;
			}
		}.call();
	}

}
