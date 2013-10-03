/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package incboard.engine;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author robertopinho
 */
public class HexPlacer extends Placer {
    
    public HexPlacer(AbstractBoard board){
        super(board);
        neighborSize = 6;
    }
    
        
     
    @Override
    public void removeItem(CellItemInterface item) {

        
        this.item = item;

        List<BoardPos> blockPos = new ArrayList<BoardPos>();



        this.board.removeItem(this.item);

        List<CellItemInterface> candidates = new ArrayList<CellItemInterface>();

        BoardPos currentPos = new BoardPos(item.getRow(), item.getCol(), 0);
        while (true) {

            //System.err.println(board);

            int deltaCol = (item.getCol() > 0) ? 1 : -1;
            int deltaRow = (item.getRow() > 0) ? 1 : -1;
            BoardCell[] c; 

            if(deltaCol==deltaRow){
                c = new BoardCell[1];
                c[0] = board.getCell(currentPos.i + deltaRow, 
                                         currentPos.j + deltaCol);
            } else {
                c = new BoardCell[2];
                c[0] = board.getCell(currentPos.i, currentPos.j + deltaCol);
                c[1] = board.getCell(currentPos.i + deltaRow, currentPos.j);
            }
            
            for (int i = 0; i < c.length; i++) {
                if (c[i] != null) {
                    if (c[i].items.size() > 0) {
                        candidates.add(c[i].items.get(0));
                    }
                }
            }
            if (candidates.size() == 0) {
                return;
            }

            //check erro for each
            float minError = Float.MAX_VALUE;
            CellItemInterface minErrorItem = null;

            for (CellItemInterface candItem : candidates) {
                GridError err = difXY(candItem, currentPos.i - candItem.getRow(),
                        currentPos.j - candItem.getCol());
                if (err.weightedErr < minError) {
                    minError = err.weightedErr;
                    minErrorItem = candItem;
                }
            }

            candidates.clear();

            //move the one with least error
            BoardPos newPos = new BoardPos(minErrorItem.getRow(), minErrorItem.getCol(), 0);
            board.move(minErrorItem, currentPos.i - minErrorItem.getRow(),
                    currentPos.j - minErrorItem.getCol());

            //add block pos
            blockPos.add(currentPos);

            //new pos
            currentPos = newPos;
            //if blocked return
            if (blockPos.contains(newPos)) {
                return;
            }
        }


    }

    
    @Override
    protected boolean validDelta(int i, int j) {
        if((i==0 && j==0)||(i==-1 && j==1)||(i==1 && j==-1)){
            return false;
            
        } else {
            return true;
        }
    }
    
}

  

