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

import java.util.LinkedList;

/**
 *
 * @author robertopinho
 */
public abstract class AbstractCell implements CellItemInterface {
    private Integer col = 0;
    private Integer row = 0;
    //private Integer zed = 0;  //HEX Board third axis
    private LinkedList<itemDistanceRn> closeNeighbors = new LinkedList<itemDistanceRn>();
    private LinkedList<itemDistanceRn> randomNeighbors = new LinkedList<itemDistanceRn>();
    private boolean onBoard;
    

    //Real Position data
    private double realRow =0.0;
    private double realCol =0.0;

    

    public LinkedList<itemDistanceRn> getCloseNeighbors() {
        return closeNeighbors;
    }

    public LinkedList<itemDistanceRn> getRandomNeighbors() {
        return randomNeighbors;
    }

    public boolean isOnBoard(){
        return this.onBoard;
    }
    
    public void setOnBoard(boolean b) {
        this.onBoard = b;
    }

    public void setRow(Integer i) {
        this.row=i;
    }

    public void setCol(Integer j) {
        this.col=j;
    }

    public Integer getRow() {
        return this.row;
    }

    public Integer getCol() {
        return this.col;
    }

    public void updateRealPos(AbstractBoard board){
            double sumD  = 0.0;
            int k=0;
            double Cx = 0.0;
            double Cy = 0.0;
            //System.out.println("*********item:" + this.getLabel());
            //System.out.println("cr:" + this.getCol() +";\t" + this.getRow());
            for(CellItemInterface other:
                board.getNeighbors(getRow(), getCol())){
                sumD += this.getDistance(other);
                k++;
                Cx += other.getRealCol();
                Cy += other.getRealRow();


            }
            if(k==0) k=1;
            Cx = Cx/k;
            Cy = Cy/k;
            //System.out.println("C:" + Cx +";\t" + Cy);
            if(sumD==0){
                realRow=Cy;
                realCol=Cx;
                return;
            }
        double gX;
        double gY;
        double dX = 0.0;
        double dY =0.0;
        Double d;
        double wX;
        double wY;
        double beta = board.getBeta();

            for(CellItemInterface other:
                board.getNeighbors(getRow(), getCol())){

                    gX = 0;//other.getRealCol() - Cx; //gX =  0;//other.getRealCol() - Cx;
                    gY = 0;//other.getRealRow() - Cy; //gY =  0;//other.getRealRow() - Cy;
                    wX = getCol()-other.getCol();
                    wY = getRow()-other.getRow();
                    d = this.getDistance(other);

                    if(wX!=0 && wY!=0){
                        wX = wX * Math.sqrt(2.0)/2.0;
                        wY = wY * Math.sqrt(2.0)/2.0;
                    }

                    //Aqui cabe colocar um fator Beta de ajuste de d : Beta * (d-gx)
                    //Beta reflete espalhanmento do mapa

                    dX += wX * beta * (d-gX) * (d/sumD);
                    dY += wY * beta * (d-gY) * (d/sumD);
                    /* System.out.println("other:" + other.getLabel());
                    System.out.println("cr:" + other.getCol() +";\t" + other.getRow());
                    System.out.println("w:" + wX +";\t" + wY);
                    System.out.println("D:" + dX +";\t" + dY);
                    System.out.println("d:" + d ); */

                }
                realCol = Cx + dX;
                realRow = Cy + dY;
    }
    public double getRealRow(){
        return realRow;
    }
    public double getRealCol(){
        return realCol;
    }


    

}
