/**
 * NoiseMap is a scientific computation plugin for OrbisGIS developed in order to
 * evaluate the noise impact on urban mobility plans. This model is
 * based on the French standard method NMPB2008. It includes traffic-to-noise
 * sources evaluation and sound propagation processing.
 *
 * This version is developed at French IRSTV Institute and at IFSTTAR
 * (http://www.ifsttar.fr/) as part of the Eval-PDU project, funded by the
 * French Agence Nationale de la Recherche (ANR) under contract ANR-08-VILL-0005-01.
 *
 * Noisemap is distributed under GPL 3 license. Its reference contact is Judicaël
 * Picaut <judicael.picaut@ifsttar.fr>. It is maintained by Nicolas Fortin
 * as part of the "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/>.
 *
 * Copyright (C) 2011 IFSTTAR
 * Copyright (C) 2011-2012 IRSTV (FR CNRS 2488)
 *
 * Noisemap is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Noisemap is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Noisemap. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.noisemap.core;

import org.locationtech.jts.algorithm.CGAlgorithms3D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PropagationPath work for FastObstructionTest,
 * @author Nicolas Fortin
 * @author Pierre Aumond
 */
public class PropagationPath {
    private List<SRPath> srList;
    private List<PointPath> pointList;
    private List<SegmentPath> segmentList;
    private boolean favorable;

    /**
     * parameters given by user
     * @param favorable
     * @param pointList
     * @param segmentList
     */
    public PropagationPath(boolean favorable, List<PointPath> pointList, List<SegmentPath> segmentList , List<SRPath> srList) {
        this.favorable = favorable;
        this.pointList = pointList;
        this.segmentList = segmentList;
        this.srList = srList;
    }

    public static class PointPath {
        // given by user
        public final Coordinate coordinate;
        public final double altitude;
        public final double gs;
        public final double alphaWall;
        public final POINT_TYPE type;
        public enum POINT_TYPE {
            SRCE,
            REFL,
            DIFV,
            DIFH,
            RECV
        }

        // computed in Augmented Points
        //public final Coordinate coordinateprime = null;
        //public final double sigma;
        //public final double[] impedance = null;

        /**
         * parameters given by user
         * @param coordinate
         * @param altitude
         * @param gs
         * @param alphaWall
         * @param type
         */
        public PointPath(Coordinate coordinate, double altitude, double gs, double alphaWall, POINT_TYPE type) {
            this.coordinate = coordinate; // absolute coordinates
            this.altitude = altitude; // floor
            this.gs = gs;
            this.alphaWall = alphaWall;
            this.type = type;
        }
    }

    public static class SRPath {

        // given by user
        public final LineSegment lineSegment;

        // computed in AugmentedSRPath
        public Double dPath; // pass by points
        public Double d ; // direct ray between source and receiver
        public Double dc; // direct ray sensible to meteorological conditions (can be curve) between source and receiver
        public Double dp; // distance on mean plane between source and receiver
        public Double eLength; // distance between first and last diffraction point
        public Double delta; // distance between first and last diffraction point

        public SRPath(LineSegment lineSegment) {
            this.lineSegment = lineSegment;
        }


    }


    public static class SegmentPath {
        //  given by user
        public final double gPath;
        public final LineSegment lineSegment;

        // computed in AugmentedSegments
        public int idPtStart;
        public int idPtFinal;

        public Double gPathPrime = null;
        public Double gw = null;
        public Double gm = null;
        public Double zs = null;
        public Double zr = null;
        public Double zsPrime = null;
        public Double zrPrime = null;
        public Double testForm = null;
        public Double testFormPrime = null;
        public Double d = null; // direct ray
        public Double dc = null; // direct ray sensible to meteorological conditions
        public Double dp = null; // direct ray sensible to meteorological conditions

        /**
         * @param gPath
         */
        public SegmentPath(double gPath, LineSegment lineSegment) {
            this.gPath = gPath;
            this.lineSegment = lineSegment;
        }


        public void setGw(double g) {
            this.gw = g;
        }

        public void setGm(double g) {
            this.gm = g;
        }

        public Double getgPathPrime(PropagationPath path) {
            if(gPathPrime == null) {
                path.computeAugmentedSegments();
            }
            return gPathPrime;
        }

        public Double getGw() {
            return gw;
        }

        public Double getGm() {
            return gm;
        }

        public Double getZs(PropagationPath path, SegmentPath segmentPath) {
            if(zs == null) {
                zs = path.computeZs(segmentPath);
            }
            return zs;
        }

        public Double getZr(PropagationPath path, SegmentPath segmentPath) {
            if(zr == null) {
                zr = path.computeZr(segmentPath);
            }
            return zr;
        }

        public Double getZsPrime(PropagationPath path, SegmentPath segmentPath) {
            if(zsPrime == null) {
                zsPrime = path.computeZsPrime(segmentPath);
            }
            return zsPrime;
        }

        public Double getZrPrime(PropagationPath path, SegmentPath segmentPath) {
            if(zrPrime == null) {
                zrPrime = path.computeZrPrime(segmentPath);
            }
            return zrPrime;
        }

    }


    public List<PointPath> getPointList() {return pointList;}

    public List<SegmentPath> getSegmentList() {return segmentList;}

    public List<SRPath> getSRList() {return srList;}

    public PropagationPath(List<SegmentPath> segmentList) {
        this.segmentList = segmentList;
    }

    public boolean isFavorable() {
        return favorable;
    }


    public void computeAugmentedSRPath() {
        double dPath =0 ;

        SRPath SR = this.srList.get(0);


        // Projected source and receiver on MeanPlane
        Coordinate SGround = srList.get(0).lineSegment.project(pointList.get(0).coordinate);
        Coordinate RGround = srList.get(0).lineSegment.project(pointList.get(pointList.size()-1).coordinate);

        // Original absolute coordinates
        Coordinate S = pointList.get(0).coordinate;
        Coordinate R= pointList.get(pointList.size()-1).coordinate;

        // Symmetric coordinates
        Coordinate Sprime = new Coordinate(2*SGround.x - S.x,2*SGround.y - S.y,2*SGround.z - S.z);
        Coordinate Rprime = new Coordinate(2*RGround.x - R.x,2*RGround.y - R.y,2*RGround.z - R.z);

        SRPath SRp = new SRPath(new LineSegment(S,Rprime));
        SRPath SpR = new SRPath(new LineSegment(Sprime,R));

        SpR.d = CGAlgorithms3D.distance(Sprime, R);
        SRp.d = CGAlgorithms3D.distance(S, Rprime);
        SR.d = CGAlgorithms3D.distance(S, R);

        SpR.dp = CGAlgorithms3D.distance(SGround, RGround);
        SRp.dp = SpR.dp;
        SR.dp = SpR.dp;

        if (!this.favorable){
            for (int idPoint = 2; idPoint < pointList.size()-1; idPoint++) {
                dPath += CGAlgorithms3D.distance(pointList.get(idPoint - 1).coordinate, pointList.get(idPoint).coordinate);
            }
            if (pointList.size()>3){
                SR.eLength = dPath;
                SpR.eLength = dPath;
                SRp.eLength = dPath;
            }
            SR.dPath = dPath
                    + CGAlgorithms3D.distance(S, pointList.get(1).coordinate)
                    + CGAlgorithms3D.distance(pointList.get(pointList.size()-2).coordinate,R);
            SpR.dPath = dPath
                    + CGAlgorithms3D.distance(Sprime, pointList.get(1).coordinate)
                    + CGAlgorithms3D.distance(pointList.get(pointList.size()-2).coordinate,R);
            SRp.dPath = dPath
                    + CGAlgorithms3D.distance(S, pointList.get(1).coordinate)
                    + CGAlgorithms3D.distance(pointList.get(pointList.size()-2).coordinate,Rprime);

            SpR.dc = SpR.d;
            SRp.dc = SRp.d;
            SR.dc = SR.d;

        }
        else
        {
            for (int idPoint = 2; idPoint < pointList.size()-1; idPoint++) {
                dPath += getRayCurveLength(CGAlgorithms3D.distance(pointList.get(idPoint - 1).coordinate, pointList.get(idPoint).coordinate));
            }
            if (pointList.size()>2){
                SR.eLength = dPath;
                SpR.eLength = dPath;
                SRp.eLength = dPath;
            }

            SR.dPath = dPath
                    + getRayCurveLength(CGAlgorithms3D.distance(S, pointList.get(1).coordinate))
                    + getRayCurveLength(CGAlgorithms3D.distance(pointList.get(pointList.size()-2).coordinate, R));
            SpR.dPath = dPath
                    + getRayCurveLength(CGAlgorithms3D.distance(Sprime, pointList.get(1).coordinate))
                    + getRayCurveLength(CGAlgorithms3D.distance(pointList.get(pointList.size()-2).coordinate, R));
            SRp.dPath = dPath
                    + getRayCurveLength(CGAlgorithms3D.distance(S, pointList.get(1).coordinate))
                    + getRayCurveLength(CGAlgorithms3D.distance(pointList.get(pointList.size()-2).coordinate, Rprime));

            SR.dc = getRayCurveLength(SR.d);
            SpR.dc = getRayCurveLength(SpR.d);
            SRp.dc = getRayCurveLength(SRp.d);
        }


        SR.delta = SR.dPath - SR.dc;
        SRp.delta = SRp.dPath - SRp.dc;
        SpR.delta = SpR.dPath - SpR.dc;

        this.srList.add(SRp);
        this.srList.add(SpR);

    }

    public void initPropagationPath() {
        computeAugmentedSegments();
        computeAugmentedPoints();
        computeAugmentedSRPath();
    }


    private void computeAugmentedSegments() {
        for (int idSegment = 0; idSegment < segmentList.size(); idSegment++) {

            this.segmentList.get(idSegment).idPtStart = idSegment;
            this.segmentList.get(idSegment).idPtFinal = idSegment+1;

            double zs= segmentList.get(idSegment).getZs(this, this.segmentList.get(idSegment));
            this.segmentList.get(idSegment).zs  =zs;

            double zr = segmentList.get(idSegment).getZr(this, this.segmentList.get(idSegment));
            this.segmentList.get(idSegment).zr = zr;

            Coordinate SGround = pointList.get(idSegment).coordinate;
            Coordinate RGround = pointList.get(idSegment+1).coordinate;
            Coordinate S = pointList.get(idSegment).coordinate;
            Coordinate R = pointList.get(idSegment+1).coordinate;
            SGround.z = SGround.z - zs;
            RGround.z = RGround.z - zr;

            double dp = CGAlgorithms3D.distance(SGround, RGround);
            this.segmentList.get(idSegment).dp = dp;

            double d = CGAlgorithms3D.distance(S, R);
            this.segmentList.get(idSegment).d = d;

            if (!this.favorable){
                this.segmentList.get(idSegment).dc = d;
            }
            else
            {
                this.segmentList.get(idSegment).dc = getRayCurveLength(d);
            }

            double gs = pointList.get(0).gs;


            double testForm = dp / (30 * (zs + zr));
            this.segmentList.get(idSegment).testForm = testForm;


            // Compute PRIME zs, zr and testForm
            double zsPrime= segmentList.get(idSegment).getZsPrime(this,this.segmentList.get(idSegment) );
            this.segmentList.get(idSegment).zs  =zs;

            double zrPrime = segmentList.get(idSegment).getZrPrime(this, this.segmentList.get(idSegment));
            this.segmentList.get(idSegment).zr = zr;

            double testFormPrime = dp / (30 * (zsPrime + zrPrime));
            this.segmentList.get(idSegment).testFormPrime = testFormPrime;

            double gPathPrime;


            if (testForm <= 1) {
                gPathPrime = testForm * segmentList.get(idSegment).gPath + (1 - testForm) * gs;
            } else {
                gPathPrime = segmentList.get(idSegment).gPath;
            }
            this.segmentList.get(idSegment).gPathPrime = gPathPrime;

        }

    }

    private void computeAugmentedPoints() {

        for (int idPoint = 0; idPoint < pointList.size(); idPoint++) {


           // this. pointList.get(idPoint).altitude ;

        }

    }


    private double computeZs(SegmentPath segmentPath) {
        double zs = pointList.get(segmentPath.idPtStart).altitude + pointList.get(segmentPath.idPtStart).coordinate.z;
        return zs;
    }

    private double computeZr(SegmentPath segmentPath) {
        double zr = pointList.get(segmentPath.idPtFinal).altitude+ pointList.get(segmentPath.idPtFinal).coordinate.z;
        return zr;
    }

    private double computeZsPrime(SegmentPath segmentPath) {
        double alpha0 = 2 * Math.pow(10, -4);
        double deltazt = 6 * Math.pow(10, -3) * segmentPath.dp / (segmentPath.zs + segmentPath.zr);
        double deltazs = alpha0 * Math.pow((segmentPath.zs / (segmentPath.zs + segmentPath.zr)), 2) * (Math.pow(segmentPath.dp, 2) / 2);
        return segmentPath.zs + deltazs + deltazt;
    }

    private double computeZrPrime(SegmentPath segmentPath) {
        double alpha0 = 2 * Math.pow(10, -4);
        double deltazt = 6 * Math.pow(10, -3) * segmentPath.dp / (segmentPath.zs + segmentPath.zr);
        double deltazr = alpha0 * Math.pow((segmentPath.zr / (segmentPath.zs + segmentPath.zr)), 2) * (Math.pow(segmentPath.dp, 2) / 2);
        return segmentPath.zr + deltazr + deltazt;
    }


    private double getRayCurveLength(double d) {

        double gamma = Math.max(1000,8*d);
        return 2*gamma*Math.asin(d/(2*gamma));

    }






}
