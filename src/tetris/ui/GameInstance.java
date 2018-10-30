package tetris.ui;

import javax.swing.JFrame;

public class GameInstance {
	JFrame mainContainer; 
	
	public static void main(String[] args) {
		GameInstance game = new GameInstance();
		GameInstance.launch(game);
	}
	
	public GameInstance() {
		mainContainer = new JFrame("Hello Swing");
		mainContainer.setSize(1200,900);
		mainContainer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void launch(GameInstance g) {
		// TODO eventually launch into a window where you can connect to a another player aka createHomeMenu
		g.createPlayingArea();
	}
	
	public void createPlayingArea() {
        this.mainContainer.setVisible(true);
	}
	
	public void createHomeMenu() {}

}
