/*
 * The MIT License
 *
 * Copyright 2022 SchumakerTeam.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.schumakerteam.tetris.gfx;

/**
 *
 * @author Hudson Schumaker
 */
import com.schumakerteam.tetris.core.Game;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

    private final int boardWidth = 10;
    private final int boardHeight = 22;
    private final Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private final JLabel statusbar;
    private Piece curPiece;
    Piece.Tetrominoes[] board;

    public Board(Game parent) {
        this.setFocusable(true);
        this.curPiece = new Piece();
        this.timer = new Timer(400, this);
        this.timer.start();

        this.statusbar = parent.getStatusBar();
        this.board = new Piece.Tetrominoes['Ü'];
        this.addKeyListener(new InputAdapter());
        this.clearBoard();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.isFallingFinished) {
            this.isFallingFinished = false;
            newPiece();
        } else {
            this.oneLineDown();
        }
    }

    int squareWidth() {
        return (int) getSize().getWidth() / boardWidth;
    }

    int squareHeight() {
        return (int) getSize().getHeight() / boardHeight;
    }

    Piece.Tetrominoes shapeAt(int x, int y) {
        return this.board[(y * 10 + x)];
    }

    public void start() {
        if (this.isPaused) {
            return;
        }
        this.isStarted = true;
        this.isFallingFinished = false;
        this.numLinesRemoved = 0;
        this.clearBoard();
        this.statusbar.setText(String.valueOf(this.numLinesRemoved));

        this.newPiece();
        this.timer.start();
    }

    private void pause() {
        if (!this.isStarted) {
            return;
        }
        this.isPaused = (!this.isPaused);
        if (this.isPaused) {
            this.timer.stop();
            this.statusbar.setText("   paused");
        } else {
            this.timer.start();
            this.statusbar.setText(String.valueOf(this.numLinesRemoved));
        }
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - 22 * squareHeight();
        for (int i = 0; i < 22; i++) {
            for (int j = 0; j < 10; j++) {
                Piece.Tetrominoes shape = shapeAt(j, 22 - i - 1);
                if (shape != Piece.Tetrominoes.NoShape) {
                    drawSquare(g, 0 + j * squareWidth(), boardTop + i * squareHeight(), shape);
                }
            }
        }
        
        if (this.curPiece.getShape() != Piece.Tetrominoes.NoShape) {
            for (int i = 0; i < 4; i++) {
                int x = this.curX + this.curPiece.x(i);
                int y = this.curY - this.curPiece.y(i);
                drawSquare(g, 0 + x * squareWidth(), boardTop + (22 - y - 1) * squareHeight(), this.curPiece.getShape());
            }
        }
    }

    private void dropDown() {
        int newY = this.curY;
        while ((newY > 0) && (tryMove(this.curPiece, this.curX, newY - 1))) {
            newY--;
        }
        pieceDropped();
    }

    private void oneLineDown() {
        if (!tryMove(this.curPiece, this.curX, this.curY - 1)) {
            pieceDropped();
        }
    }

    private void clearBoard() {
        for (int i = 0; i < 220; i++) {
            this.board[i] = Piece.Tetrominoes.NoShape;
        }
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) { // sidess
            int x = this.curX + this.curPiece.x(i);
            int y = this.curY - this.curPiece.y(i);
            this.board[(y * 10 + x)] = this.curPiece.getShape();
        }
        removeFullLines();
        if (!this.isFallingFinished) {
            newPiece();
        }
    }

    private void newPiece() {
        this.curPiece.setRandomShape();
        this.curX = 6;
        this.curY = (21 + this.curPiece.minY());
        if (!tryMove(this.curPiece, this.curX, this.curY)) {
            this.curPiece.setShape(Piece.Tetrominoes.NoShape);
            this.timer.stop();
            this.isStarted = false;
            this.statusbar.setText(" game over, press R to restart");
        }
    }

    private boolean tryMove(Piece newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if ((x < 0) || (x >= 10) || (y < 0) || (y >= 22)) {
                return false;
            }
            if (shapeAt(x, y) != Piece.Tetrominoes.NoShape) {
                return false;
            }
        }
        this.curPiece = newPiece;
        this.curX = newX;
        this.curY = newY;
        repaint();
        return true;
    }

    private void removeFullLines() {
        int numFullLines = 0;
        for (int i = 21; i >= 0; i--) {
            boolean lineIsFull = true;
            for (int j = 0; j < 10; j++) {
                if (shapeAt(j, i) == Piece.Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                numFullLines++;
                for (int k = i; k < 21; k++) {
                    for (int j = 0; j < 10; j++) {
                        this.board[(k * 10 + j)] = shapeAt(j, k + 1);
                    }
                }
            }
        }
        if (numFullLines > 0) {
            this.numLinesRemoved += numFullLines;
            this.statusbar.setText(String.valueOf(this.numLinesRemoved));
            this.isFallingFinished = true;
            this.curPiece.setShape(Piece.Tetrominoes.NoShape);
            repaint();
        }
    }

    private void drawSquare(Graphics g, int x, int y, Piece.Tetrominoes shape) {
        Color[] colors = {
            new Color(0, 0, 0),       // black
            new Color(204, 102, 102), 
            new Color(102, 204, 102), 
            new Color(102, 102, 204), 
            new Color(204, 204, 102),
            new Color(204, 102, 204), 
            new Color(102, 204, 204), 
            new Color(218, 170, 0)};

        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);

        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }

    private class InputAdapter extends KeyAdapter {

        public InputAdapter() {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keycode = e.getKeyCode();
            if (keycode == KeyEvent.VK_R) {
                Board.this.start();
            }
            
            if ((!Board.this.isStarted) || (Board.this.curPiece.getShape() == Piece.Tetrominoes.NoShape)) {
                return;
            }

            if (keycode == KeyEvent.VK_P) {
                Board.this.pause();
                return;
            }

            if (Board.this.isPaused) {
                return;
            }

            switch (keycode) {
                case 37:
                    Board.this.tryMove(Board.this.curPiece, Board.this.curX - 1, Board.this.curY);
                    break;
                case 39:
                    Board.this.tryMove(Board.this.curPiece, Board.this.curX + 1, Board.this.curY);
                    break;
                case 40:
                    Board.this.tryMove(Board.this.curPiece.rotateRight(), Board.this.curX, Board.this.curY);
                    break;
                case 38:
                    Board.this.tryMove(Board.this.curPiece.rotateLeft(), Board.this.curX, Board.this.curY);
                    break;
                case 32:
                    Board.this.dropDown();
                    break;
                case 68:
                    Board.this.oneLineDown();
                    break;
            }
        }
    }
}
