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
import java.util.List;


/**
 *
 * @author robertopinho
 */
public class Board   extends AbstractBoard {
    
    public List<CellItemInterface> getNeighbors(int i_row, int j_col) {
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
    }

    //Add step distance
    public int getDistance(CellItemInterface item, CellItemInterface other) {
        return getDistance(item.getRow(), item.getCol(),   
                other.getRow(), other.getCol());
    }

    public int getDistance(int i, int j, int x, int y) {
        return getDistanceStep( i, j, x,  y);
    }
    
    protected  int getDistanceManhattan(int i, int j, int x, int y) {
        return Math.abs(i - x) +
                Math.abs(j - y);
    }
    
    protected int getDistanceStep(int i, int j, int x, int y) {
    //  min{|xi-xj| , |yi-yj|} + | |xi-xj| - |yi-yj| |    
        return Math.max(Math.abs(i - x),Math.abs(j - y));
    }



    
    
}


