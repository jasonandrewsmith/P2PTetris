package tetris.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import tetris.network.*;

/**
 * Class that handles the local game being played via keyboard
 * 
 * @author Jason Smith
 *
 */

public class Board extends JPanel implements ActionListener {

	private static final int BOARD_WIDTH = 10;
	private static final int BOARD_HEIGHT = 22;
	public Timer timer;
	private boolean isFallingFinished = false;
	private boolean isStarted = false;
	private boolean isPaused = false;
	private int numLinesRemoved = 0;
	private int curX = 0;
	private int curY = 0;
	public JLabel statusBar;
	private Shape curPiece;
	public Tetrominoes[] board;
	public ArrayList<Action> actions = new ArrayList<>();
	public Queue<Integer> junkQueue;
	private Tetris parent;
	public String score = "0";
	
	public Board(Tetris parent) {
		setFocusable(true);
		curPiece = new Shape();
		timer = new Timer(400, this); // timer for lines down
		statusBar = parent.localLabel;
		board = new Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
		
		junkQueue = new LinkedList<Integer>();
		
		this.parent = parent;
		
		setBackground(new Color(0, 255, 153));
		setPreferredSize(new Dimension(200, 400));
		
		clearBoard();
		addKeyListener(new MyTetrisAdapter());
	}

	public int squareWidth() {
		return (int) getSize().getWidth() / BOARD_WIDTH;
	}

	public int squareHeight() {
		return (int) getSize().getHeight() / BOARD_HEIGHT;
	}

	public Tetrominoes shapeAt(int x, int y) {
		return board[y * BOARD_WIDTH + x];
	}
	
	/**
	 * Empties board
	 */
	
	private void clearBoard() {
		for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
			board[i] = Tetrominoes.NoShape;
		}
	}
	
	/**
	 * Logic performed when block touches down
	 */
	
	private void pieceDropped() {
		for (int i = 0; i < 4; i++) {
			int x = curX + curPiece.x(i);
			int y = curY - curPiece.y(i);
			board[y * BOARD_WIDTH + x] = curPiece.getShape();
		}

		removeFullLines();

		if (!isFallingFinished) {
//			newPiece();
			isFallingFinished = true;
		}
	}
	
	/**
	 * logic for new piece
	 */
	
	public void newPiece() {
		curPiece.setRandomShape();
		curX = BOARD_WIDTH / 2 + 1;
		curY = BOARD_HEIGHT - 1 + curPiece.minY();

		if (!tryMove(curPiece, curX, curY - 1)) {
			curPiece.setShape(Tetrominoes.NoShape);
			sendData(encodeData());
			timer.stop();
			isStarted = false;
			score = statusBar.getText();
			statusBar.setText("Game Over: Score = "+score);
		}
	}
	
	/**
	 * Moves current block down one
	 */
	
	private void oneLineDown() {
		if (!tryMove(curPiece, curX, curY - 1))
			pieceDropped();
//		sendData(encodeData());
	}
	
	/**
	 * Converts data to string
	 */
	
	private String encodeData() {
		String jsonString = "";
		
		/*** overkill for what I need (aka just the enum type)
		jsonString += "[\n";
		for(int i = 0; i < 10; i++) {
			String current = "\t{\n\t\t";
			current += "\"type\": \""+board[i].name()+"\",\n\t\t";
			current += "\"coords\":"+board[i].getCoords()+",\n\t\t";
			current += "\"R\":"+board[i].color.getRed()+",\n\t\t";
			current += "\"G\":"+board[i].color.getGreen()+",\n\t\t";
			current += "\"B\":"+board[i].color.getBlue()+",\n\t\t";
			current += "\"A\":"+board[i].color.getAlpha()+"\n\t}\n";
			jsonString += current;
		}
		jsonString +="]";
		***/
		jsonString += statusBar.getText()+"\n";
		for(int i = 0; i < board.length; i++) {
			jsonString += board[i].name()+"\n";
		}
		
		return jsonString;
	}
	
	/**
	 * Sends data to network
	 */
	
	private void sendData(String data) {
		try {
			parent.serverManager.send(data);
		} catch(Exception e) {
			System.out.println("Data failed to send!");
		}
	}
	
	/**
	 * Logic performed each timer tick
	 */
	
	@Override
	public void actionPerformed(ActionEvent ae) {		
//		System.out.println("Sent data!");
		
		if (isFallingFinished) {
			processJunkQueue();
			isFallingFinished = false;
			newPiece();
		} else {
			oneLineDown();
		}
		sendData(encodeData());
	}
	
	/**
	 * Draws indvidual squares of tetrominoes
	 */
	
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
	
	/**
	 * Called when repaint on board is called
	 */
	
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
	
	/**
	 * Starts game
	 */
	
	public void start() {
		if (isPaused)
			return;

		isStarted = true;
		isFallingFinished = false;
		numLinesRemoved = 0;
		clearBoard();
		newPiece();
		timer.start();
	}
	
	/**
	 * Pauses game
	 */
	
	public void pause() {
		if (!isStarted)
			return;

		isPaused = !isPaused;

		if (isPaused) {
			timer.stop();
			statusBar.setText("Paused");
		} else {
			timer.start();
			statusBar.setText(String.valueOf(numLinesRemoved));
		}

		repaint();
	}

	private boolean tryMove(Shape newPiece, int newX, int newY) {
		for (int i = 0; i < 4; ++i) {
			int x = newX + newPiece.x(i);
			int y = newY - newPiece.y(i);

			if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT)
				return false;

			if (shapeAt(x, y) != Tetrominoes.NoShape)
				return false;
		}

		curPiece = newPiece;
		curX = newX;
		curY = newY;
//		sendData(encodeData());
		repaint();

		return true;
	}
	
	/**
	 * Removes a full line on the board
	 */
	
	private void removeFullLines() {
		int numFullLines = 0;

		for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
			boolean lineIsFull = true;

			for (int j = 0; j < BOARD_WIDTH; ++j) {
				if (shapeAt(j, i) == Tetrominoes.NoShape) {
					lineIsFull = false;
					break;
				}
			}

			if (lineIsFull) {
				++numFullLines;

				for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
					for (int j = 0; j < BOARD_WIDTH; ++j) {
						board[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
					}
				}
			}

			if (numFullLines > 0) {
				numLinesRemoved += numFullLines;
				statusBar.setText(String.valueOf(numLinesRemoved));
				score = numLinesRemoved+"";
				isFallingFinished = true;
				curPiece.setShape(Tetrominoes.NoShape);
				repaint();
			}
		}
		
		if (numFullLines > 0) {
			sendData("ATTACK " + numFullLines);
		}
	}
	
	/**
	 * Adds junk blocks to board
	 */
	
	private void addJunkLines(int n) {
		for (int i = BOARD_HEIGHT - 1 ; i >= n; i--) {
			for (int j = 0; j < BOARD_WIDTH; j++) {
				board[i * BOARD_WIDTH + j] = shapeAt(j, i - n);
				board[(i - n) * BOARD_WIDTH + j] = Tetrominoes.NoShape;
			}
		}
		
		int randomSpace = (int) (Math.random() * (BOARD_WIDTH));
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < BOARD_WIDTH; j++) {
				if (j != randomSpace) {
					board[i * BOARD_WIDTH + j] = Tetrominoes.LineShape;
				}
				else {
					board[i * BOARD_WIDTH + j] = Tetrominoes.NoShape;
				}
			}
		}
		
		repaint();
	}
	
	private void processJunkQueue() {
		while (!junkQueue.isEmpty()) {
			addJunkLines(junkQueue.poll());
		}
	}
	
	public void addJunkAttackToQueue(int numLines) {
		junkQueue.add(numLines);
	}
	
	public void processMessage(String message) {
		String[] spaceSplit = message.split(" ");
		
		if (spaceSplit.length > 1) {
			if (spaceSplit[0].equals("ATTACK")) {
				addJunkAttackToQueue(Integer.parseInt(spaceSplit[1]));
			}
		}
	}

	private void dropDown() {
		int newY = curY;

		while (newY > 0) {
			if (!tryMove(curPiece, curX, newY - 1)) {
				break;
			}

			--newY;
		}
		pieceDropped();
	}
	
	/**
	 * Keyboard listener
	 */
	
	class MyTetrisAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent ke) {

//			sendData(encodeData());
			
			if (!isStarted || curPiece.getShape() == Tetrominoes.NoShape)
				return;

			int keyCode = ke.getKeyCode();

			if (keyCode == 'p' || keyCode == 'P')
				pause();

			if (isPaused)
				return;

			switch (keyCode) {
			case KeyEvent.VK_LEFT:
				tryMove(curPiece, curX - 1, curY);
				break;
			case KeyEvent.VK_RIGHT:
				tryMove(curPiece, curX + 1, curY);
				break;
			case KeyEvent.VK_DOWN:
				tryMove(curPiece.rotateRight(), curX, curY);
				break;
			case KeyEvent.VK_UP:
				tryMove(curPiece.rotateLeft(), curX, curY);
				break;
			case KeyEvent.VK_SPACE:
				dropDown();
				break;
			case 'd':
			case 'D':
				oneLineDown();
				break;
			}

		}
	}

}
