package games.stendhal.client.actions;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.achievements.AchievementsDialog;

/**
 * Show the achievements dialog
 */
class AchievementsAction implements SlashAction {
	private AchievementsDialog dialog;

	/**
	 * Execute a chat command.
	 *
	 * @param params
	 *            The formal parameters.
	 * @param remainder
	 *            Line content after parameters.
	 *
	 * @return <code>true</code> if was handled.
	 */
	@Override
	public boolean execute(String[] params, String remainder) {
		if (dialog == null) {
			dialog = new AchievementsDialog(j2DClient.get().getMainFrame());
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					dialog = null;
				}
			});
		}
		dialog.setVisible(true);
		dialog.toFront();

		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 *
	 * @return The parameter count.
	 */
	@Override
	public int getMaximumParameters() {
		return 0;
	}

	/**
	 * Get the minimum number of formal parameters.
	 *
	 * @return The parameter count.
	 */
	@Override
	public int getMinimumParameters() {
		return 0;
	}
}
