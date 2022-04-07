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
import java.util.Random;

public class Piece {

    private Tetrominoes pieceShape;
    private final int[][] coords;
    private int[][][] coordsTable;

    static enum Tetrominoes {
        NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape;

        private Tetrominoes() {
        }
    }

    public Piece() {
        this.coords = new int[4][2];
        setShape(Tetrominoes.NoShape);
    }

    public void setShape(Tetrominoes shape) {
        this.coordsTable = new int[][][]{
            {{0, 0}, {0, 0}, {0, 0}, {0, 0}}, 
            {{0, -1}, {0, 0}, {-1, 0}, {-1, 1}},
            {{0, -1}, {0, 0}, {1, 0}, {1, 1}}, 
            {{0, -1}, {0, 0}, {0, 1}, {0, 2}}, 
            {{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, 
            {{0, 0}, {1, 0}, {0, 1}, {1, 1}}, 
            {{-1, -1}, {0, -1}, {0, 0}, {0, 1}}, 
            {{1, -1}, {0, -1}, {0, 0}, {0, 1}}};
        
        for (int i = 0; i < 4; i++) {
            System.arraycopy(this.coordsTable[shape.ordinal()][i], 0, this.coords[i], 0, 2);
        }
        this.pieceShape = shape;
    }
    
    public Tetrominoes getShape() {
        return this.pieceShape;
    }

    private void setX(int index, int x) {
        this.coords[index][0] = x;
    }

    private void setY(int index, int y) {
        this.coords[index][1] = y;
    }

    public int x(int index) {
        return this.coords[index][0];
    }

    public int y(int index) {
        return this.coords[index][1];
    }

    public void setRandomShape() {
        Random r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1;
        Tetrominoes[] values = Tetrominoes.values();
        setShape(values[x]);
    }
 
    public int minX() {
        int m = this.coords[0][0];
        for (int i = 0; i < 4; i++) {
            m = Math.min(m, this.coords[i][0]);
        }
        return m;
    }

    public int minY() {
        int m = this.coords[0][1];
        for (int i = 0; i < 4; i++) {
            m = Math.min(m, this.coords[i][1]);
        }
        return m;
    }

    public Piece rotateLeft() {
        if (this.pieceShape == Tetrominoes.SquareShape) {
            return this;
        }
        
        Piece result = new Piece();
        result.pieceShape = this.pieceShape;
        for (int i = 0; i < 4; i++) {
            result.setX(i, y(i));
            result.setY(i, -x(i));
        }
        return result;
    }

    public Piece rotateRight() {
        if (this.pieceShape == Tetrominoes.SquareShape) {
            return this;
        }
        
        Piece result = new Piece();
        result.pieceShape = this.pieceShape;
        for (int i = 0; i < 4; i++) {
            result.setX(i, -y(i));
            result.setY(i, x(i));
        }
        return result;
    }
}
