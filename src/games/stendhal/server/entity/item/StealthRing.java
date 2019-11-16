package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;

public class StealthRing extends Item {

	public StealthRing(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item copied item
	 */
	public StealthRing(final StealthRing item) {
		super(item);
	}

	/**
	 * Create a StealthRing.
	 */
	public StealthRing() {
		super("pierścień ukrycia", "ring", "stealth-ring", null);
		put("amount", 1);
	}

	/**
	 * Checks if Player have fitted the stealth ring on any finger.
	 * @param p
	 * 			Ring owner
	 * @param c
	 * 			Creature
	 */
	public boolean FittedRing(final Player p, final Creature c) {
		//final Item item = SingletonRepository.getEntityManager().getItem("pierścień ukrycia");
		if (p.isEquippedItemInSlot("fingerb", "pierścień ukrycia")) { //p.isEquipped("pierścień ukrycia")
			return c.getNearestEnemy(c.getPerceptionRange()/2) != null;
		} else {
			return c.getNearestEnemy(c.getPerceptionRange()+2) != null;
		}
	}

	/**
	 * Gets the description.
	 */
	@Override
	public String describe() {
		String text = "Oto §'pierścień ukrycia'. Jeden by wszystkimi rządzić...";
		if (isBound()) {
			return text + " Oto specjalna nagroda dla " + getBoundTo()
					+ " za wykonanie zadania, która nie może być używana przez innych.";
		} else {
			return text;
		}
	}
}
