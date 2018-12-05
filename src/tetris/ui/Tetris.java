package tetris.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	public JTextField hostnameTF, portTF;
	public JButton hostBtn, connectBtn;
	public JLabel timerLabel;
	public long offset = 0; 
	public int timerValue = 45;
	private int timerWaitCount = 3;
	private Timer timer;
	
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
	
	private class BackgroundResponder extends Thread {
		public void run() {
			while(true) {
				System.out.println("Running thread...");
				respondToPing();
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}	
			}
		}
		
		private void respondToPing() {
			// if exception raised then oh well, we continue
			try {
				long[] received = (long[])serverManager.receive().content;	
				received[1] = Instant.now().toEpochMilli();
				received[2] = Instant.now().toEpochMilli();
				
				for(long d : received) {
					System.out.println("=> " + d);
				}
				
				serverManager.send(received);
				pingResponse = true;
			} catch(Exception e) {
				System.out.println("Caught exception in resp. to ping");
			}
		}
	}
	
	private long[] pingOpponent() {		
		board.timer.stop();
		boardOpponent.timer.stop();
		long[] timeStamps = new long[4];
		timeStamps[0] = Instant.now().toEpochMilli();
		
		serverManager.send(timeStamps);
		
		while(!pingResponse) {		
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			
			try {
				timeStamps = (long[])serverManager.receive().content;				
			} catch(Exception e) {
				/*just waiting...*/
				System.out.println("Waiting for ping response");
			}
		}
		
		timeStamps[3] = Instant.now().toEpochMilli();
		
		for(long d : timeStamps) {
			System.out.println("===> " + d);
		}
		
		System.out.println("\n\n");
		
		board.timer.start();
		boardOpponent.timer.start();
		return timeStamps;
	}
	
	public void findTimeOffset() {
		long[][] timeStampSets = new long[10][4];
		for(int i = 0; i<timeStampSets.length; i++) {
			timeStampSets[i] = pingOpponent();
		}
		
		long[] deltas = new long[10];
		for(long[] row : timeStampSets) {
			int i = 0;
			for(long timeStamp : row) { 
				deltas[i] = ((row[3]-row[0]) - (row[2]-row[1])) / 2;
				i++;
			}
		}
		
		int indexOfSmallest = 0;
		for(int k = 0; k<deltas.length; k++) {
			if(deltas[k] < deltas[indexOfSmallest]) indexOfSmallest = k;
		}
		
		long theta = ((timeStampSets[indexOfSmallest][1]-timeStampSets[indexOfSmallest][0])+
				      (timeStampSets[indexOfSmallest][2]-timeStampSets[indexOfSmallest][3])) / 2;
		offset = theta/1000;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		System.out.println("In tetris timer tick!");
		
		if(timerValue == 0) {
			board.statusBar.setText("Times Up! Score: " + board.score);
			boardOpponent.statusBar.setText("Times Up! Opponent Score: " + boardOpponent.opponentScore);
			board.timer.stop();
			boardOpponent.timer.stop();
			timer.stop();
		}
		this.timerLabel.setText(timerValue+"");
		timerLabel.repaint();
		
		System.out.println("VALUE: " + timerValue);
		
		// if count is 3 means ready to adjust again
		// adjusts clock over 1200ms (3 ticks)
		// by adding the delay/3 to timer delay
		System.out.println(Math.abs(offset)+"<----------");
		if(Math.abs(offset) < 10000L) {
			if(timerWaitCount == 3) {
				findTimeOffset();
				try {
					timer.setDelay(1000 + Math.round(offset/3));					
				} catch(Exception e) {
					timer.setDelay(1000);
				}
			} else if(timerWaitCount == 0) {
				timerWaitCount = 4;
				timer.setDelay(1000);
			}
		}
		timerWaitCount--;
		timerValue--;
	}
	
	
	private class inputButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {					
			if (e.getSource() == Tetris.this.hostBtn) {
				Tetris.this.hostBtn.setEnabled(false);
				Tetris.this.connectBtn.setEnabled(false);	
				
				Tetris.this.hostGame();
			} else if (e.getSource() == Tetris.this.connectBtn) {
				if(Tetris.this.hostnameTF.getText().length() == 0 || Tetris.this.portTF.getText().length() == 0) {
					return;
				}
				Tetris.this.hostname = hostnameTF.getText();
				Tetris.this.portNumber = Integer.parseInt(portTF.getText());
				Tetris.this.hostBtn.setEnabled(false);
				Tetris.this.connectBtn.setEnabled(false);	
				
				Tetris.this.connectToGame();
			}
		}
	}

	public void hostGame() {
		try {
			connection = new Connection("localhost", 12550);
			serverManager = new ServerManager(connection);

		} catch (Exception e) {
			System.err.println("Error while establishing connection!\n");
			e.printStackTrace();
			return;
		}
		
		Object handShake = serverManager.receive(); 
		
		// simulate a blocking call
		while(handShake == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			handShake = serverManager.receive();
		}
		
		System.out.println("HANDSHAKE RECIEVED :: HOST");
		serverManager.send("Yo");
		isReadyToStart = true;
	}
	
	public void connectToGame() {
		try {
			connection = new Connection("localhost", 12551);
			serverManager = new ServerManager(connection);
			
			serverManager.connect( new Connection(hostname, portNumber) );
//			serverManager.connect( new Connection("localhost", 12550) );
			serverManager.send("Sup");
		} catch (Exception e) {
			System.err.println("Error while establishing connection!\n");
			e.printStackTrace();
			return;
		}
		
		Object handShake = serverManager.receive();
		
		// simulate a blocking call
		while(handShake == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			handShake = serverManager.receive();
		}
		
		System.out.println("HANDSHAKE RECIEVED :: CLIENT");
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
		Tetris.BackgroundResponder repinger = new Tetris.BackgroundResponder();
		repinger.start();
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
