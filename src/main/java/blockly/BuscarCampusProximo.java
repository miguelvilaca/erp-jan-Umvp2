package blockly;

import cronapi.*;
import cronapi.rest.security.CronappSecurity;
import java.util.Iterator;
import java.util.concurrent.Callable;

@CronapiMetaData(type = "blockly")
@CronappSecurity
public class BuscarCampusProximo {

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
			private Var cep = Var.VAR_NULL;
			private Var codigoCursoBruto = Var.VAR_NULL;
			private Var posicaoDoPonto = Var.VAR_NULL;
			private Var codigoCurso = Var.VAR_NULL;
			private Var jsonPolosProximos = Var.VAR_NULL;
			private Var listaPolosProximos = Var.VAR_NULL;
			private Var campus = Var.VAR_NULL;
			private Var polos = Var.VAR_NULL;

			public Var call() throws Exception {
				watsonContext = cronapi.object.Operations.getObjectField(watsonMessage, Var.valueOf("context"));
				cep = cronapi.list.Operations.getFirst((cronapi.object.Operations.getObjectField(watsonMessage,
						Var.valueOf("$.entities[?(@.entity==\'sys-number\')].value"))));
				codigoCursoBruto = cronapi.map.Operations.getJsonOrMapField(watsonContext, Var.valueOf("$.idCurso"));
				posicaoDoPonto = Var.valueOf(
						codigoCursoBruto.getObjectAsString().indexOf(Var.valueOf(".").getObjectAsString()) + 1);
				codigoCurso = cronapi.text.Operations.getLettersFromStartToFromStart(codigoCursoBruto, Var.valueOf(0),
						(cronapi.math.Operations.subtract(posicaoDoPonto, Var.valueOf(1))));
				jsonPolosProximos = cronapi.util.Operations.getURLFromOthers(Var.valueOf("GET"),
						Var.valueOf("application/json"),
						Var.valueOf(Var.valueOf("https://www.unip.br/ead/api/servicos/cursos/").toString()
								+ codigoCurso.toString() + Var.valueOf("/polos/").toString() + cep.toString()),
						Var.VAR_NULL, Var.VAR_NULL);
				if (cronapi.logic.Operations.isNullOrEmpty(jsonPolosProximos).negate().getObjectAsBoolean()) {
					listaPolosProximos = cronapi.json.Operations.toJson(jsonPolosProximos);
					campus = Var.valueOf("");
					for (Iterator it_polos = listaPolosProximos.iterator(); it_polos.hasNext();) {
						polos = Var.valueOf(it_polos.next());
						campus = cronapi.map.Operations.createObjectMapWith(
								Var.valueOf("latitude",
										cronapi.object.Operations.getObjectField(polos, Var.valueOf("$.Latitude"))),
								Var.valueOf("longitude",
										cronapi.object.Operations.getObjectField(polos, Var.valueOf("$.Longitude"))));
						break;
					} // end for
					cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("location"), campus);
					cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("nomeCampus"),
							cronapi.object.Operations.getObjectField(polos, Var.valueOf("$.Nome")));
				}
				return Var.VAR_NULL;
			}
		}.call();
	}

}
