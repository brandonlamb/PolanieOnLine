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
package games.stendhal.server.maps.krakow.ship;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.krakow.ship.KrakowFerry.Status;

import java.util.Map;


/** Factory for the captain of Krakow Ferry. */
public class CaptainNPC implements ZoneConfigurator  {

	private Status ferrystate;

	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Kapitan Jacek") {

			@Override
			public void createDialog() {
				addGreeting("Ahoj szczury lądowe!");
				addGoodbye("Na razie...");
				// if you can make up a help message that is more helpful to the,
				// player, feel free to replace this one.
				addHelp("Nie wychylaj się, bo możesz wpaść do wody!");
				addJob("Jestem kapitanem tej łajby.");

				add(ConversationStates.ATTENDING,
						"status",
						null,
						ConversationStates.ATTENDING,
						null,
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								npc.say(ferrystate.toString());
								//.getCurrentDescription());
							}
						});
			}

			@Override
			protected void onGoodbye(final RPEntity player) {
				// Turn back to the wheel
				setDirection(Direction.DOWN);
			}
		};

		new KrakowFerry.FerryListener() {
			@Override
			public void onNewFerryState(final Status status) {
				ferrystate = status;
					switch (status) {
					case ANCHORED_AT_WARSZAWA:
					case ANCHORED_AT_KRAKOW:
						// capital letters symbolize shouting
						npc.say("ZRZUCIĆ CUMY!");
						break;

					default:
						npc.say("CUMY ZRZUCONE! ODPŁYWAMY!");
						break;
					}
					// Turn back to the wheel
					npc.setDirection(Direction.DOWN);
			}
		};

		npc.setPosition(23, 36);
		npc.setEntityClass("npc_kptjacekwrobel");
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
