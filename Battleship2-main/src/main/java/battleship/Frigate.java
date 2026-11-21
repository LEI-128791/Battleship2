package battleship;

/**
 * The type Frigate represents a ship with a size of 4 units.
 * It is positioned on the game board based on its bearing and initial position.
 *
 * Author: britoeabreu
 * Date: 2023-10-10
 * Time: 15:30
 */
public class Frigate extends Ship {

	/**
	 * Instantiates a new Frigate.
	 *
	 * @param bearing The bearing of the ship (NORTH, SOUTH, EAST, or WEST).
	 * @param pos     The initial position of the ship on the game board.
	 */
	public Frigate(Compass bearing, IPosition pos) {
		super("Fragata", bearing, pos, 4);

		switch (bearing) {
			case NORTH:
				fillVertical(pos);
				break;
			case SOUTH:
				fillVertical(pos);
				break;
			case EAST:
				fillHorizontal(pos);
				break;
			case WEST:
				fillHorizontal(pos);
				break;
		}
	}
}