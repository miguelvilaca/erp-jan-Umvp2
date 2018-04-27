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
			private Var retornoNota = Var.VAR_NULL;
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
					retornoNota = Var.valueOf("");
					for (Iterator it_nota = listaNotas.iterator(); it_nota.hasNext();) {
						nota = Var.valueOf(it_nota.next());
						retornoNota = Var.valueOf(retornoNota.toString() + Var.valueOf("Disciplina:\n").toString()
								+ cronapi.object.Operations.getObjectField(nota, Var.valueOf("$.nomeDisciplina"))
										.toString()
								+ Var.valueOf("\n  - Média: ").toString() + Var.valueOf("\n").toString()
								+ Var.valueOf("\n").toString());
					} // end for
					cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("retornoNota"), retornoNota);
				}
				return Var.VAR_NULL;
			}
		}.call();
	}

}
