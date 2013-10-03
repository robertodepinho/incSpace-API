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
public class HexBoard extends AbstractBoard  {
    
    @Override
    public List<CellItemInterface> getNeighbors(int i_row, int j_col) {
        List<CellItemInterface> neighList = new ArrayList<CellItemInterface>();
        for(int i =-1;i<2;i++){
            for(int j =-1;j<2;j++){
                if(i==0 && j==0 || i==1 && j==-1 || i==-1 && j==1  ) continue;
                List<CellItemInterface> itemNeighbors = getItems(i_row+i, j_col+j); 
                if (itemNeighbors != null) {
                    neighList.addAll(itemNeighbors);
                }
            }
        }
        
        
        return neighList;
    }

    //Add step distance
    @Override
    public int getDistance(CellItemInterface item, CellItemInterface other) {
       // return getDistance(item.getRow(), item.getCol(), item.getZed(),   
       //         other.getRow(), other.getCol(), other.getZed());
      return getDistance(item.getRow(), item.getCol(),   
                other.getRow(), other.getCol());
    }

    
    @Override
    public int getDistance(int i, int j, int x, int y) {
        int k, z;
        k = j -i;
        z = y - x;        
        return getDistanceStep( i, j, k, x,  y, z);
    }
    
    
    public int getDistance(int i, int j, int k,  int x, int y, int z) {
        return getDistanceStep( i, j, k, x,  y, z);
    }
    
    protected static int getDistanceManhattan(int i, int j, int k, 
                                                    int x, int y, int z) {
        return Math.abs(i - x) +
                Math.abs(j - y) +
                Math.abs(k - z);
    }
    
    protected int getDistanceStep(int i, int j, int k, 
                                                int x, int y, int z) {
    //  min{|xi-xj| , |yi-yj|} + | |xi-xj| - |yi-yj| |    
        return Math.max(Math.abs(k - z),
                            Math.max(Math.abs(i - x),Math.abs(j - y)));
    }

    protected int getDistanceStep(int i, int j, 
                                                int x, int y) {
    
        int k, z;
        k = j -i;
        z = y - x;        
        return getDistanceStep(i, j, k, x, y, z);
        
    }

    @Override
    protected int getDistanceManhattan(int i, int j, int x, int y) {
        int k, z;
        k = j -i;
        z = y - x;        
        return getDistanceManhattan(i, j, k, x, y, z);
        
    }

    

    
    

    
    
}


