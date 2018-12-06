package tetris.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import tetris.network.*;

public class Tetris extends JFrame implements ActionListener {
 
	public JLabel localLabel, opponentLabel;
	private Connection connection;
	public BoardOpponent boardOpponent;
	public Board board;
	public ServerManager serverManager;
	public boolean isHost, isReadyToStart = false, pingResponse = false;
	public String hostname;
	public int portNumber;
	public String connectToHostname;
	public int connectToPortNumber;
	public JTextField hostnameTF, portTF;
	public JButton hostBtn, connectBtn;
	public JLabel timerLabel;
	public int timerValue = 100;
	private Timer timer;
	public Connection viewing;
	
	public Tetris() {
		localLabel = new JLabel("0");
		opponentLabel = new JLabel("0");
		timer = new Timer(1000, this);
		board = new Board(this);
		boardOpponent = new BoardOpponent(this);
		JPanel infoPanel = createInputPanel();
		
		board.add(localLabel, BorderLayout.SOUTH);
		boardOpponent.add(opponentLabel, BorderLayout.SOUTH);
		
		add(boardOpponent, BorderLayout.LINE_END);
		add(infoPanel, BorderLayout.CENTER);
		add(board, BorderLayout.LINE_START);

		setSize(600, 400); // W x H
		setTitle("My Tetris");

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				onExit();
			}
		});
	}

	public void onExit() {
//		this.serverManager.close(); not sure if needed
		System.exit(0);
	}

	private JPanel createInputPanel() {
		JPanel result = new JPanel();
		result.setPreferredSize(new Dimension(200, 200));

		JLabel hostnameLabel = new JLabel("Hostname");
		JLabel portLabel = new JLabel("Port");
		hostnameTF = new JTextField();
		portTF = new JTextField();
		hostBtn = new JButton("Host Game");
		connectBtn = new JButton("Connect");

		hostnameTF.setPreferredSize(new Dimension(170, 25));
		portTF.setPreferredSize(new Dimension(170, 25));

		JPanel hostPanel = new JPanel();
		JPanel portPanel = new JPanel();
		hostPanel.setPreferredSize(new Dimension(200, 100));
		portPanel.setPreferredSize(new Dimension(200, 100));
		hostPanel.add(hostnameLabel);
		hostPanel.add(hostnameTF);
		portPanel.add(portLabel);
		portPanel.add(portTF);

		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.gridx = 0;
		
		timerLabel = new JLabel(this.timerValue+"");
		
		result.setLayout(new GridBagLayout());
		result.add(timerLabel);
		result.add(hostPanel, cons);
		result.add(portPanel, cons);
		result.add(hostBtn, cons);
		result.add(connectBtn, cons);

		hostBtn.addActionListener(new inputButtonListener());
		connectBtn.addActionListener(new inputButtonListener());

		return result;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {		
		if(timerValue == 0) {
			board.statusBar.setText("Times Up! Score: " + board.score);
			boardOpponent.statusBar.setText("Times Up! Opponent Score: " + boardOpponent.opponentScore);
			board.timer.stop();
			boardOpponent.timer.stop();
			timer.stop();
		}
		this.timerLabel.setText(timerValue+"");
		timerLabel.repaint();
		timerValue--;
	}
	
	
	private class inputButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {					
			if (e.getSource() == Tetris.this.hostBtn) {
				Tetris.this.hostname = hostnameTF.getText();
				Tetris.this.portNumber = Integer.parseInt(portTF.getText());
				Tetris.this.hostBtn.setEnabled(false);
				Tetris.this.connectBtn.setEnabled(false);	
				
				Tetris.this.hostGame();
			} else if (e.getSource() == Tetris.this.connectBtn) {
				if(Tetris.this.hostnameTF.getText().length() == 0 || Tetris.this.portTF.getText().length() == 0) {
					return;
				}
				Tetris.this.hostname = hostnameTF.getText();
				Tetris.this.portNumber = Integer.parseInt(portTF.getText());
				
				Tetris.this.connectToHostname = JOptionPane.showInputDialog("Enter host to connect to:");
				Tetris.this.connectToPortNumber = Integer.parseInt(JOptionPane.showInputDialog("Enter port to connect to:"));
				
				System.out.println(connectToHostname);
				System.out.println(connectToPortNumber);
				
				Tetris.this.hostBtn.setEnabled(false);
				Tetris.this.connectBtn.setEnabled(false);	
				
				Tetris.this.connectToGame();
			}
		}
	}

	public void hostGame() {
		try {
			connection = new Connection(hostname, portNumber);
			serverManager = new ServerManager(connection);

		} catch (Exception e) {
			System.err.println("Error while establishing connection!\n");
			e.printStackTrace();
			return;
		}
		
		Message handShake = serverManager.receive(); 
		
		// simulate a blocking call
		while(handShake == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			handShake = serverManager.receive();
		}
		// only see game of first client to connect
		viewing = handShake.getSource();
		
		System.out.println("HANDSHAKE RECIEVED :: HOST");
		isReadyToStart = true;
	}
	
	public void connectToGame() {
		
		try {
			// 12610 - 12669
//			int randomNum = ThreadLocalRandom.current().nextInt(12610, 12669 + 1);
			connection = new Connection(hostname, portNumber);
			serverManager = new ServerManager(connection);
			
			viewing = new Connection(connectToHostname, connectToPortNumber);
			serverManager.connect(viewing);
//			serverManager.connect( new Connection("localhost", 12550) );
			serverManager.send("Sup");
		} catch (Exception e) {
			System.err.println("Error while establishing connection!\n");
			e.printStackTrace();
			return;
		}
		
		isReadyToStart = true;
	}

	// block until connection established
	public void waitToStart() {
		while(!isReadyToStart) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public static void main(String[] args) {
		Tetris myTetris = new Tetris();

		myTetris.setLocationRelativeTo(null);
		myTetris.pack();
		myTetris.setVisible(true);
		
		myTetris.waitToStart();
		myTetris.timer.start();
		myTetris.board.start();
		myTetris.boardOpponent.start();
	}

}
