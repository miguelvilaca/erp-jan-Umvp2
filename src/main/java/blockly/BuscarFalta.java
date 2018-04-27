package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.util.Iterator;
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
			private Var listaFaltas = Var.VAR_NULL;
			private Var carousel = Var.VAR_NULL;
			private Var falta = Var.VAR_NULL;

			public Var call() throws Exception {
				watsonContext = cronapi.object.Operations.getObjectField(watsonMessage, Var.valueOf("context"));
				codigoAlunoBruto = cronapi.conversion.Operations.toString(
						cronapi.json.Operations.getJsonOrMapField(watsonContext, Var.valueOf("$.codigoAluno")));
				numeroRa = cronapi.json.Operations.getJsonOrMapField(watsonContext, Var.valueOf("$.numeroRA"));
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
					listaFaltas = cronapi.json.Operations.toList(jsonFalta);
					carousel = cronapi.list.Operations.newList();
					for (Iterator it_falta = listaFaltas.iterator(); it_falta.hasNext();) {
						falta = Var.valueOf(it_falta.next());
						cronapi.list.Operations.addLast(carousel,
								cronapi.map.Operations.createObjectMapWith(Var.valueOf("image", Var.valueOf("")),
										Var.valueOf("message", Var.valueOf(Var.valueOf("Disciplina:")
												.toString()
												+ cronapi.object.Operations.getObjectField(falta, Var.valueOf(
														"$.componenteCurricular"))
														.toString()
												+ Var.valueOf(
														Var.valueOf("- Faltas: ").toString() + cronapi.object.Operations
																.getObjectField(falta, Var.valueOf("$.numeroFaltas"))
																.toString()
																+ Var.valueOf("- Freq: ")
																		.toString()
																+ cronapi.object.Operations
																		.getObjectField(falta,
																				Var.valueOf("$.percentualFrequencia"))
																		.toString())
														.toString())),
										Var.valueOf("quick_reply", Var.valueOf(""))));
						cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("carousel"), carousel);
					} // end for
				}
				return Var.VAR_NULL;
			}
		}.call();
	}

}
