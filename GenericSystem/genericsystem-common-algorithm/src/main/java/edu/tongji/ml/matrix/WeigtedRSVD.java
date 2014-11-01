/**
 * Tongji Edu.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package edu.tongji.ml.matrix;

import edu.tongji.data.SparseMatrix;
import edu.tongji.data.SparseVector;
import edu.tongji.util.LoggerUtil;

/**
 * 
 * @author Hanke Chen
 * @version $Id: WeigtedRSVD.java, v 0.1 2014-10-19 上午11:20:27 chench Exp $
 */
public class WeigtedRSVD extends MatrixFactorizationRecommender {

    /**  serial number*/
    private static final long serialVersionUID = -6860703746675880356L;

    /** the rating distribution w.r.t each user*/
    public float[][]          userWeights;

    /** the rating distribution w.r.t each item*/
    public float[][]          itemWeights;

    //===================================
    //      parameter
    //===================================
    private float             base1            = 1.55f;

    private float             base2            = 0.5f;

    /*========================================
     * Constructors
     *========================================*/
    /**
     * Construct a matrix-factorization-based model with the given data.
     * 
     * @param uc The number of users in the dataset.
     * @param ic The number of items in the dataset.
     * @param max The maximum rating value in the dataset.
     * @param min The minimum rating value in the dataset.
     * @param fc The number of features used for describing user and item profiles.
     * @param lr Learning rate for gradient-based or iterative optimization.
     * @param r Controlling factor for the degree of regularization. 
     * @param m Momentum used in gradient-based or iterative optimization.
     * @param iter The maximum number of iterations.
     * @param b1 The base of learning rate.
     * @param b2 The base of userWeights or itemWeights.
     */
    public WeigtedRSVD(int uc, int ic, double max, double min, int fc, double lr, double r,
                       double m, int iter, float b1, float b2) {
        super(uc, ic, max, min, fc, lr, r, m, iter);
        this.base1 = b1;
        this.base2 = b2;
    }

    /**
     * Construct a matrix-factorization-based model with the given data.
     * 
     * @param uc The number of users in the dataset.
     * @param ic The number of items in the dataset.
     * @param max The maximum rating value in the dataset.
     * @param min The minimum rating value in the dataset.
     * @param fc The number of features used for describing user and item profiles.
     * @param lr Learning rate for gradient-based or iterative optimization.
     * @param r Controlling factor for the degree of regularization. 
     * @param m Momentum used in gradient-based or iterative optimization.
     * @param iter The maximum number of iterations.
     */
    public WeigtedRSVD(int uc, int ic, double max, double min, int fc, double lr, double r,
                       double m, int iter) {
        super(uc, ic, max, min, fc, lr, r, m, iter);
    }

    /** 
     * @see edu.tongji.ml.matrix.MatrixFactorizationRecommender#buildModel(edu.tongji.data.SparseMatrix)
     */
    @Override
    public void buildModel(SparseMatrix rateMatrix) {
        super.buildModel(rateMatrix);

        //initialize weights
        getUserWeights(rateMatrix);
        getItemWeights(rateMatrix);

        // Gradient Descent:
        int round = 0;
        int rateCount = rateMatrix.itemCount();
        double prevErr = 99999;
        double currErr = 9999;

        while (Math.abs(prevErr - currErr) > 0.0001 && round < maxIter) {
            double sum = 0.0;
            for (int u = 0; u < userCount; u++) {
                SparseVector items = rateMatrix.getRowRef(u);
                int[] itemIndexList = items.indexList();

                if (itemIndexList != null) {
                    for (int i : itemIndexList) {
                        SparseVector Fu = userFeatures.getRowRef(u);
                        SparseVector Gi = itemFeatures.getColRef(i);

                        double AuiEst = Fu.innerProduct(Gi);
                        double AuiReal = rateMatrix.getValue(u, i);
                        double err = AuiReal - AuiEst;
                        sum += Math.abs(err);

                        int weightIndx = Double.valueOf(AuiReal / minValue - 1).intValue();
                        for (int s = 0; s < featureCount; s++) {
                            double Fus = userFeatures.getValue(u, s);
                            double Gis = itemFeatures.getValue(s, i);
                            userFeatures.setValue(u, s,
                                Fus
                                        + learningRate
                                        * (err * Gis * getWeight(u, i, weightIndx) - regularizer
                                                                                     * Fus));
                            itemFeatures.setValue(s, i,
                                Gis
                                        + learningRate
                                        * (err * Fus * getWeight(u, i, weightIndx) - regularizer
                                                                                     * Gis));
                        }
                    }
                }
            }

            prevErr = currErr;
            currErr = sum / rateCount;

            round++;

            // Show progress:
            LoggerUtil.info(logger, round + "\t" + currErr);
        }

    }

    public double getWeight(int u, int i, int weightIndx) {
        return (userWeights[u][weightIndx] * itemWeights[i][weightIndx] + base1);
    }

    public void getUserWeights(SparseMatrix rateMatrix) {
        int weightSize = Double.valueOf(maxValue / minValue).intValue();
        userWeights = new float[userCount][weightSize];
        for (int u = 0; u < userCount; u++) {
            SparseVector Fu = rateMatrix.getRowRef(u);
            int[] itemIndexList = Fu.indexList();

            if (itemIndexList == null) {
                for (int s = 0; s < weightSize; s++) {
                    userWeights[u][s] = base2 + 1.0f / weightSize;
                }
            } else {
                int total = weightSize;
                int[] rates = new int[weightSize];

                // count
                for (int i : itemIndexList) {
                    int indx = Double.valueOf(Fu.getValue(i) / minValue - 1).intValue();
                    rates[indx]++;
                    total++;
                }

                // compute weights
                for (int s = 0; s < weightSize; s++) {
                    userWeights[u][s] = base2 + (rates[s] + 1.0f) / total;
                }
            }
        }
    }

    public void getItemWeights(SparseMatrix rateMatrix) {
        int weightSize = Double.valueOf(maxValue / minValue).intValue();
        itemWeights = new float[itemCount][weightSize];
        for (int i = 0; i < itemCount; i++) {
            SparseVector Gi = rateMatrix.getColRef(i);
            int[] userIndexList = Gi.indexList();

            if (userIndexList == null) {
                for (int s = 0; s < weightSize; s++) {
                    itemWeights[i][s] = base2 + 1.0f / weightSize;
                }
            } else {
                int total = weightSize;
                int[] rates = new int[weightSize];

                // count
                for (int u : userIndexList) {
                    int indx = Double.valueOf(Gi.getValue(u) / minValue - 1).intValue();
                    rates[indx]++;
                    total++;
                }

                // compute weights
                for (int s = 0; s < weightSize; s++) {
                    itemWeights[i][s] = base2 + (rates[s] + 1.0f) / total;
                }
            }

        }
    }

}
