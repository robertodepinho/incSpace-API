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
package incboard.api;

import incboard.engine.AbstractBoard;
import incboard.engine.Board;
import incboard.engine.CellItemInterface;
import incboard.engine.DataCell;
import incboard.engine.HexBoard;
import incboard.engine.HexPlacer;
import incboard.engine.MoveEventCell;
import incboard.engine.MoveListener;
import incboard.engine.Placer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author RobertoPinho
 */
public class IncBoard implements MoveListener {

    private DataModelInterface dataModel;
    private List _listeners = new ArrayList();
    private AbstractBoard board;
    private Placer placer;
    private HashMap<DataItemInterface, CellItemInterface> dataCellMap =
            new HashMap<DataItemInterface, CellItemInterface>();

    public IncBoard(DataModelInterface dataModel, boolean useHex) {
        this.dataModel = dataModel;
        this.addMoveListener(dataModel);
        if (useHex) {
            this.board = new HexBoard();
            this.placer = new HexPlacer(this.board);
        } else {
            this.board = new Board();
            this.placer = new Placer(this.board);
        }

        this.board.addMoveListener(this);
    }

    public void add(DataItemInterface item) {
        placer.addItem(getCell(item));
    }

    public void add(List<DataItemInterface> items) {
        for (DataItemInterface item : items) {
            add(item);
        }
    }

    public void remove(DataItemInterface item) {
        placer.removeItem(getCell(item));
    }

    public void remove(List<DataItemInterface> items) {
        for (DataItemInterface item : items) {
            add(item);
        }
    }

    /////////////////
    //Event control//
    /////////////////
    public synchronized void addMoveListener(DataModelInterface l) {

        _listeners.add(l);

    }

    public synchronized void removeMoveListener(DataModelInterface l) {
        _listeners.remove(l);
    }

    protected synchronized void _fireMoveEvent(DataItemInterface item) {
        MoveEvent move = new MoveEvent(this, item);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((DataModelInterface) listeners.next()).moveReceived(move);
        }
    }

    private CellItemInterface getCell(DataItemInterface item) {
        CellItemInterface cell = dataCellMap.get(item);
        if (cell == null) {
            cell = new DataCell(item);
            dataCellMap.put(item, cell);
        }
        return cell;
    }

    public void moveReceived(MoveEventCell event) {
        DataItemInterface dataItem = ((DataCell) event.movedItem()).getDataItem();
        dataItem.setCol(event.movedItem().getCol());
        dataItem.setRow(event.movedItem().getRow());
        dataItem.setRealCol(event.movedItem().getRealCol());
        dataItem.setRealRow(event.movedItem().getRealRow());
        _fireMoveEvent(dataItem);
    }
}
