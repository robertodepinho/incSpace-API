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



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;


/**
 *
 * @author robertopinho
 */
public abstract class AbstractBoard  {
    private List _listeners = new ArrayList();

    Map<Integer, Row> rows = new HashMap<Integer, Row>();
    private LinkedHashSet<CellItemInterface> items = new LinkedHashSet<CellItemInterface>();
    int minRow = Integer.MAX_VALUE;
    int minCol = Integer.MAX_VALUE;
    int maxRow = Integer.MIN_VALUE;
    int maxCol = Integer.MIN_VALUE;
    protected int moveCount =0;

    private double beta = 20;
    
    public BoardCell getCell(int i, int j) {
        Row r = rows.get(i);
        if (r == null) {
            return null;
        } else {
            return (BoardCell) rows.get(i).cols.get(j);
        }
    }

    public List<CellItemInterface> getItems(int i, int j) {
        BoardCell c = getCell(i, j);
        if (c == null) {
            return null;
        } else {
            return c.getItems();
        }
    }

    public int itemCount(int i, int j) {
        List<CellItemInterface> cellItems = getItems(i, j);
        if (cellItems == null) {
            return 0;
        } else {
            return cellItems.size();
        }

    }

    void addItem(CellItemInterface item) {

        if (item.getRow() < minRow) {
            minRow = item.getRow();
        }
        if (item.getCol() < minCol) {
            minCol = item.getCol();
        }
        if (item.getRow() > maxRow) {
            maxRow = item.getRow();
        }
        if (item.getCol() > maxCol) {
            maxCol = item.getCol();
        }




        Row r = getRow(item.getRow());
        if (r == null) {
            r = new Row();
            rows.put(item.getRow(), r);
        }
        BoardCell c = getCell(item.getRow(), item.getCol());
        if (c == null) {
            c = new BoardCell();
            r.cols.put(item.getCol(), c);
        }
        c.items.add(item);
        item.setOnBoard(true);
        item.updateRealPos(this);
        getItems().add(item);
        _fireMoveEvent(item);

    }

    public abstract List<CellItemInterface> getNeighbors(int i_row, int j_col);
    
    /*{
        List<CellItemInterface> neighList = new ArrayList<CellItemInterface>();
        for(int i =-1;i<2;i++){
            for(int j =-1;j<2;j++){
                if(i==0 && j==0) continue;
                List<CellItemInterface> itemNeighbors = getItems(i_row+i, j_col+j); 
                if (itemNeighbors != null) {
                    neighList.addAll(itemNeighbors);
                }
            }
        }
        
        
        return neighList;
    }*/

    CellItemInterface getOther(CellItemInterface item) {
        BoardCell c = getCell(item.getRow(), item.getCol());
        if (c.items.size() < 2) {
            return null;
        }
        if (c.items.get(0).equals(item)) {
            return c.items.get(1);
        } else {
            return c.items.get(0);
        }
    }
    //Add step distance
    public abstract int getDistance(CellItemInterface item, CellItemInterface other);
    
    
    public abstract int  getDistance(int i, int j, int x, int y);
    
    protected abstract  int getDistanceManhattan(int i, int j, int x, int y);
    
    protected abstract int getDistanceStep(int i, int j, int x, int y);


    public int itemCount() {
        return getItems().size();
    }

    void move(CellItemInterface item, int deltaI, int deltaJ) {
        
        if (deltaI == 0 && deltaJ == 0) {
            return;
        }
        moveCount++;
        BoardCell previous = getCell(item.getRow(), item.getCol());


        previous.getItems().remove(item);

        item.setRow(item.getRow() + deltaI);
        item.setCol(item.getCol() + deltaJ);

        item.updateRealPos(this);

        addItem(item);
        
        _fireMoveEvent(item);
    }

    void removeItem(CellItemInterface item) {
        item.setOnBoard(false);
        BoardCell previous = getCell(item.getRow(), item.getCol());
        previous.getItems().remove(item);
        this.getItems().remove(item);
        
        _fireMoveEvent(item, true);
    }

    private Row getRow(int row) {
        Row r = rows.get(row);
        if (r == null) {
            return null;
        } else {
            return r;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("");
        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                int itCount = itemCount(i, j);
                if (itCount == 0) {
                    sb.append("|.\t");
                } else {
                    sb.append("|" + getItems(i, j).get(0).toString());
                    if (itCount > 1) {
                        sb.append("+\t");
                    } else {
                        sb.append("\t");
                    }
                }

            }
            sb.append("|\n");

        }
        return sb.toString();

    }

    public int getMoveCount() {
        return moveCount;
    }
    
    public LinkedHashSet<CellItemInterface> getItems() {
        return items;
    }


    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }
    
    /////////////////
    //Event control//
    /////////////////
    public synchronized void addMoveListener( MoveListener l ) {
        //l.setBoard(this);
        _listeners.add( l );
        
    }
    
    public synchronized void removeMoveListener( MoveListener l ) {
        _listeners.remove( l );
    }
     
    protected synchronized void _fireMoveEvent(CellItemInterface item) {
        MoveEventCell move = new MoveEventCell( this, item );
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (MoveListener) listeners.next() ).moveReceived( move );
        }
    }
    
    protected  synchronized void _fireMoveEvent(CellItemInterface item, boolean remove) {
        MoveEventCell move = new MoveEventCell( this, item, remove );
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (MoveListener) listeners.next() ).moveReceived( move );
        }
    }

    

    
    
}
class Row {

    Map<Integer, BoardCell> cols = new HashMap<Integer, BoardCell>();
}



