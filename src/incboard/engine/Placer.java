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


import incboard.engine.Placer.AddMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 *
 * @author robertopinho
 */
public class Placer {
    private Cache cache;

    protected boolean useTrapRun = false;
    protected boolean useBoardNeighbors = false;
    protected boolean adjustListSize = true;
    private boolean adjustDropLocation = true;
    private boolean useFullModeForDropLocation = true;
    private boolean allwaysFullModeForDropLocation = true;
     
    protected int closeListSize = 24;
    private int randomListSize = 16;
    protected int adjustedListSize = closeListSize + randomListSize - 6;
    
    private boolean debug = true;
    protected CellItemInterface item;
    protected AbstractBoard board;
    public List<Double> movesByItem = new ArrayList<Double>();
    protected  int neighborSize =8;

    public Placer(AbstractBoard board){
        this.cache = new Cache(this);
        this.board = board;
    }
    
    public int getCloseListSize() {
        return closeListSize;
    }

    public void setCloseListSize(int closeListSize) {
        this.closeListSize = closeListSize;
    }

    public int getRandomListSize() {
        return randomListSize;
    }

    public void setRandomListSize(int randomListSize) {
        this.randomListSize = randomListSize;
    }

    public AddMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(AddMode currentMode) {
        this.currentMode = currentMode;
    }

    public static enum AddMode {

        FULL, FAST
    }

    
    
        ;

    public   AddMode currentMode    
         = AddMode
        .FULL  ;

    
            public void addItem(CellItemInterface item) {
        
            
            this.item = item;


            this.cache.flush();

            List<BoardPos> blockPos = new ArrayList<BoardPos>();
            LinkedList<itemDistanceRn> distanceList;

            if (adjustDropLocation && board.itemCount() > (closeListSize + randomListSize)) {
                
                if (currentMode == AddMode.FAST && allwaysFullModeForDropLocation) {
                    CellItemInterface refItem = getClosestItem(item);
                    item.setCol(refItem.getCol());
                    item.setRow(refItem.getRow());
                } else if (currentMode == AddMode.FULL && useFullModeForDropLocation) {
                    distanceList = this.cache.getDistanceList(item);
                    item.setCol(distanceList.getFirst().other.getCol());
                    item.setRow(distanceList.getFirst().other.getRow());
                } else {
                    updateNeighbors(item);
                    item.setCol(item.getCloseNeighbors().getFirst().other.getCol());
                    item.setRow(item.getCloseNeighbors().getFirst().other.getRow());
                }
            } else {
                item.setCol(0);
                item.setRow(0);
            }
            this.board.addItem(this.item);

            //System.out.println(board);
            CellItemInterface currentItem = this.item;
            int previousMoveCount = board.getMoveCount();
            while (this.board.itemCount(currentItem.getRow(), currentItem.getCol()) > 1) {
                currentItem = solveOverlap(currentItem, blockPos);

            }
            //if(board.itemCount()>10)
            movesByItem.add(((double) board.getMoveCount() - previousMoveCount) / board.itemCount());
            //System.gc();

    }

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
            BoardCell[] c = new BoardCell[3];

            c[0] = board.getCell(currentPos.i + deltaRow, currentPos.j + deltaCol);
            c[1] = board.getCell(currentPos.i, currentPos.j + deltaCol);
            c[2] = board.getCell(currentPos.i + deltaRow, currentPos.j);
            for (int i = 0; i < 3; i++) {
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

    public void setMode(AddMode m) {
        this.setCurrentMode(m);
    }

    private boolean addCloseNeighbor(CellItemInterface item, CellItemInterface other) {
        itemDistanceRn itemD = new itemDistanceRn();
        itemD.other = other;
        itemD.distanceR2 = board.getDistance(item, other);
        itemD.distanceRn = item.getDistance(other);
        if (item.getCloseNeighbors().size() < this.closeListSize ||
                itemD.distanceRn < item.getCloseNeighbors().getLast().distanceRn) {
            int index = Collections.binarySearch(item.getCloseNeighbors(), itemD);
            if (index < 0) {
                item.getCloseNeighbors().add(-index - 1, itemD);
                
                addCloseNeighbor(other, item);

                if (item.getCloseNeighbors().size() > this.closeListSize) {
                    item.getCloseNeighbors().remove(item.getCloseNeighbors().getLast());
                }
            } else {
                // add to the other
                return false;
            }
            // add to the other
            return true;
        
        }
        return false;
    }

    private CellItemInterface getClosestItem(CellItemInterface item) {
        CellItemInterface refItem=null;
        double minDist = Double.MAX_VALUE;
        int count=0;
        for(CellItemInterface other:board.getItems()){
            if(refItem==null && count>0){
                System.err.println("Null: "+count);
            }
            count++;
            if(item.equals(other)) continue;
            
            double dist = item.getDistance(other);
            if(dist<minDist){
                minDist = dist;
                refItem = other;
            }
        }
        
        return refItem;
    }

    private CellItemInterface getRandomItem(CellItemInterface item) {
        int rndIndex = (int) (Math.random() * board.itemCount());

            Iterator<CellItemInterface> it = board.getItems().iterator();
            
        CellItemInterface other = it.next();
            for (int count = 0; count < rndIndex; count++) {
                other = it.next();

            }

            if (other.equals(item)) {
                other = getRandomItem(item);
            }
        return other;
    }

    protected  boolean isTrapped(CellItemInterface item, List<BoardPos> blockPos) {
        List<CellItemInterface> neighborList = board.getNeighbors(item.getRow(), item.getCol());
        if (neighborList.size() < neighborSize) {
            return false;
        }
        for (CellItemInterface neighbor : neighborList) {
            if (!blockPos.contains(new BoardPos(neighbor))) {
                return false;
            }
        }
        return true;

    }

    protected CellItemInterface solveOverlap(CellItemInterface item, List<BoardPos> blockPos) { // weighted & untie & random start


        CellItemInterface other = this.board.getOther(item);


        if (getCurrentMode() == AddMode.FAST) { //update close Neighbors
            updateNeighbors(item);
            updateNeighbors(other);
        }

        int tie = 0;

        int minErr = Integer.MAX_VALUE;
        int minErrTie = Integer.MAX_VALUE;
        int minPos[] = new int[4];


        boolean movedOther = false;


        GridError otherErr;
        GridError currErr;



        //Move Other
        int iOther = 0;
        int jOther = 0;
        int i, j;

        otherErr = difXY(other, iOther, jOther);

        int randomSeedI = (int) (Math.random() * 3);
        int randomSeedJ = (int) (Math.random() * 3);

        int blockCount = 0;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                i = (randomSeedI + x) % 3 - 1;
                j = (randomSeedJ + y) % 3 - 1;
                if (!validDelta(i, j)) {
                    continue;
                }
                BoardPos currentPos = new BoardPos(item.getRow() + i, item.getCol() + j, 0);


                if (blockPos.contains(currentPos)) {
                    blockCount++;
                    continue;
                }
                currErr = difXY(item, i, j);
                int err = currErr.weightedErr +
                        otherErr.weightedErr;


                int errTie = currErr.posErr +
                        otherErr.posErr;


                if (err == minErr) {
                    tie++;
                }

                if ((err < minErr) ||
                        ((err == minErr) && (errTie < minErrTie))) {
                    minErr = err;
                    minErrTie = errTie;

                    movedOther = false;


                    minPos[0] = i;
                    minPos[1] = j;
                    minPos[2] = iOther;
                    minPos[3] = jOther;

                }
            }
        }


        if (blockCount == neighborSize) {
            return solveTrap(item, blockPos);
        }

        //Keep current on center
        i = 0;
        j = 0;

        blockCount = 0;

        int randomSeedOtI = (int) (Math.random() * 3);
        int randomSeedOtJ = (int) (Math.random() * 3);




        currErr = difXY(item, i, j);

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                iOther = (randomSeedOtI + x) % 3 - 1;
                jOther = (randomSeedOtJ + y) % 3 - 1;

                if (!validDelta(iOther, jOther)) {
                    continue;
                }
                BoardPos currentPos = new BoardPos(other.getRow() + iOther, other.getCol() + jOther, 0);
                if (blockPos.contains(currentPos)) {
                    blockCount++;
                    continue;

                }
                otherErr = difXY(other, iOther, jOther);

                int err = currErr.weightedErr +
                        otherErr.weightedErr;


                int errTie = currErr.posErr +
                        otherErr.posErr;

                if (err == minErr) {
                    tie++;
                }

                if ((err < minErr) ||
                        ((err == minErr) && (errTie < minErrTie))) {
                    minErr = err;
                    minErrTie = errTie;
                    movedOther = true;
                    minPos[0] = i;
                    minPos[1] = j;
                    minPos[2] = iOther;
                    minPos[3] = jOther;

                }
            }
        }


        if (blockCount == neighborSize) {
            return solveTrap(item, blockPos);
        }

        BoardPos currentPos = new BoardPos(item.getRow(), item.getCol(), 0);
        blockPos.add(currentPos);





        if (movedOther) {
            board.move(other, minPos[2], minPos[3]);
            return other;
        } else {
            board.move(item, minPos[0], minPos[1]);
            return item;
        }

    }
    
    protected boolean validDelta(int i, int j) {
        if((i==0 && j==0)){
            return false;
            
        } else {
            return true;
        }
    }
    

    GridError difXY(CellItemInterface item, int x, int y) {
        
        //if(currentMode == AddMode.FULL)
                return difXY_v20(item, x, y);
        //else 
        //        return difXY_v10(item, x, y);
    }    
        
    GridError difXY_v20(CellItemInterface item, int x, int y) {

        Iterator<CellItemInterface> it;
        Set<CellItemInterface> baseList;

        LinkedList<itemDistanceRn> distanceList = 
                this.cache.getDistanceList(item);
        
        //Evaluate Dist and Pos in R2
        for(itemDistanceRn itemD : distanceList){
            int dist = board.getDistance(
                    itemD.other.getRow(), itemD.other.getCol(), 
                    item.getRow() + x, item.getCol() + y);
            itemD.distanceR2 = dist;
        }
        
        Collections.sort(distanceList, new Comparator<itemDistanceRn>() {

            
            public int compare(itemDistanceRn o1, itemDistanceRn o2) {
                return o1.distanceR2.compareTo(o2.distanceR2);
            }
        });
        //Evalute Pos
        int lastDist=Integer.MAX_VALUE;
        int lastPos = 0;
        for(int i=0;i<distanceList.size();i++){
            itemDistanceRn itemD = distanceList.get(i);
            if(itemD.distanceR2==lastDist){
                itemD.posR2= lastPos;
            } else {
                itemD.posR2 = i+1;
                lastPos = i+1;
                lastDist = itemD.distanceR2;
            }
            //System.err.println(itemD.distanceR2 + "\tPos:"+ itemD.posR2+"\t"+ itemD.other.getLabel());
        }

        
        GridError gError = new GridError();
        debug = false;
        int count = board.itemCount()-1;//distanceList.size();
        for(itemDistanceRn itemD : distanceList){
            if (itemD.posR2 != itemD.posRn) {

                gError.weightedErr += Math.abs(itemD.posR2 - itemD.posRn) * (count - itemD.posRn+1); 
                //gError.weightedErr += Math.abs(posR2[k] - posRn[k]) * (count - k);
                gError.countWeightErr += count - itemD.posRn+1;
                gError.posErr += Math.abs(itemD.posR2 - itemD.posRn);
                gError.countErr++;
            }
        }
        
        return gError;
        
    }



    GridError difXY_v10(CellItemInterface item, int x, int y) {

        Iterator<CellItemInterface> it;
        Set<CellItemInterface> baseList;
if (this.getCurrentMode() == AddMode.FAST) {

            baseList = new HashSet<CellItemInterface>();
            for (itemDistanceRn iDRn : item.getCloseNeighbors()) {
                baseList.add(iDRn.other);
            }
            
        
        if (useBoardNeighbors) {
                List<CellItemInterface> b = board.getNeighbors(item.getRow() + x, item.getCol() + y);
                b.remove(item);
                baseList.addAll(b);  //variable size list.....
            }
            if (adjustListSize) {
               itemDistanceRn iDRn;
               int i =0;
               while(baseList.size()<adjustedListSize && i<item.getRandomNeighbors().size()){
                   iDRn = item.getRandomNeighbors().get(i);
                   i++;
                   baseList.add(iDRn.other);
               }
            } else {
                for (itemDistanceRn iDRn : item.getRandomNeighbors()) {
                    baseList.add(iDRn.other);
                }
            }
            //System.err.println("BaseList:\t"+ baseList.size() 
            //        + "\tRandom:"+ item.getRandomNeighbors().size()
            //        + "\tNeighbors:"+ item.getCloseNeighbors().size());
            it = baseList.iterator();
        } else {

            it = board.getItems().iterator();
        }

        
        
        GridError gError = new GridError();
        debug = false;

        int count = board.itemCount();

        //Distance on Board and on Rn
        int distR2[] = new int[count];
        int posR2[] = new int[count];
        List<Integer> listR2 = new ArrayList<Integer>();

        double distRn[] = new double[count];
        int posRn[] = new int[count];
        CellItemInterface[] refOther = new CellItemInterface[count];
        List<Double> listRn = new ArrayList<Double>();


        CellItemInterface other;
        int k = 0;
        for (; it.hasNext();) {
            other = it.next();
            if (other.equals(item)) {
                continue;
            }
            if (k >= count) {
                break;
            }
            distR2[k] = board.getDistance(other.getRow(), other.getCol(), item.getRow() + x, item.getCol() + y);

            listR2.add(new Integer(distR2[k]));

            distRn[k] = item.getDistance(other);
            listRn.add(new Double(distRn[k]));
            refOther[k] = other;

            k++;
        }


        //Check this routine
        
        Collections.sort(listR2);
        Collections.sort(listRn);

        if (getCurrentMode() == AddMode.FULL) {
            item.getCloseNeighbors().clear();
        }
        for (k = 0; k < count - 1; k++) {
            posR2[k] = (listR2.indexOf(distR2[k]) + 1);

            posRn[k] = (listRn.indexOf(distRn[k]) + 1);


            if (getCurrentMode() == AddMode.FULL &&
                    posRn[k] < closeListSize) {    //if full mode and element in range
                //add to close neighbors list
                itemDistanceRn itemD = new itemDistanceRn();
                itemD.distanceRn = distRn[k];
                itemD.distanceR2 = distR2[k];
                itemD.other = refOther[k];
                item.getCloseNeighbors().add(itemD);
            }

            if (posR2[k] != posRn[k]) {

                gError.weightedErr += Math.abs(posR2[k] - posRn[k]) * (count - posRn[k]); //gError.weightedErr += Math.abs(posR2[k] - posRn[k]) * (count - k);
                gError.countWeightErr += count - k;
                gError.posErr += Math.abs(posR2[k] - posRn[k]);
                gError.countErr++;
            }

        }
        if (getCurrentMode() == AddMode.FULL) {
            Collections.sort(item.getCloseNeighbors());
        }

        return gError;

    }

    
    
 
    
    
  
    protected  CellItemInterface solveTrap(CellItemInterface item, List<BoardPos> blockPos) {
        return solveTrap(item, blockPos, useTrapRun);
    }

    protected  CellItemInterface solveTrap(CellItemInterface item, List<BoardPos> blockPos, boolean run) {
        //System.out.println("Trapped:" + item);

        BoardPos previousPos = new BoardPos(item.getRow(), item.getCol(), 0);
        blockPos.add(previousPos);
        
        int minI = -Integer.MAX_VALUE;
        int maxI = Integer.MAX_VALUE;

        int minJ = -Integer.MAX_VALUE;
        int maxJ = Integer.MAX_VALUE;

        boolean whileCondition;
        
        

        if (run) {
            whileCondition = (this.board.itemCount(item.getRow(), item.getCol()) > 1);
        } else {
            whileCondition = isTrapped(item, blockPos);
        }

        if (!whileCondition) {
            System.err.println("*************FALSE***");
        }
        int trapCount = 0;

        while (whileCondition) {
            trapCount++;
            //if(trapCount>50){
            System.err.println(trapCount + ":\t" + item.getRow() + "|" + item.getCol());
            //}
            if (getCurrentMode() == AddMode.FAST) { //update close Neighbors
                updateNeighbors(item);
            }

            int tie = 0;

            int minErr = Integer.MAX_VALUE;
            int minErrTie = Integer.MAX_VALUE;
            int minPos[] = new int[4];

            GridError currErr;



            //Move item
            int i, j;




            int blockCount = 0;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    i = x;
                    j = y;
                    if (!validDelta(i,j)) {
                        continue;
                    }

                    if (item.getRow() + i <= minI ||
                            item.getRow() + i >= maxI ||
                            item.getCol() + j <= minJ ||
                            item.getCol() + j >= maxJ) {
                        continue;
                    }



                    BoardPos currentPos = new BoardPos(item.getRow() + i, item.getCol() + j, 0);


                    currErr = difXY(item, i, j);
                    int err = currErr.weightedErr;


                    int errTie = currErr.posErr;


                    if (err == minErr) {
                        tie++;
                    }

                    if ((err < minErr) ||
                            ((err == minErr) && (errTie < minErrTie))) {
                        minErr = err;
                        minErrTie = errTie;
                        minPos[0] = i;
                        minPos[1] = j;
                    }
                }
            }

            if (minPos[0] == -1) { //moved up
                maxI = item.getRow();
            }
            if (minPos[0] == 1) { //moved down
                minI = item.getRow();
            }
            if (minPos[1] == -1) { //moved up
                maxJ = item.getCol();
            }
            if (minPos[1] == 1) { //moved up
                minJ = item.getCol();
            }

            //System.err.println(minPos[0] + "," + minPos[1] + ":\t" + minErr);

            //blockPos.add(new BoardPos(item));
            board.move(item, minPos[0], minPos[1]);
            if (run) {
                whileCondition = (this.board.itemCount(item.getRow(), item.getCol()) > 1);
            } else {
                whileCondition = isTrapped(item, blockPos);
            }
        }
        //System.out.println(board);

        return item;


    }

    protected void updateNeighbors(CellItemInterface item) {
        if (true) {
            updateNeighborsNewImplementation(item);
        } else {
            updateNeighborsDefault(item);
        }
    }

    private void updateNeighborsNewImplementation(CellItemInterface item) {
        boolean newItem = (item.getCloseNeighbors().size() == 0);

        item.getRandomNeighbors().clear();
        CellItemInterface other;

        if (!newItem) {
            List<itemDistanceRn> oldNeighbors = new ArrayList<itemDistanceRn>();
            for (itemDistanceRn neighbor : item.getCloseNeighbors()) {
                if (!neighbor.other.isOnBoard()) {
                    oldNeighbors.add(neighbor);
                }
            }
            item.getCloseNeighbors().removeAll(oldNeighbors);
        }
        

        while (item.getRandomNeighbors().size() < this.randomListSize) {
            other = getRandomItem(item);
            if (!addCloseNeighbor(item, other)) {
                itemDistanceRn itemD = new itemDistanceRn();
                itemD.other = other;
                itemD.distanceR2 = board.getDistance(item, other);
                itemD.distanceRn = item.getDistance(other);
                item.getRandomNeighbors().add(itemD);
            }

        }


        if (newItem) {    //if new item, try to improve the close list 
            //using the closest neighbors lits'
            itemDistanceRn previousClosestItem = null;

            while (item.getCloseNeighbors().getFirst() != previousClosestItem) {
                previousClosestItem = item.getCloseNeighbors().getFirst();
                for (itemDistanceRn itemD : previousClosestItem.other.getCloseNeighbors()) {
                    other = itemD.other;
                    addCloseNeighbor(item, other);

                }
            }
        }
    }

    private void updateNeighborsDefault(CellItemInterface item) {
        boolean newItem = (item.getCloseNeighbors().size() == 0);

        item.getRandomNeighbors().clear();
        CellItemInterface other;
        while (item.getRandomNeighbors().size() < this.randomListSize) {
            int rndIndex = (int) (Math.random() * board.itemCount());

            Iterator<CellItemInterface> it = board.getItems().iterator();

            other = it.next();
            for (int count = 0; count < rndIndex; count++) {
                other = it.next();

            }

            if (other.equals(item)) {
                continue;
            }
            itemDistanceRn itemD = new itemDistanceRn();
            itemD.other = other;
            itemD.distanceR2 = board.getDistance(item, other);
            itemD.distanceRn = item.getDistance(other);
            if (item.getCloseNeighbors().size() == 0 ||
                    itemD.distanceRn < item.getCloseNeighbors().getLast().distanceRn) {
                int index = Collections.binarySearch(item.getCloseNeighbors(), itemD);
                if (index < 0) {
                    item.getCloseNeighbors().add(-index - 1, itemD);

                    if (item.getCloseNeighbors().size() > this.closeListSize) {
                        item.getCloseNeighbors().remove(item.getCloseNeighbors().getLast());
                    }
                }
            // add to the other
            } else {
                item.getRandomNeighbors().add(itemD);
            }

        }


        if (newItem) {    //if new item, try to improve the close list 
            //using the closest neighbors lits'
            itemDistanceRn previousClosestItem = null;

            while (item.getCloseNeighbors().getFirst() != previousClosestItem) {
                previousClosestItem = item.getCloseNeighbors().getFirst();
                for (itemDistanceRn itemD : previousClosestItem.other.getCloseNeighbors()) {
                    other = itemD.other;
                    itemDistanceRn newItemD = new itemDistanceRn();
                    newItemD.other = other;
                    newItemD.distanceR2 = board.getDistance(item, other);
                    newItemD.distanceRn = item.getDistance(other);
                    if (newItemD.distanceRn < item.getCloseNeighbors().getLast().distanceRn) {
                        int index = Collections.binarySearch(item.getCloseNeighbors(), newItemD);
                        if (index < 0) {
                            item.getCloseNeighbors().add(-index - 1, newItemD);

                            if (item.getCloseNeighbors().size() > this.closeListSize) {
                                item.getCloseNeighbors().remove(item.getCloseNeighbors().getLast());
                            }
                        }

                    }
                }
            }
        }
    }
}
class itemDistanceR2 implements Comparable<itemDistanceR2> {

    public CellItemInterface other;
    public int distanceR2;
    public double distanceRn;

    public int compareTo(itemDistanceR2 o) {
        return new Integer(this.distanceR2).compareTo(((itemDistanceR2) o).distanceR2);
    }

    @Override
    public String toString() {
        return other.toString() + "\t" + distanceR2 + "\t" + distanceRn;
    }
    }

class GridError {

    public int countErr = 0;
    public int weightedErr = 0;
    public int posErr = 0;
    public int countWeightErr = 0;
    }

class Cache {
    private Placer placer;
    private HashMap<CellItemInterface,HashSet<CellItemInterface>> baseListCache
            = new HashMap<CellItemInterface, HashSet<CellItemInterface>>();
    private HashMap<CellItemInterface,LinkedList<itemDistanceRn>> distanceListCache
            = new HashMap<CellItemInterface, LinkedList<itemDistanceRn>>();

    Cache(Placer placer) {
        this.placer = placer;
    }

    void flush(){
        baseListCache.clear();
        distanceListCache.clear();
    }
    
    public LinkedList<itemDistanceRn> getDistanceList(CellItemInterface item){
        LinkedList<itemDistanceRn> distanceList;
        
        distanceList = distanceListCache.get(item);
        
        if(distanceList!=null) return distanceList;
        
        Iterator<CellItemInterface> it = getBaseListIterator(item);
        CellItemInterface other;
        distanceList = 
                    new LinkedList<itemDistanceRn>();
        
        for (; it.hasNext();) {
            other = it.next();
            if (other.equals(item)) {
                continue;
            }
            itemDistanceRn itemD = new itemDistanceRn();
            itemD.other = other;
            itemD.distanceRn = item.getDistance(other);
            distanceList.add(itemD);
        }
        Collections.sort(distanceList);
        
        double lastDist=Double.POSITIVE_INFINITY;
        int lastPos = 0;
        
        //Improve Close neighbors list with full neighbors
        if(placer.getCurrentMode()== Placer.AddMode.FULL){
            item.getCloseNeighbors().clear();
        }
        
        for(int i=0;i<distanceList.size();i++){
            itemDistanceRn itemD = distanceList.get(i);
            if(itemD.distanceRn==lastDist){
                itemD.posRn= lastPos;
            } else {
                itemD.posRn = i+1;
                lastPos = i+1;
                lastDist = itemD.distanceRn;
            }
            if(placer.getCurrentMode()== Placer.AddMode.FULL &&
                    i< placer.closeListSize){
                item.getCloseNeighbors().add(itemD);
            }
            
            //System.err.println(itemD.distanceRn + "\tPos:"+ itemD.posRn+"\t"+ itemD.other.getLabel());
        }
        distanceListCache.put(item, distanceList);
        
        
        return distanceList;
    }
    
    Iterator<CellItemInterface>  getBaseListIterator(CellItemInterface item) {
        HashSet<CellItemInterface> baseList;
        Iterator<CellItemInterface> it;
        
        
        if (placer.getCurrentMode() == AddMode.FAST) {

            baseList = getBaseList(item);
            
            it = baseList.iterator();
        } else {

            it = placer.board.getItems().iterator();
        }
        return it;
    }

    private HashSet<CellItemInterface> getBaseList(CellItemInterface item) {
            HashSet<CellItemInterface> baseList; 
            
            baseList = baseListCache.get(item);
            
            if(baseList!=null) return baseList;
            
            baseList = new HashSet<CellItemInterface>();
            for (itemDistanceRn iDRn : item.getCloseNeighbors()) {
                baseList.add(iDRn.other);
            }
            
            if (placer.useBoardNeighbors) {
                List<CellItemInterface> b = placer.board.getNeighbors(item.getRow() , item.getCol());
                b.remove(item);
                baseList.addAll(b);  //variable size list.....
            }
            if (placer.adjustListSize) {
               itemDistanceRn iDRn;
               int i =0;
               while(baseList.size()<placer.adjustedListSize && i<item.getRandomNeighbors().size()){
                   iDRn = item.getRandomNeighbors().get(i);
                   i++;
                   baseList.add(iDRn.other);
               }
            } else {
                for (itemDistanceRn iDRn : item.getRandomNeighbors()) {
                    baseList.add(iDRn.other);
                }
            }
            baseListCache.put(item, baseList);
            return baseList;
            //System.err.println("BaseList:\t"+ baseList.size() 
            //        + "\tRandom:"+ item.getRandomNeighbors().size()
            //        + "\tNeighbors:"+ item.getCloseNeighbors().size());
    }
    
}