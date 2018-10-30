package tetris.ui;

import javax.swing.JFrame;

public class GameInstance {
	
	public static void main(String[] args) {
		GameInstance game = new GameInstance();
		game.launch();
		
	}
	
	public void launch() {
		// TODO eventually launch into a window where you can connect to a another player aka createHomeMenu
		
		JFrame frame = new JFrame("Hello Swing");
		frame.setSize(1200,1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
	}
	
	public void createPlayingArea() {
		
	}
	
	public void createHomeMenu() {}

}
