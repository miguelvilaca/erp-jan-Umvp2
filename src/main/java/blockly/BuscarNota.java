package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.util.Iterator;
import java.util.concurrent.Callable;

@CronapiMetaData(type = "blockly")
@CronappSecurity
public class BuscarNota {

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
			private Var jsonNota = Var.VAR_NULL;
			private Var listaNotas = Var.VAR_NULL;
			private Var carousel = Var.VAR_NULL;
			private Var nota = Var.VAR_NULL;

			public Var call() throws Exception {
				watsonContext = cronapi.object.Operations.getObjectField(watsonMessage, Var.valueOf("context"));
				codigoAlunoBruto = cronapi.conversion.Operations.toString(
						cronapi.json.Operations.getJsonOrMapField(watsonContext, Var.valueOf("$.codigoAluno")));
				numeroRa = cronapi.json.Operations.getJsonOrMapField(watsonContext, Var.valueOf("$.numeroRA"));
				posicaoDoPonto = Var.valueOf(
						codigoAlunoBruto.getObjectAsString().indexOf(Var.valueOf(".").getObjectAsString()) + 1);
				codigoAluno = cronapi.text.Operations.getLettersFromStartToFromStart(codigoAlunoBruto, Var.valueOf(0),
						(cronapi.math.Operations.subtract(posicaoDoPonto, Var.valueOf(1))));
				jsonNota = cronapi.util.Operations.getURLFromOthers(
						Var.valueOf("GET"), Var
								.valueOf(
										"application/json"),
						Var.valueOf(
								Var.valueOf("https://fabrica1.lyceum.com.br/api/pessoas/").toString()
										+ codigoAluno.toString()
										+ Var.valueOf(Var.valueOf("/alunos/").toString() + numeroRa.toString()
												+ Var.valueOf("/boletim").toString()).toString()),
						Var.VAR_NULL, Var.VAR_NULL);
				if (cronapi.logic.Operations.isNullOrEmpty(jsonNota).negate().getObjectAsBoolean()) {
					listaNotas = cronapi.json.Operations.toList(jsonNota);
					carousel = cronapi.list.Operations.newList();
					System.out.println(listaNotas.getObjectAsString());
					for (Iterator it_nota = listaNotas.iterator(); it_nota.hasNext();) {
						nota = Var.valueOf(it_nota.next());
						cronapi.list.Operations
								.addLast(carousel,
										cronapi.map.Operations.createObjectMapWith(
												Var.valueOf("image", Var.valueOf("")), Var
														.valueOf("message",
																Var.valueOf(
																		Var.valueOf("Disciplina:").toString()
																				+ cronapi.object.Operations
																						.getObjectField(nota,
																								Var.valueOf(
																										"$.nomeDisciplina"))
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
