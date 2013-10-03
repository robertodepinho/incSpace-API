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

package incboard.demo;

import incboard.api.DataItemInterface;
import incboard.api.DataModelInterface;
import incboard.api.DefaultDataItem;
import incboard.api.IncBoard;
import incboard.api.MoveEvent;
import incboard.engine.DataCell;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author RobertoPinho
 */
public class SimpleDemo implements DataModelInterface {
    
    public static void main(String[] args) {
        SimpleDemo sDemo = new SimpleDemo();
        sDemo.run();
    }

    List<DefaultDataItem> items = new ArrayList<DefaultDataItem>();
    int minRow = Integer.MAX_VALUE;
    int maxRow = Integer.MIN_VALUE;
    int minCol = Integer.MAX_VALUE;
    int maxCol = Integer.MIN_VALUE;
    int count = 0;
    int STEP = 10;
    
    
    public void moveReceived(MoveEvent event) {
        count++;
        DataItemInterface moved = event.movedItem();
        if(moved.getRow()<minRow) minRow = moved.getRow();
        if(moved.getRow()>maxRow) maxRow = moved.getRow();
        if(moved.getCol()<minCol) minCol = moved.getCol();
        if(moved.getCol()>maxCol) maxCol = moved.getCol();
        if(count%STEP!=0) return;
        
        dumpData();
    }

    private void dumpData() {
        for(int r = minRow;r<=maxRow;r++) {
            for (int c = minCol; c <= maxCol; c++) {
                System.out.print("|");
                for(DefaultDataItem d:items){
                    if(d.getRow()==r && d.getCol()==c){
                        //System.out.print(d.getURI()+" ("+d.getRealCol()+";"+d.getRealRow()+")");
                        System.out.print(d.getURI()+" ");
                    }
                }
                System.out.print("\t");
            }
            System.out.println("");
        }
        System.out.println("============================================");
        System.out.print("\t");
        for(DefaultDataItem e:items){
                System.out.print(e.getURI()+"\t");
        }
        System.out.println("");
        double dist;
        for(DefaultDataItem d:items){
            System.out.print(d.getURI()+"\t");
            for(DefaultDataItem e:items){
                dist =  Math.sqrt(
                        Math.pow(d.getRealCol()-e.getRealCol(),2) +
                        Math.pow(d.getRealRow()-e.getRealRow(),2));
                System.out.print(dist+"\t");
            }
            System.out.println("");
        }
    }

    private void run() {
        IncBoard engine = new IncBoard(this, false);
        String rndStr;
        DefaultDataItem dCell;
        for(int i = 0;i<3;i++){
            rndStr = Integer.toString((int)(Math.random()*20));
            dCell = new DefaultDataItem(rndStr);
            engine.add(dCell);
            items.add(dCell);
        }
        dumpData();
    }
}
