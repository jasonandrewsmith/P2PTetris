package tetris.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import tetris.network.ServerManager;

public class BoardOpponent extends JPanel implements ActionListener {
	private static final int BOARD_WIDTH = 10;
	private static final int BOARD_HEIGHT = 22;
	private Timer timer;
	public ServerManager serverMananger;
	private Tetris parent;
	private Tetrominoes[] board = new Tetrominoes[220];
	private Shape curPiece;
	private int curX = 0;
	private int curY = 0;
	private int opponentScore = 0;
	private JLabel statusBar;
	
	public BoardOpponent(Tetris parent) {
		setPreferredSize(new Dimension(200, 400));
		setBackground(new Color(255, 102, 102));
		
		this.parent = parent;
		curPiece = new Shape();
		timer = new Timer(380, this);
		board = new Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
		statusBar = parent.opponentLabel;
		
		clearBoard();
	}
	
	private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
		Color color = shape.color;
		g.setColor(color);
		g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
		g.setColor(color.brighter());
		g.drawLine(x, y + squareHeight() - 1, x, y);
		g.drawLine(x, y, x + squareWidth() - 1, y);
		g.setColor(color.darker());
		g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
		g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
	}
	
	private String receiveOpponentData() {
		String fromOpponent = "";
		try {
			fromOpponent = (String)parent.serverManager.receive().content;
		} catch(Exception e) {
			System.out.println("Failed to recieve data from client!");
		}
//		System.out.println("GOT THIS: \n" + fromOpponent);
		return fromOpponent;
	}
	
	// converts and applies new board data
	private void decodeOpponentData(String data) {
		Scanner sc = new Scanner(data);
		try {
			String raw = sc.nextLine();
			this.statusBar.setText(raw);
			if(raw.equals("Game Over")) timer.stop();
		} catch(Exception e) {
			// idk
		}
		
		int index = 0;
		while(sc.hasNext()) {
			board[index] = determineType(sc.nextLine());
			index++;
		}
	}
	
	private Tetrominoes determineType(String s) {
		switch(s) {
			case "NoShape": 
				return Tetrominoes.NoShape;
			case "ZShape": 
				return Tetrominoes.ZShape;			
			case "SShape": 
				return Tetrominoes.SShape;			
			case "LineShape": 
				return Tetrominoes.LineShape;			
			case "TShape": 
				return Tetrominoes.TShape;			
			case "SquareShape": 
				return Tetrominoes.SquareShape;			
			case "LShape": 
				return Tetrominoes.LShape;			
			case "MirroredLShape": 
				return Tetrominoes.MirroredLShape;
			default:
				return Tetrominoes.NoShape;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		String data = receiveOpponentData();
		
		if (data.startsWith("ATTACK")) {
			parent.board.processMessage(data);;
		}
		else {
			decodeOpponentData(data);
				
		}	
		repaint();
	}
	
	private void clearBoard() {
		for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
			board[i] = Tetrominoes.NoShape;
		}
	}
	
	public void start() {
		timer.start();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Dimension size = getSize();
		int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

		for (int i = 0; i < BOARD_HEIGHT; i++) {
			for (int j = 0; j < BOARD_WIDTH; ++j) {
				Tetrominoes shape = shapeAt(j, BOARD_HEIGHT - i - 1);

				if (shape != Tetrominoes.NoShape) {
					drawSquare(g, j * squareWidth(), boardTop + i * squareHeight(), shape);
				}
			}
		}

		if (curPiece.getShape() != Tetrominoes.NoShape) {
			for (int i = 0; i < 4; ++i) {
				int x = curX + curPiece.x(i);
				int y = curY - curPiece.y(i);
				drawSquare(g, x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
						curPiece.getShape());
			}
		}
	}
	
	public Tetrominoes shapeAt(int x, int y) {
		return board[y * BOARD_WIDTH + x];
	}
	
	public int squareWidth() {
		return (int) getSize().getWidth() / BOARD_WIDTH;
	}

	public int squareHeight() {
		return (int) getSize().getHeight() / BOARD_HEIGHT;
	}
}
