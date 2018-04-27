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
			private Var codigoCurso = Var.VAR_NULL;
			private Var cep = Var.VAR_NULL;
			private Var jsonPolosProximos = Var.VAR_NULL;
			private Var listaPolosProximos = Var.VAR_NULL;
			private Var carousel = Var.VAR_NULL;
			private Var polos = Var.VAR_NULL;

			public Var call() throws Exception {
				watsonContext = cronapi.object.Operations.getObjectField(watsonMessage, Var.valueOf("context"));
				codigoCurso = Var.valueOf("151");
				cep = Var.valueOf("04671160");
				jsonPolosProximos = cronapi.util.Operations.getURLFromOthers(Var.valueOf("GET"),
						Var.valueOf("application/json"),
						Var.valueOf(Var.valueOf("https://www.unip.br/ead/api/servicos/cursos/").toString()
								+ codigoCurso.toString() + Var.valueOf("/polos/").toString() + cep.toString()),
						Var.VAR_NULL, Var.VAR_NULL);
				if (cronapi.logic.Operations.isNullOrEmpty(jsonPolosProximos).negate().getObjectAsBoolean()) {
					listaPolosProximos = cronapi.json.Operations.toJson(jsonPolosProximos);
					carousel = cronapi.list.Operations.newList();
					for (Iterator it_polos = listaPolosProximos.iterator(); it_polos.hasNext();) {
						polos = Var.valueOf(it_polos.next());
						cronapi.list.Operations
								.addLast(carousel,
										cronapi.map.Operations
												.createObjectMapWith(
														Var.valueOf("image",
																Var.valueOf(
																		"https://pronatec2019.com.br/wp-content/uploads/2018/01/Cursos-PRONATEC-UNIP-2019.jpg")),
														Var.valueOf("message",
																Var.valueOf(
																		Var.valueOf("Latitude").toString()
																				+ cronapi.object.Operations
																						.getObjectField(polos,
																								Var.valueOf(
																										"$.Latitude"))
																						.toString()
																				+ Var.valueOf("\nLongitude").toString()
																				+ cronapi.object.Operations
																						.getObjectField(polos,
																								Var.valueOf(
																										"$.Longitude"))
																						.toString()))));
						cronapi.map.Operations.setMapFieldByKey(watsonContext, Var.valueOf("carousel"), carousel);
					} // end for
				}
				return Var.VAR_NULL;
			}
		}.call();
	}

}
