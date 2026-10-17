package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.MessageFormat;

import java.util.*;

public class Game implements IGame
{
	/**
	 * Prints the game board by representing the positions of ships, adjacent tiles,
	 * shots, and other game elements onto the console. The method also optionally
	 * displays shot positions and a legend explaining the symbols used on the board.
	 *
	 * @param fleet       the fleet of ships to be displayed on the board. Ships are marked
	 *                    and their positions are shown according to their placement.
	 * @param moves       the list of moves containing shots. If shot positions are shown,
	 *                    they will be rendered based on their outcome (hit, miss, etc.).
	 * @param show_shots  if true, displays the shots taken during the game and marks
	 *                    their result (hit or miss) on the board.
	 * @param showLegend  if true, displays an explanatory legend of the symbols used
	 *                    to represent various elements such as ships, misses, hits, etc.
	 */
	public static void printBoard(IFleet fleet, List<IMove> moves, boolean show_shots, boolean showLegend) {

		assert fleet != null;
		assert moves != null;

		char[][] map = new char[BOARD_SIZE][BOARD_SIZE];

		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				map[r][c] = EMPTY_MARKER;

		printShips(fleet, map);

		if (show_shots)
			printBoardShots(moves, map);

		printBoardFrame(map);

		if (showLegend) {
			System.out.println(Messages.get("legend.title"));
			System.out.println(Messages.get("legend.ships",
					SHIP_MARKER, SHIP_ADJACENT_MARKER, EMPTY_MARKER));
			System.out.println(Messages.get("legend.shots",
					SHOT_SHIP_MARKER, SHOT_WATER_MARKER));
		}
		System.out.println();
	}

	private static void printShips(IFleet fleet, char[][] map) {
		for (IShip ship : fleet.getShips()) {
			for (IPosition ship_pos : ship.getPositions())
				map[ship_pos.getRow()][ship_pos.getColumn()] = SHIP_MARKER;
			if (!ship.stillFloating())
				for (IPosition adjacent_pos : ship.getAdjacentPositions())
					map[adjacent_pos.getRow()][adjacent_pos.getColumn()] = SHIP_ADJACENT_MARKER;
		}
	}

	private static void printBoardShots(List<IMove> moves, char[][] map) {
		for (IMove move : moves)
			for (IPosition shot : move.getShots()) {
				if (shot.isInside()){
					int row = shot.getRow();
					int col = shot.getColumn();
					if (map[row][col] == SHIP_MARKER)
						map[row][col] = SHOT_SHIP_MARKER;
					if (map[row][col] == EMPTY_MARKER || map[row][col] == SHIP_ADJACENT_MARKER)
						map[row][col] = SHOT_WATER_MARKER;
				}
			}
	}

	private static void printBoardFrame(char[][] map) {
		System.out.println();
		System.out.print("    ");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print(" " + (col + 1));
		}
		System.out.println();

		System.out.print("   +-");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print("--");
		}
		System.out.println("+");

		for (int row = 0; row < BOARD_SIZE; row++) {
			Position pos = new Position(row, 0);
			char rowLabel = pos.getClassicRow();
			System.out.print(" " + rowLabel + " |");
			for (int col = 0; col < BOARD_SIZE; col++)
				System.out.print(" " + map[row][col]);
			System.out.println(" |");
		}

		System.out.print("   +");
		for (int col = 0; col < BOARD_SIZE; col++)
			System.out.print("--");
		System.out.println("-+");
	}

	/**
	 * Serializes a list of shot positions into a JSON string. Each shot is represented
	 * with its classic row and column values. The method uses the Jackson library for
	 * JSON serialization.
	 *
	 * @param shots a list of shot positions to be serialized. Each position is represented
	 *              by an implementation of the {@code IPosition} interface. The list must
	 *              not be null.
	 * @return a formatted JSON string containing the shot positions. Each shot includes
	 *         its classic row and column.
	 * @throws RuntimeException if an error occurs during JSON serialization.
	 */
	public static String jsonShots(List<IPosition> shots) {

		assert shots != null;

		// Serializar os tiros gerados em JSON usando a biblioteca Jackson
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		// 1. Create a simplified list containing only the desired data
		List<Map<String, Object>> simplifiedShots = new ArrayList<>();
		for (IPosition shot : shots) {
			Map<String, Object> simplePos = new LinkedHashMap<>();
			// We use getClassicRow() and getClassicColumn() based on your current JSON output
			simplePos.put("row", String.valueOf(shot.getClassicRow()));
			simplePos.put("column", shot.getClassicColumn());
			simplifiedShots.add(simplePos);
		}

		String jsonString = null;
		try {
			// 2. Serialize the simplified list instead of the raw 'shots' list
			jsonString = objectMapper.writeValueAsString(simplifiedShots);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(Messages.get("json.serializeError"), e);
		}

		System.out.println(jsonString);
		System.out.println();

		// Retornar o JSON
		return jsonString;
	}

	//------------------------------------------------------------------
	public static final int BOARD_SIZE = 10;
	public static final int NUMBER_SHOTS = 3;

	private static final char EMPTY_MARKER = '.';
	private static final char SHIP_MARKER = '#';
	private static final char SHOT_SHIP_MARKER = '*';
	private static final char SHOT_WATER_MARKER = 'o';
	private static final char SHIP_ADJACENT_MARKER = '-';

	//------------------------------------------------------------------
	private final IFleet myFleet;
	private final List<IMove> alienMoves;

	private final IFleet alienFleet;
	private final List<IMove> myMoves;

	private Integer countInvalidShots;
	private Integer countRepeatedShots;
	private Integer countHits;
	private Integer countSinks;
	private int moveNumber;

	//------------------------------------------------------------------
	public Game(IFleet myFleet)
	{
		this.moveNumber = 1;

		this.alienMoves = new ArrayList<IMove>();
		this.myMoves = new ArrayList<IMove>();

		this.alienFleet = new Fleet();
		this.myFleet = myFleet;

		this.countInvalidShots = 0;
		this.countRepeatedShots = 0;
		this.countHits = 0;
		this.countSinks = 0;
	}

	@Override
	public IFleet getMyFleet()
	{
		return myFleet;
	}

	@Override
	public IFleet getAlienFleet()
	{
		return alienFleet;
	}

	@Override
	public List<IMove> getMyMoves()
	{
		return myMoves;
	}

	@Override
	public List<IMove> getAlienMoves()
	{
		return alienMoves;
	}

	/**
	 * Simulates a random firing action by the enemy, generating a set of unique shot coordinates
	 * and serializing them into a JSON string. The method ensures that the random shots are valid
	 * and do not duplicate existing shots in the game or previous enemy moves. After generating
	 * the shots, it applies the firing logic and serializes the result for further processing.
	 *
	 * @return A JSON string representing the list of randomly generated enemy shots.
	 * @throws RuntimeException if there is an error during the JSON serialization of the shots.
	 */
	public String randomEnemyFire() {

		// Criar uma instância de Random com uma seed baseada no timestamp atual
		Random random = new Random(System.currentTimeMillis());

		Set<IPosition> usablePositions = new HashSet<IPosition>();
		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				usablePositions.add(new Position(r, c));

		this.myFleet.getSunkShips().forEach(ship -> usablePositions.removeAll(ship.getAdjacentPositions()));
		this.alienMoves.forEach(move ->  usablePositions.removeAll(move.getShots()));

		List<IPosition> candidateShots = new ArrayList<>(usablePositions);

		// Criar lista para armazenar os tiros
		List<IPosition> shots = new ArrayList<IPosition>();

		System.out.println();
		// Gerar coordenadas únicas até atingir o número definido por NUMBER_SHOTS

		IPosition newShot = null;
		if (candidateShots.size() >= Game.NUMBER_SHOTS)
			while (shots.size() < Game.NUMBER_SHOTS) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
		else {
			while (shots.size() < candidateShots.size()) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
			while (shots.size() < Game.NUMBER_SHOTS)
				shots.add(newShot);
		}

		System.out.println(Messages.get("command.fire"));
		for (IPosition shot : shots)
			System.out.print(shot + " ");
		System.out.println();

		this.fireShots(shots);

		return Game.jsonShots(shots);
	}


	/**
	 * Reads and processes the enemy fire input from the specified scanner.
	 * The method expects input describing positions for enemy shots. It verifies
	 * the format, ensures the correct number of positions are provided, and then fires
	 * on those positions.
	 *
	 * @param in the scanner object to read the enemy fire positions from, input must
	 *           be formatted either as a single token combining the column and row
	 *           (e.g., "A3") or as separate tokens (e.g., "A" followed by "3").
	 * @throws IllegalArgumentException if the provided positions are incomplete,
	 *                                  incorrectly formatted, or do not match the
	 *                                  required number of shots (NUMBER_SHOTS).
	 */
	public String readEnemyFire(Scanner in) {

		assert in != null;

		String input = in.nextLine().trim();

		// A rajada pode vir colada na linha seguinte ao comando
		if (input.isEmpty() && in.hasNextLine())
			input = in.nextLine().trim();

		// Os chats costumam devolver o JSON dentro de um bloco ```json ... ```
		if (input.startsWith("```")) {
			StringBuilder fenced = new StringBuilder(input.substring(3).replaceFirst("^(?i)json", ""));
			while (!fenced.toString().contains("```") && in.hasNextLine())
				fenced.append(' ').append(in.nextLine().trim());
			input = fenced.toString().replace("```", "").trim();
		}

		// Criar lista para armazenar os tiros
		List<IPosition> shots = new ArrayList<>();

		// Rajada em JSON (protocolo do LLM), possivelmente em várias linhas
		if (input.startsWith("{") || input.startsWith("[")) {
			StringBuilder json = new StringBuilder(input);
			while (!jsonBalanced(json) && in.hasNextLine())
				json.append(' ').append(in.nextLine().trim());

			java.util.regex.Matcher m = java.util.regex.Pattern
					.compile("\"row\"\\s*:\\s*\"?([A-Za-z])\"?\\s*,\\s*\"column\"\\s*:\\s*\"?(\\d+)\"?")
					.matcher(json);
			while (m.find())
				shots.add(new Position(Character.toUpperCase(m.group(1).charAt(0)), Integer.parseInt(m.group(2))));

			if (shots.size() != NUMBER_SHOTS) {
				throw new IllegalArgumentException(MessageFormat.format(Messages.get("game.wrongShotCount"), NUMBER_SHOTS));
			}
			this.fireShots(shots);
			return Game.jsonShots(shots);
		}

		Scanner inputScanner = new Scanner(input);
		while (shots.size() < NUMBER_SHOTS && inputScanner.hasNext()) {
			// Lê a próxima parte e constrói uma posição
			String token = inputScanner.next();

			if (token.matches("[A-Za-z]")) {
				// Caso seja somente uma coluna ("A", "B", etc.), esperar o próximo número
				if (inputScanner.hasNextInt()) {
					int row = inputScanner.nextInt();
					shots.add(new Position(token.toUpperCase().charAt(0), row));
				} else {
					throw new IllegalArgumentException(MessageFormat.format(Messages.get("position.incomplete"), token));
				}
			} else {
				// Caso o token já contenha a coluna e a linha juntas (ex.: "A3")
				Scanner singleScanner = new Scanner(token);
				shots.add(Tasks.readClassicPosition(singleScanner));
			}
		}

		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException(MessageFormat.format(Messages.get("game.wrongShotCount"), NUMBER_SHOTS));
		}

		this.fireShots(shots);

		return Game.jsonShots(shots);
	}

	/**
	 * Checks whether every opening bracket/brace in the given JSON text has been closed.
	 */
	private static boolean jsonBalanced(CharSequence json) {
		int depth = 0;
		for (int i = 0; i < json.length(); i++) {
			char c = json.charAt(i);
			if (c == '{' || c == '[') depth++;
			else if (c == '}' || c == ']') depth--;
		}
		return depth <= 0;
	}

	/**
	 * Fires a set of shots during a player's move. Each shot is resolved and
	 * consolidated into a move, which is processed and added to the list of alien moves.
	 * The method ensures exactly {@code NUMBER_SHOTS} shots are fired, validates
	 * each shot's position, and increments the move counter after completing the operation.
	 *
	 * @param shots a list of positions representing the locations to fire shots at.
	 *              The positions should be unique and valid within the bounds of the game board.
	 *              The size of the list must be equal to {@code NUMBER_SHOTS}.
	 * @throws IllegalArgumentException if the list of shots is null, contains an invalid
	 *                                  number of positions, or includes duplicate positions.
	 */
	public void fireShots(List<IPosition> shots)
	{
		assert shots != null;

		List<ShotResult> shotResults = new ArrayList<ShotResult>();
		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException(MessageFormat.format(Messages.get("game.wrongShotsPerMove"), NUMBER_SHOTS));
		}

		List<IPosition> alreadyShot = new ArrayList<IPosition>();
		for (IPosition pos : shots) {
			shotResults.add(fireSingleShot(pos, alreadyShot.contains(pos)));
			alreadyShot.add(pos);
		}

		Move move = new Move(moveNumber, shots, shotResults);

//		System.out.println(move);

		String json = move.processEnemyFire(true);

		alienMoves.add(move);

		System.out.println(llmPasteLine(json));
		System.out.println();

		moveNumber++;
	}

	/**
	 * Builds the single line to paste into the LLM chat: the JSON response, the logbook (every volley
	 * with its result), every shot fired so far (grouped by row, so the LLM never has to remember them)
	 * and a short reminder of the answer format.
	 * It only repeats what the LLM already received, so it gives no information about the fleet.
	 *
	 * @param json the JSON response produced by {@link Move#processEnemyFire(boolean)}
	 * @return the text to paste into the chat
	 */
	String llmPasteLine(String json) {
		Map<Character, List<Integer>> byRow = new TreeMap<>();
		for (IMove m : alienMoves)
			for (IPosition p : m.getShots())
				if (p.isInside())
					byRow.computeIfAbsent(p.getClassicRow(), r -> new ArrayList<>()).add(p.getClassicColumn());

		StringJoiner diary = new StringJoiner("; ");
		for (IMove m : alienMoves) {
			StringJoiner shots = new StringJoiner(" ");
			for (IPosition p : m.getShots())
				shots.add(p.toString());
			diary.add("R" + m.getNumber() + " " + shots + " = " + m.getSummary());
		}

		StringJoiner fired = new StringJoiner(" | ");
		for (Map.Entry<Character, List<Integer>> row : byRow.entrySet()) {
			StringJoiner cols = new StringJoiner(" ");
			row.getValue().stream().distinct().sorted().forEach(c -> cols.add(String.valueOf(c)));
			fired.add(row.getKey() + ": " + cols);
		}

		String compact;
		try {
			ObjectMapper mapper = new ObjectMapper();
			compact = mapper.writeValueAsString(mapper.readTree(json));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(Messages.get("json.serializeError"), e);
		}

		return Messages.get("llm.copy") + System.lineSeparator()
				+ compact + " " + MessageFormat.format(Messages.get("llm.diary"), diary.toString())
				+ " " + MessageFormat.format(Messages.get("llm.fired"), fired.toString())
				+ " " + Messages.get("llm.reminder");
	}

	/**
	 * Fires a single shot at the specified position, handling scenarios such as invalid positions,
	 * repeated shots, hits, misses, and sinking a ship. The method updates the necessary counters
	 * for invalid shots, repeated shots, hits, and sunk ships.
	 *
	 * @param pos the position to fire the shot at; must be valid and within the game board boundaries.
	 * @param isRepeated true if the shot is marked as a repeat attempt, false otherwise.
	 * @return a ShotResult object containing the result of the shot, including whether the shot was
	 *         valid, repeated, a hit, and whether a ship was sunk.
	 */
	public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) {

		assert pos != null;

		if (!pos.isInside()) {
			countInvalidShots++;
			return new ShotResult(false, false, null, false);
		}

		if (isRepeated || repeatedShot(pos)) {
			countRepeatedShots++;
			return new ShotResult(true, true, null, false);
		}

		IShip ship = myFleet.shipAt(pos);
		if (ship == null)
			return new ShotResult(true, false, null, false);
		else
		{
			ship.shoot(pos);
			countHits++;
			if (!ship.stillFloating()) {
				countSinks++;
			}
			return new ShotResult(true, false, ship, !ship.stillFloating());
		}
	}

	@Override
	public int getRepeatedShots()
	{
		return this.countRepeatedShots;
	}

	@Override
	public int getInvalidShots()
	{
		return this.countInvalidShots;
	}

	@Override
	public int getHits()
	{
		return this.countHits;
	}

	@Override
	public int getSunkShips()
	{
		return this.countSinks;
	}

	@Override
	public int getRemainingShips()
	{
		List<IShip> floatingShips = myFleet.getFloatingShips();
		return floatingShips.size();
	}

	public boolean repeatedShot(IPosition pos)
	{
		assert pos != null;

		for (IMove move : alienMoves)
			if (move.getShots().contains(pos))
				return true;
		return false;
	}

	public void printMyBoard(boolean show_shots, boolean show_legend)
	{
		Game.printBoard(this.myFleet, this.alienMoves, show_shots, show_legend);
	}

	public void printAlienBoard(boolean show_shots, boolean show_legend)
	{
		Game.printBoard(this.alienFleet, this.myMoves, show_shots, show_legend);
	}

	public void over() {
			System.out.println();
			System.out.println("+--------------------------------------------------------------+");
			System.out.println(Messages.get("game.farewell"));
			System.out.println("+--------------------------------------------------------------+");
	}
}