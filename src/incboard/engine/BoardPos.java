/*
 * 
    Copyright 2008 RobertoPinho. All rights reserved.

    This file is part of incboard.api.

    incboard.api is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    incboard.api is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with incboard.api.  If not, see <http://www.gnu.org/licenses/>.

    For academic use, please cite:
    
    Pinho, R, de Oliveira, M.C. and Lopes, A. A. 2009. 
    Incremental Board: A grid-based space for visualizing dynamic data sets. 
    In Proceedings of the 2009 ACM Symposium on Applied Computing 
    (Honolulu, Hawaii, USA, March 8 - 12, 2009). SAC '09. ACM, New York, NY. 
    (to appear)
 */

package incboard.engine;



/**
 *
 * @author robertopinho
 */
public class BoardPos {

    public int i = -1;
    public int j = -1;
    int err = Integer.MAX_VALUE;

    BoardPos(CellItemInterface neighbor) {
        this(neighbor.getRow(),neighbor.getCol());
    }

    BoardPos(int i, int j, int err) {
        this.i = i;
        this.j = j;
        this.err = err;
    }
    
    public BoardPos(int i, int j) {
        this.i = i;
        this.j = j;
        this.err = 0;
    }


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.i;
        hash = 59 * hash + this.j;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        BoardPos p = (BoardPos) o;
        return (p.i == this.i && p.j == this.j);
    }
    }
