package games.stendhal.client.gui.achievements;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import games.stendhal.client.gui.layout.SBoxLayout;

class NotObtainedAchievements {
	/** Container for the setting components. */
	private final JComponent page;

	NotObtainedAchievements() {
		int pad = SBoxLayout.COMMON_PADDING;
		page = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);

		page.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
	}

	/**
	 * Get the component containing not obtained achievemnts yet.
	 *
	 * @return general settings page
	 */
	JComponent getComponent() {
		return page;
	}
}
