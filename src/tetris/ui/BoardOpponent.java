package tetris.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

import tetris.network.ServerManager;

public class BoardOpponent extends JPanel {
	private static final int BOARD_WIDTH = 10;
	private static final int BOARD_HEIGHT = 22;
	private Timer timer;
	private ArrayList<Action> actionsQueue = new ArrayList<>();
	public ServerManager serverMananger;
	private Tetris parent;
	
	public BoardOpponent(Tetris parent) {
		this.parent = parent;
		setPreferredSize(new Dimension(200, 400));
		setBackground(Color.CYAN);
	}
	
	private void applyActions() {
		for(Action action : actionsQueue) {
			// do something
		}
	}
}
