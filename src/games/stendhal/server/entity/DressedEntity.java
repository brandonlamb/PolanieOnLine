/***************************************************************************
 *                   (C) Copyright 2019 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity;

import static games.stendhal.common.Outfits.RECOLORABLE_OUTFIT_PARTS;

import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.item.Corpse;
import marauroa.common.game.RPObject;
import marauroa.common.game.SyntaxException;

/**
 * Defines an entity whose appearance (outfit) can be changed.
 */
public abstract class DressedEntity extends RPEntity {

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(DressedEntity.class);

	public DressedEntity() {
		super();
	}

	public DressedEntity(RPObject object) {
		super(object);
	}

	public static void generateRPClass() {
		try {
			DressedEntityRPClass.generateRPClass();
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	/**
	 * This is simply for backwards compatibility to update a user's outfit
	 * with the "outfit" attribute.
	 */
	@Override
	public void put(final String attr, final String value) {
		if (attr.equals("outfit")) {
			final StringBuilder sb = new StringBuilder();
			final int code = Integer.parseInt(value);

			sb.append("body=" + code % 100);
			sb.append(",dress=" + code / 100 % 100);
			sb.append(",head=" + (int) (code / Math.pow(100, 2) % 100));
			sb.append(",mask=" + (int) (code / Math.pow(100, 3) % 100));
			sb.append(",hair=" + (int) (code / Math.pow(100, 4) % 100));
			sb.append(",hat=" + (int) (code / Math.pow(100, 5) % 100));
			sb.append(",detail=" + (int) (code / Math.pow(100, 6) % 100));

			// "outfit_ext" actually manages the entity's outfit
			super.put("outfit_ext", sb.toString());
		}

		super.put(attr, value);
	}

	/**
	 * Gets this entity's outfit.
	 *
	 * Note: some entities (e.g. sheep, many NPC's, all monsters) don't use
	 * the outfit system.
	 *
	 * @return The outfit, or null if this RPEntity is represented as a single
	 *         sprite rather than an outfit combination.
	 */
	public Outfit getOutfit() {
		if (has("outfit_ext")) {
			return new Outfit(get("outfit_ext"));
		} else if (has("outfit")) {
			return new Outfit(Integer.toString(getInt("outfit")));
		}
		return null;
	}

	public Outfit getOriginalOutfit() {
		if (has("outfit_ext_orig")) {
			return new Outfit(get("outfit_ext_orig"));
		} else if (has("outfit_org")) {
			return new Outfit(Integer.toString(getInt("outfit_org")));
		}
		return null;
	}

	/**
	 * gets the color map
	 *
	 * @return color map
	 */
	public Map<String, String> getOutfitColors() {
		return getMap("outfit_colors");
	}

	/**
	 * Uses 'outfit' attribute to get a string formatted for 'outfit_ext' attribute.
	 * If entity does not have 'outfit' attribute, either the value of 'outfit_ext'
	 * attribute will be returned if found or a random outfit string will be generated.
	 *
	 * @return outfit_ext
	 */
	protected String getOutfitExtFromOutfitCode() {
		if (!has("outfit")) {
			if (has("outfit_ext")) {
				logger.info("DressedEntity.getOutfitExtFromOutfitCode: Returning value of 'outfit_ext' attribute");
				return get("outfit_ext");
			} else {
				logger.info("DressedEntity.getOutfitExtFromOutfitCode: Returning random outfit");
				return Outfit.getRandomOutfit().toString();
			}
		}

		return new Outfit(get("outfit")).toString();
	}

	/**
	 * Sets this entity's outfit.
	 *
	 * Note: some entities (e.g. sheep, many NPC's, all monsters) don't use
	 * the outfit system.
	 *
	 * @param outfit
	 *            The new outfit.
	 */
	public void setOutfit(final Outfit outfit) {
		setOutfit(outfit, false);
	}

	/**
	 * Makes this player wear the given outfit. If the given outfit contains
	 * null parts, the current outfit will be kept for these parts. If the
	 * outfit change includes any colors, they should be changed <b>after</b>
	 * calling this.
	 *
	 * @param outfit
	 *            The new outfit.
	 * @param temporary
	 *            If true, the original outfit will be stored so that it can be
	 *            restored later.
	 */
	public void setOutfit(final Outfit outfit, final boolean temporary) {
		// if the new outfit is temporary and the player is not wearing
		// a temporary outfit already, store the current outfit in a
		// second slot so that we can return to it later.
		if (temporary) {
			if (has("outfit_ext") && !has("outfit_ext_orig")) {
				put("outfit_ext_orig", get("outfit_ext"));
			}
			if (has("outfit") && !has("outfit_org")) {
				put("outfit_org", get("outfit"));
			}
			if (has("outfit_ext") || has("outfit")) {
				// remember the old color selections.
				for (String part : RECOLORABLE_OUTFIT_PARTS) {
					String tmp = part + "_orig";
					String color = get("outfit_colors", part);
					if (color != null) {
						put("outfit_colors", tmp, color);
						if (!"hair".equals(part)) {
							remove("outfit_colors", part);
						}
					} else if (has("outfit_colors", tmp)) {
						// old saved colors need to be cleared in any case
						remove("outfit_colors", tmp);
					}
				}
			}
		} else {
			if (has("outfit_ext_orig")) {
				remove("outfit_ext_orig");
			}
			if (has("outfit_org")) {
				remove("outfit_org");
			}
			if (has("outfit_ext_orig") || has("outfit_org")) {
				// clear colors
				for (String part : RECOLORABLE_OUTFIT_PARTS) {
					if (has("outfit_colors", part)) {
						remove("outfit_colors", part);
					}
				}
			}
		}

		// combine the old outfit with the new one, as the new one might
		// contain null parts.
		final Outfit newOutfit = outfit.putOver(getOutfit());

		StringBuilder sb = new StringBuilder();
		sb.append("body=" + newOutfit.getLayer("body") + ",");
		sb.append("dress=" + newOutfit.getLayer("dress") + ",");
		sb.append("head=" + newOutfit.getLayer("head") + ",");
		sb.append("mask=" + newOutfit.getLayer("mask") + ",");
		sb.append("hair=" + newOutfit.getLayer("hair") + ",");
		sb.append("hat=" + newOutfit.getLayer("hat") + ",");
		sb.append("detail=" + newOutfit.getLayer("detail"));

		put("outfit_ext", sb.toString());
		// FIXME: can't update "outfit" attribute without affecting "outfit_ext" (see: overridden method DressedEntity.put)
		//put("outfit", newOutfit.getCode());
		notifyWorldAboutChanges();
	}

	/**
	 * Makes this player wear the given outfit. If the given outfit contains
	 * null parts, the current outfit will be kept for these parts. If the
	 * outfit change includes any colors, they should be changed <b>after</b>
	 * calling this.
	 *
	 * Currently supported layers should be in this order:
	 * 		body, dress, head, mask, hair, hat, detail
	 *
	 * @param layers
	 *            Integer indexes of each outfit layer or null.
	 */
	public void setOutfit(final Integer... layers) {
		setOutfit(new Outfit(layers), false);
	}

	/**
	 * Makes this player wear the given outfit. If the given outfit contains
	 * null parts, the current outfit will be kept for these parts. If the
	 * outfit change includes any colors, they should be changed <b>after</b>
	 * calling this.
	 *
	 * Currently supported layers should be in this order:W
	 * 		body, dress, head, mask, hair, hat, detail
	 *
	 * @param temporary
	 *            If true, the original outfit will be stored so that it can be
	 *            restored later.
	 * @param layers
	 *            Integer indexes of each outfit layer or null.
	 */
	public void setOutfit(final boolean temporary, final Integer... layers) {
		setOutfit(new Outfit(layers), temporary);
	}

	public void setOutfit(final String strcode, final boolean temporary) {
		setOutfit(new Outfit(strcode), temporary);
	}

	public void setOutfit(final String strcode) {
		setOutfit(strcode, false);
	}

	// Hack to preserve detail layer
	public void setOutfitWithDetail(final Outfit outfit) {
		setOutfitWithDetail(outfit, false);
	}

	// Hack to preserve detail layer
	public void setOutfitWithDetail(final Outfit outfit, final boolean temporary) {
		// preserve detail layer
		final int detailCode = getOutfit().getLayer("detail");

		// set the new outfit
		setOutfit(outfit, temporary);

		if (detailCode > 0) {
			// get current outfit code
			final int outfitCode = outfit.getCode() + (detailCode * 100000000);

			// re-add detail
			put("outfit", outfitCode);
			notifyWorldAboutChanges();
		}
	}

	@Override
	protected abstract void dropItemsOn(Corpse corpse);

	@Override
	public abstract void logic();
}
