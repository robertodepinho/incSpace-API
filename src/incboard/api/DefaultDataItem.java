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



/**
 *
 * @author RobertoPinho
 */
public class DefaultDataItem implements  DataItemInterface {
    private String uRI;
    private int x;
    private int y;
    private double rX;
    private double rY;

    public DefaultDataItem(String uRI){
        this.uRI = uRI;
    }
    
    public String getURI() {
        return uRI;
    }

    public int getCol() {
        return this.x;
    }

    public int getRow() {
        return this.y;
    }


    public void setCol(int x) {
        this.x = x;
    }

    public void setRow(int y) {
        this.y = y;
    }

    public Double getDistance(DataItemInterface other) {
        return Math.abs(1.0*Integer.parseInt(uRI)- Integer.parseInt(other.getURI()));
    }

    public double getRealRow() {
        return rY;
    }

    public double getRealCol() {
        return rX;
    }

    public void setRealCol(double x) {
        this.rX =x;

    }

    public void setRealRow(double y) {
        this.rY = y;

    }

    
    
}
