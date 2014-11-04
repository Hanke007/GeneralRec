/**
 * Tongji Edu.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package prea.util;

import edu.tongji.data.BlockMatrix;
import edu.tongji.data.SparseMatrix;
import edu.tongji.data.SparseVector;

/**
 * Matrix information utility
 * 
 * @author Hanke Chen
 * @version $Id: MatrixInformationUtil.java, v 0.1 2014-10-28 下午2:55:50 chench Exp $
 */
public final class MatrixInformationUtil {

    /**
     * forbid construction
     */
    private MatrixInformationUtil() {

    }

    /**
     * compute the sparsity message about the integrated matrix
     * 
     * @param rateMatrices
     * @return the sparsity message about the integrated matrix
     */
    public static String sparsity(BlockMatrix rateMatrices) {
        StringBuilder msg = new StringBuilder("\n==========================================");
        int itemTotalCount = rateMatrices.itemCount();
        int[][] structure = rateMatrices.structure();
        for (int i = 0; i < structure.length; i++) {
            for (int j = 0; j < structure[i].length; j++) {
                msg.append("\n******************************************\n")
                    .append(
                        "Matrix[" + i + ", " + j + "]   S: "
                                + String.format("%.5f", rateMatrices.getSparsity(i, j)))
                    .append(" R: ")
                    .append(
                        String.format("%.5f", rateMatrices.getBlock(i, j).itemCount() * 1.0
                                              / itemTotalCount))
                    .append(hierarchy(rateMatrices.getBlock(i, j)));
            }
        }
        msg.append('\n').append("Block Matrix : " + rateMatrices.getSparsity());
        return msg.toString();
    }

    /**
     * compute the rating distribution w.r.t the given matrix
     * 
     * @param rateMatrix    the matrix to compute
     * @return the rating distribution
     */
    public static String hierarchy(SparseMatrix rateMatrix) {
        int[] countTable = new int[10];
        int M = rateMatrix.length()[0];
        for (int u = 0; u < M; u++) {
            SparseVector Ru = rateMatrix.getRowRef(u);
            int[] indexList = Ru.indexList();
            if (indexList == null) {
                continue;
            }

            for (int v : indexList) {
                double val = Ru.getValue(v);
                int pivot = Double.valueOf(val / 0.5 - 1).intValue();
                countTable[pivot]++;
            }
        }

        StringBuilder msg = new StringBuilder();
        int itemCount = rateMatrix.itemCount();
        for (int i = 0; i < 10; i++) {
            msg.append("\n\t").append(String.format("%.1f", (i + 1) * 0.5)).append('\t')
                .append(countTable[i] * 1.0 / itemCount);
        }

        return msg.toString();
    }

    /**
     * analyze the error distribution
     * 
     * @param testMatrix        
     * @param predictedMatrix
     * @return
     */
    public static String RMSEAnalysis(SparseMatrix testMatrix, SparseMatrix predictedMatrix) {
        int[] countTable = new int[10];
        double[] rmseTable = new double[10];

        int rowCount = testMatrix.length()[0];
        for (int u = 0; u < rowCount; u++) {
            SparseVector Ru = testMatrix.getRowRef(u);
            int[] indexList = Ru.indexList();
            if (indexList == null) {
                continue;
            }

            for (int v : indexList) {
                double RuvReal = Ru.getValue(v);
                double RuvEsitm = predictedMatrix.getValue(u, v);

                int pivot = Double.valueOf(RuvReal / 0.5 - 1).intValue();
                countTable[pivot]++;
                rmseTable[pivot] += Math.pow(RuvReal - RuvEsitm, 2.0d);
            }
        }

        double rmseTotal = 0.0d;
        double countTotal = 0.0d;
        for (int i = 0; i < 10; i++) {
            rmseTotal += rmseTable[i];
            countTotal += countTable[i];
        }

        // message
        StringBuilder msg = new StringBuilder("\n");
        for (int i = 0; i < 10; i++) {
            if (countTable[i] == 0) {
                continue;
            }
            double RMSE = Math.sqrt(rmseTable[i] / countTable[i]);
            msg.append("\t").append((i + 1) * 0.5).append('\t')
                .append(String.format("%.5f", countTable[i] / countTotal)).append("\t\t")
                .append(String.format("%.6f", RMSE)).append(" [")
                .append(String.format("%.5f", rmseTable[i] / rmseTotal)).append("]\n");
        }
        return msg.toString();
    }
}