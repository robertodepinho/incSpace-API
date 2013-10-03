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



import java.util.EventObject;

/**
 *
 * @author robertopinho
 */
public class MoveEventCell extends EventObject {
    private CellItemInterface item;
    private boolean remove = false;
    
    public MoveEventCell(Object source){
        super(source);
    }

    public MoveEventCell(Object source, CellItemInterface item) {
        super(source);
        this.item = item;
        
    }
    
    public MoveEventCell(Object source, CellItemInterface item, boolean remove) {
        super(source);
        this.item = item;
        this.remove = remove;
        
    }
    
    
    public CellItemInterface movedItem(){
        return this.item;
        
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

}
