package battleship;

/**
 * Strings to be used by the user
 */
final class MenuCommands {

	static String AJUDA;
	static String GERAFROTA;
	static String LEFROTA;
	static String DESISTIR;
	static String RAJADA;
	static String TIROS;
	static String MAPA;
	static String STATUS;
	static String SIMULA;
	static String EXPORTAR_PDF;

	private MenuCommands() {
	}

	static void load() {
		AJUDA = Messages.get("command.help");
		GERAFROTA = Messages.get("command.generate");
		LEFROTA = Messages.get("command.readFleet");
		DESISTIR = Messages.get("command.quit");
		RAJADA = Messages.get("command.fire");
		TIROS = Messages.get("command.shots");
		MAPA = Messages.get("command.map");
		STATUS = Messages.get("command.status");
		SIMULA = Messages.get("command.simulate");
		EXPORTAR_PDF = Messages.get("command.pdf");
	}
}
