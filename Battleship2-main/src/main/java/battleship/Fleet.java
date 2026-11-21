/**
 * 
 */
package battleship;

import java.util.ArrayList;
import java.util.List;
import java.text.MessageFormat;
import java.util.function.Predicate;

/**
 * The type Fleet.
 */
public class Fleet implements IFleet
{
	private List<IShip> filterShips(Predicate<IShip> condition) {
		List<IShip> filteredShips = new ArrayList<>();

		for (IShip s : ships) {
			if (condition.test(s)) {
				filteredShips.add(s);
			}
		}

		return filteredShips;
	}

	private static final String[] SHIP_TYPES = {"galeao",                           // 1 galleon
			"fragata",                           // 1 frigate
			"nau", "nau",                        // 2 carracks
			"caravela", "caravela", "caravela",  // 3 caravels
			"barca", "barca", "barca", "barca"};

	/**
	 * Creates a randomly generated fleet containing ships of various predefined types.
	 * Each ship is assigned a random bearing and position. If a ship cannot be added
	 * due to constraints (e.g., collision or boundary issues), it will be retried.
	 *
	 * @return a fully constructed and valid fleet as an instance of IFleet
	 */
	public static IFleet createRandom() {

		IFleet randomFleet = new Fleet();

		// Define the types of ships to be added
		String[] shipTypes =
				SHIP_TYPES; // 4 barges

		int fleetSize = 0;

		while (fleetSize < shipTypes.length) {

			// Build the ship
			Ship ship = Ship.buildShip(shipTypes[fleetSize], Compass.randomBearing(), Position.randomPosition());

			// Attempt to add the ship to the fleet
			if (randomFleet.addShip(ship)) {
				fleetSize++; // Increment count if ship is successfully added
			}
		}
		return randomFleet;
	}


    // -----------------------------------------------------

	/**
	 * The Ships.
	 */
	private final List<IShip> ships;

	// -----------------------------------------------------ge
	/**
	 * Instantiates a new Fleet.
	 */
	public Fleet()
    {
	ships = new ArrayList<>();
    }

	/**
	 * Gets ships.
	 *
	 * @return the ships
	 */
	@Override
    public List<IShip> getShips()
    {
	return ships;
    }
	
	/**
	 * Add ship boolean.
	 *
	 * @param s the s
	 * @return the boolean
	 */
	/*
     * (non-Javadoc)
     * 
     * @see battleship.IFleet#addShip(battleship.IShip)
     */
    @Override
    public boolean addShip(IShip s)
    {
		assert s != null;

		boolean result = false;
		boolean canAdd = (ships.size() <= FLEET_SIZE) && (s.isInsideBoard()) && (!collisionRisk(s));
		if (canAdd)
		{
			ships.add(s);
			result = true;
		}
		return result;
    }

	/**
	 * Gets ships like.
	 *
	 * @param category the category
	 * @return the ships like
	 */
	/*
     * (non-Javadoc)
     * 
     * @see battleship.IFleet#getShipsLike(java.lang.String)
     */
	@Override
	public List<IShip> getShipsLike(String category) {
		assert category != null;

		return filterShips(s -> s.getCategory().equals(category));
	}
	/**
	 * Gets floating ships.
	 *
	 * @return the floating ships
	 */
	/*
     * (non-Javadoc)
     * 
     * @see battleship.IFleet#getFloatingShips()
     */
	@Override
	public List<IShip> getFloatingShips() {
		return filterShips(s -> s.stillFloating());
	}


	/**
	 * Gets sunk ships.
	 *
	 * @return the sunk ships
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see battleship.IFleet#getSunkShips()
	 */
	@Override
	public List<IShip> getSunkShips() {
		return filterShips(s -> !s.stillFloating());
	}

	/**
	 * Ship at ship.
	 *
	 * @param pos the pos
	 * @return the ship
	 */
	/*
     * (non-Javadoc)
     * 
     * @see battleship.IFleet#shipAt(battleship.IPosition)
     */
    @Override
    public IShip shipAt(IPosition pos)
    {
		assert pos != null;

		for (IShip ship : ships)
			if (ship.occupies(pos))
				return ship;
		return null;
    }

	/**
	 * Colision risk boolean.
	 *
	 * @param s the s
	 * @return the boolean
	 */
	private boolean collisionRisk(IShip s)
    {
		assert s != null;

		for (int i = 0; i < ships.size(); i++)
		{
			if (ships.get(i).tooCloseTo(s))
				return true;
		}
		return false;
    }

	/**
	 * This operation prints all the given ships
	 *
	 * @param shipsToPrint The list of ships
	 */
	public void printShips(List<IShip> shipsToPrint)
	{
		assert shipsToPrint != null;

		for (IShip ship : shipsToPrint)
			System.out.println(ship);
	}

	/**
	 * This operation shows the state of a fleet
	 */
	public void printStatus()
    {
		System.out.println(MessageFormat.format(Messages.get("fleet.status"),
				this.getFloatingShips().size(),
				this.getSunkShips().size()));
//		printAllShips();
//		printFloatingShips();
//		printShipsByCategory("Galeao");
//		printShipsByCategory("Fragata");
//		printShipsByCategory("Nau");
//		printShipsByCategory("Caravela");
//		printShipsByCategory("Barca");
    }

	/**
	 * This operation prints all the ships of a fleet belonging to a particular
	 * category
	 *
	 * @param category The category of ships of interest
	 */
	public void printShipsByCategory(String category)
    {
		assert category != null;

		printShips(getShipsLike(category));
    }

	/**
	 * This operation prints all the ships of a fleet but not yet shot
	 */
	public void printFloatingShips()
    {
	printShips(getFloatingShips());
    }

	/**
	 * This operation prints all the ships of a fleet
	 */
	void printAllShips()
    {
		printShips(ships);
    }
}