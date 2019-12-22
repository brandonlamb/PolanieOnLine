/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Outfits;
import games.stendhal.common.Rand;

/**
 * A data structure that represents the outfit of an RPEntity. This RPEntity can
 * either be an NPC which uses the outfit sprite system, or of a player.
 *
 * You can use this data structure so that you don't have to deal with the way
 * outfits are stored internally.
 *
 * An outfit can contain up to five parts: detail, hair, head, dress, and body.
 *
 * Note, however, that you can create outfit objects that consist of less than
 * five parts by setting the other parts to <code>null</code>. For example,
 * you can create a dress outfit that you can combine with the player's current
 * so that the player gets the dress, but keeps his hair, head, and body.
 *
 * Not all outfits can be chosen by players.
 *
 * @author daniel
 *
 */
public class Outfit {

	/** the logger instance. */
	private static final Logger LOGGER = Logger.getLogger(Outfit.class);

	private static final Map<String, String> EMPTY_MAP = new HashMap<>();

	private final Map<String, Integer> layers = new HashMap<>();

	/**
	 * Creates a new outfit. Set some of the parameters to null if you want an
	 * entity that put on this outfit to keep on the corresponding parts of its
	 * current outfit.
	 *
	 * Currently supported layers should be in this order:
	 * 		body, dress, head, mask, hair, hat, detail
	 *
	 * @param layers
	 * 		Integer indexes of each outfit layer or null.
	 */
	public Outfit(final Integer... layers) {
		int idx = 0;
		for (final String lname: Outfits.LAYER_NAMES) {
			if (idx >= layers.length) {
				break;
			}
			this.layers.put(lname, layers[idx]);
			idx++;
		}
	}

	/**
	 * Construct an outfit using a string.
	 *
	 * @param strcode
	 * 		Can be a comma separated key=value list or a 10-digit integer
	 * 		for backward compatibility.
	 */
	public Outfit(final String strcode) {
		if (strcode.contains("=")) {
			final String[] layers;
			if (strcode.contains(",")) {
				layers = strcode.split(",");
			} else {
				layers = new String[] {strcode};
			}

		    for (int idx = 0; idx < layers.length; idx++) {
		    	final String layer = layers[idx];
				if (layer.contains("=")) {
					final String[] key = layer.split("=");
					if (Outfits.LAYER_NAMES.contains(key[0])) {
						this.layers.put(key[0], Integer.parseInt(key[1]));
					}
				}
			}
		} else {
			try {
				final int code = Integer.parseInt(strcode);

				this.layers.put("body", code % 100);
				this.layers.put("dress", code / 100 % 100);
				this.layers.put("head", (int) (code / Math.pow(100, 2) % 100));
				this.layers.put("mask", 0);
				this.layers.put("hair", (int) (code / Math.pow(100, 3) % 100));
				this.layers.put("hat", 0);
				this.layers.put("detail", (int) (code / Math.pow(100, 4) % 100));
			} catch (NumberFormatException e) {
				LOGGER.warn("Can't parse outfit code, setting failsafe outfit.");
			}
		}
	}

	/**
	 * This method is added for backwards compatibility. Anything using this should be updated
	 * for new method.
	 *
	 * Creates a new outfit. Set some of the parameters to null if you want an
	 * entity that put on this outfit to keep on the corresponding parts of its
	 * current outfit.
	 *
	 * @param detail
	 *            The index of the detail style, or null
	 * @param hair
	 *            The index of the hair style, or null
	 * @param head
	 *            The index of the head style, or null
	 * @param dress
	 *            The index of the dress style, or null
	 * @param body
	 *            The index of the body style, or null
	 */
	@Deprecated
	public Outfit(final Integer detail, final Integer hair, final Integer head,
			final Integer dress, final Integer body) {
		layers.put("body", body);
		layers.put("dress", dress);
		layers.put("head", head);
		layers.put("hair", hair);
		layers.put("detail", detail);
	}

	public Integer getLayer(final String layerName) {
		Integer layer = layers.get(layerName);
		if (layer == null) {
			layer = 0;
		}

		return layer;
	}

	/**
	 * Represents this outfit in a numeric code.
	 *
	 * This is for backward-compatibility with old outfit system.
	 *
	 * @return A 10-digit decimal number where the first pair of digits stand for
	 *         detail, the second pair for hair, the third pair for head, the
	 *         fourth pair for dress, and the fifth pair for body
	 */
	public int getCode() {
		Integer detail = layers.get("detail");
		Integer hair = layers.get("hair");
		Integer head = layers.get("head");
		Integer dress = layers.get("dress");
		Integer body = layers.get("body");

		int de = 0;
		int ha = 0;
		int he = 0;
		int dr = 0;
		int bo = 0;

		if (detail != null) {
			de = detail.intValue();
		}
		if (hair != null) {
			ha = hair.intValue();
		}
		if (head != null) {
			he = head.intValue();
		}
		if (dress != null) {
			dr = dress.intValue();
		}
		if (body != null) {
			bo = body.intValue();
		}

		return (de * 100000000) + (ha * 1000000) + (he * 10000) + (dr * 100)
				+ bo;
	}

	/**
	 * Gets the result that you get when you wear this outfit over another
	 * outfit. Note that this new outfit can contain parts that are marked as
	 * NONE; in this case, the parts from the other outfit will be used.
	 *
	 * @param other
	 *            the outfit that should be worn 'under' the current one
	 * @return the combined outfit
	 */
	public Outfit putOver(Outfit old) {
		// make sure old outfit is not null
		if (old == null) {
			old = new Outfit();
		}

		Integer newBody = layers.get("body");
		Integer newDress = layers.get("dress");
		Integer newHead = layers.get("head");
		Integer newMask = layers.get("mask");
		Integer newHair = layers.get("hair");
		Integer newHat = layers.get("hat");
		Integer newDetail = layers.get("detail");

		// wear the this outfit 'over' the other outfit;
		// use the other outfit for parts that are not defined for this outfit.
		if (newBody == null) {
			newBody = old.getLayer("body");
		}
		if (newDress == null) {
			newDress = old.getLayer("dress");
		}
		if (newHead == null) {
			newHead = old.getLayer("head");
		}
		if (newMask == null) {
			newMask = old.getLayer("mask");
		}
		if (newHair == null) {
			newHair = old.getLayer("hair");
		}
		if (newHat == null) {
			newHat = old.getLayer("hat");
		}
		if (newDetail == null) {
			newDetail = old.getLayer("detail");
		}

		return new Outfit(newBody, newDress, newHead, newMask, newHair, newHat, newDetail);
	}

	/**
	 * Gets the result that you get when you remove (parts of) an outfit.
	 * Removes the parts in the parameter, from the current outfit.
	 * NOTE: If a part does not match, the current outfit part will remain the same.
	 *
	 * @param other
	 *            the outfit that should be removed from the current one
	 * @return the new outfit, with the parameter-outfit removed
	 */
	public Outfit removeOutfit(final Outfit other) {
		Integer newBody = layers.get("body");
		Integer newDress = layers.get("dress");
		Integer newHead = layers.get("head");
		Integer newMask = layers.get("mask");
		Integer newHair = layers.get("hair");
		Integer newHat = layers.get("hat");
		Integer newDetail = layers.get("detail");

		// wear the this outfit 'over' the other outfit;
		// use the other outfit for parts that are not defined for this outfit.
		if (newBody == null || newBody.equals(other.getLayer("body"))) {
			newBody = 0;
		}
		if ((newDress == null) || newDress.equals(other.getLayer("dress"))) {
			newDress = 0;
		}
		if ((newHead == null) || newHead.equals(other.getLayer("head"))) {
			newHead = 0;
		}
		if ((newMask == null) || newMask.equals(other.getLayer("mask"))) {
			newMask = 0;
		}
		if ((newHair == null) || newHair.equals(other.getLayer("hair"))) {
			newHair = 0;
		}
		if ((newHat == null) || newHat.equals(other.getLayer("hat"))) {
			newHat = 0;
		}
		if ((newDetail == null) || newDetail.equals(other.getLayer("detail"))) {
			newDetail = 0;
		}

		return new Outfit(newBody, newDress, newHead, newMask, newHair, newHat, newDetail);
	}

	/**
	 * Gets the result that you get when you remove (parts of) an outfit.
	 * Removes the parts in the parameter, from the current outfit.
	 * NOTE: If a part does not match, the current outfit part will remain the same.
	 *
	 * Currently supported layers should be in this order:
	 * 		body, dress, head, mask, hair, hat, detail
	 *
	 * @param layers
	 * 		Integer indexes of each outfit layer that should be removed.
	 * @return the new outfit, with the layers removed.
	 */
	public Outfit removeOutfit(final Integer... layers) {
		return removeOutfit(new Outfit(layers));
	}

	/**
	 * removes the details
	 */
	public void removeDetail() {
		// XXX: would it be better to use put("detail", 0)???
		layers.remove("detail");
	}

	/**
	 * Checks whether this outfit is equal to or part of another outfit.
	 *
	 * @param other
	 *            Another outfit.
	 * @return true iff this outfit is part of the given outfit.
	 */
	public boolean isPartOf(final Outfit other) {
		Integer hat = layers.get("hat");
		Integer mask = layers.get("mask");
		Integer detail = layers.get("detail");
		Integer hair = layers.get("hair");
		Integer head = layers.get("head");
		Integer dress = layers.get("dress");
		Integer body = layers.get("body");

		return ((hat == null) || hat.equals(other.getLayer("hat")))
				&& ((mask == null) || mask.equals(other.getLayer("mask")))
				&& ((detail == null) || detail.equals(other.getLayer("detail")))
				&& ((hair == null) || hair.equals(other.getLayer("hair")))
				&& ((head == null) || head.equals(other.getLayer("head")))
				&& ((dress == null) || dress.equals(other.getLayer("dress")))
				&& ((body == null) || body.equals(other.getLayer("body")));
	}

	/**
	 * Checks a single layer of the outfit.
	 *
	 * @param layer
	 * @param index
	 * @return
	 */
	public boolean isPartOf(final String layer, final Integer index) {
		return index.equals(layers.get(layer));
	}

	/**
	 * Checks whether this outfit may be selected by a normal player as normal
	 * outfit. It returns false for special event and GM outfits.
	 *
	 * @return true if it is a normal outfit
	 */
	public boolean isChoosableByPlayers() {
		Integer hat = layers.get("hat");
		Integer mask = layers.get("mask");
		Integer detail = layers.get("detail");
		Integer hair = layers.get("hair");
		Integer head = layers.get("head");
		Integer dress = layers.get("dress");
		Integer body = layers.get("body");

		return (hat == null || (hat < Outfits.HAT_OUTFITS) && (hat >= 0))
				&& (mask == null || (mask < Outfits.MASK_OUTFITS) && (mask >= 0))
				&& (detail == null || detail == 0)
				&& (hair == null || (hair < Outfits.HAIR_OUTFITS) && (hair >= 0))
				&& (head == null || (head < Outfits.HEAD_OUTFITS) && (head >= 0))
				&& (dress == null || (dress < Outfits.CLOTHES_OUTFITS) && (dress >= 0))
				&& (body == null || (body < Outfits.BODY_OUTFITS) && (body >= 0));
	}

	/**
	 * Is outfit missing a dress?
	 *
	 * @return true if naked, false if dressed
	 */
	public boolean isNaked() {
		Integer dress = layers.get("dress");

		if (isCompatibleWithClothes()) {
			return (dress == null) || dress.equals(0);
		}

		return false;
	}

	/**
	 * Create a random unisex outfit, with a 'normal' face and unisex body
	 *
	 * <ul>
	 * <li>hair number (1 to 26) selection of hairs which look ok with both goblin
	 *     face and cute face (later hairs only look right with cute face)</li>
	 * <li>head numbers (1 to 15) to avoid the cut eye, pink eyes, weird green eyeshadow etc</li>
	 * <li>dress numbers (1 to 16) from the early outfits before lady player base got introduced i.e. they are all unisex</li>
	 * <li>base numbers ( 1 to 15), these are the early bodies which were unisex</li>
	 * </ul>
	 * @return the new random outfit
	 */
	public static Outfit getRandomOutfit() {
		final int newHair = Rand.randUniform(0, Outfits.HAIR_OUTFITS - 1);
		final int newHead = Rand.randUniform(1, Outfits.HEAD_OUTFITS - 1);
		final int newDress = Rand.randUniform(1, Outfits.CLOTHES_OUTFITS - 1);
		final int newBody = Rand.randUniform(0, Outfits.BODY_OUTFITS - 1);

		LOGGER.debug("chose random outfit: "
				+ newHair + " " + newHead
				+ " " + newDress + " " + newBody);
		return new Outfit(newBody, newDress, newHead, 0, newHair, 0, 0);
	}

	/**
	 * Can this outfit be worn with normal clothes
	 *
	 * @return true if the outfit is compatible with clothes, false otherwise
	 */
	public boolean isCompatibleWithClothes() {
		final Integer body = layers.get("body");
		return body == null || !(body > 17 && body < 33)
				&& !(body == 50 && body == 51) && !(body > 77 && body < 99);
	}

	@Override
	public boolean equals(Object other) {
		boolean ret = false;

		if (!(other instanceof Outfit)) {
			return ret;
		}
		else {
			Outfit outfit = (Outfit)other;
			return this.getCode() == outfit.getCode();
		}
	}

	@Override
	public int hashCode() {
		return this.getCode();
	}

	public String getData(Map<String, String> colors) {
		if (colors == null) {
			colors = EMPTY_MAP;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("body-" + getLayer("body") + "-");
		sb.append(Integer.toHexString(MathHelper.parseIntDefault(colors.get("skin"), 0)));
		sb.append("_dress-" + getLayer("dress") + "-");
		sb.append(Integer.toHexString(MathHelper.parseIntDefault(colors.get("dress"), 0)));
		sb.append("_head-" + getLayer("head") + "-");
		sb.append(Integer.toHexString(MathHelper.parseIntDefault(colors.get("skin"), 0)));
		sb.append("_mask-" + getLayer("mask") + "-");
		sb.append(Integer.toHexString(MathHelper.parseIntDefault(colors.get("mask"), 0)));
		sb.append("_hair-" + getLayer("hair") + "-");
		sb.append(Integer.toHexString(MathHelper.parseIntDefault(colors.get("hair"), 0)));
		sb.append("_hat-" + getLayer("hat") + "-");
		sb.append(Integer.toHexString(MathHelper.parseIntDefault(colors.get("hat"), 0)));
		sb.append("_detail-" + getLayer("detail") + "-");
		sb.append(Integer.toHexString(MathHelper.parseIntDefault(colors.get("detail"), 0)));
		return sb.toString();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		int idx = 0;
		for (final String layer: Outfits.LAYER_NAMES) {
			if (layers.containsKey(layer)) {
				if (idx > 0) {
					sb.append(",");
				}
				sb.append(layer + "=" + layers.get(layer));
			}
			idx++;
		}

		return sb.toString();
	}
}
