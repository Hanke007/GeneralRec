/**
 * Tongji Edu.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package edu.tongji.engine.smartgrid;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.util.StopWatch;
import edu.tongji.orm.SmartGridDataSource;
import edu.tongji.util.DateUtil;
import edu.tongji.util.LoggerUtil;
import edu.tongji.util.RandomUtil;
import edu.tongji.vo.MeterReadingVO;

/**
 * 
 * @author chench
 * @version $Id: AnalysisEngine.java, v 0.1 2014-2-14 下午12:42:00 chench Exp $
 */
public class AnalysisEngine extends SmartGridEngine {
    /** 高斯噪声产生范围*/
    private double[] gauseDomain;

    /** 主部和高斯噪声对应的比重系数*/
    private double[] weightDomain;

    /** 
     * @see edu.tongji.engine.smartgrid.SmartGridEngine#assembleDataSet()
     */
    @Override
    protected void assembleDataSet() {
        //物理数据集为老数据集，
        //逻辑数据集保持稳健
        if (!dataSource.isFresh() && keepSteady) {
            return;
        }

        //交替上下文
        List<MeterReadingVO> context = new ArrayList<MeterReadingVO>();
        context.addAll(SmartGridDataSource.meterContexts);
        SmartGridDataSource.meterContexts.clear();

        //对上下文进行排序
        Collections.sort(context, new Comparator<MeterReadingVO>() {
            @Override
            public int compare(MeterReadingVO o1, MeterReadingVO o2) {
                return ((Long) (o1.getTimeVal() - o2.getTimeVal())).intValue();
            }
        });

        //处理数据集
        MeterReadingVO meterReading = null;
        for (MeterReadingVO meter : context) {

            if (meterReading == null) {
                //初始化
                meterReading = meter;
                continue;
            } else if (!DateUtil.sameDayAndHour(meterReading.getTimeVal(), meter.getTimeVal())) {
                //新的电表计时周期
                SmartGridDataSource.meterContexts.add(meterReading);
                meterReading = meter;
                LoggerUtil.debug(logger, "O：" + meterReading.getReading() + "   Date："
                                         + new Timestamp(meterReading.getTimeVal()));
                continue;
            }
            //在同一计时周期，累计读数
            meterReading.setReading(meterReading.getReading() + meter.getReading());

        }
    }

    /** 
     * @see edu.tongji.engine.smartgrid.SmartGridEngine#emulate()
     */
    @Override
    protected void emulate() {
        //1.模拟记录读数
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (MeterReadingVO reading : SmartGridDataSource.meterContexts) {
            double reads = reading.getReading();
            for (int i = 0; i < gauseDomain.length; i++) {
                reads += weightDomain[i] * RandomUtil.nextGaussian(gauseDomain[i]);
            }
            LoggerUtil.debug(logger, "O：" + reading.getReading() + " R：" + reads);
        }
    }

    /**
     * Getter method for property <tt>gauseDomain</tt>.
     * 
     * @return property value of gauseDomain
     */
    public double[] getGauseDomain() {
        return gauseDomain;
    }

    /**
     * Setter method for property <tt>gauseDomain</tt>.
     * 
     * @param gauseDomain value to be assigned to property gauseDomain
     */
    public void setGauseDomain(double[] gauseDomain) {
        this.gauseDomain = gauseDomain;
    }

    /**
     * Getter method for property <tt>weightDomain</tt>.
     * 
     * @return property value of weightDomain
     */
    public double[] getWeightDomain() {
        return weightDomain;
    }

    /**
     * Setter method for property <tt>weightDomain</tt>.
     * 
     * @param weightDomain value to be assigned to property weightDomain
     */
    public void setWeightDomain(double[] weightDomain) {
        this.weightDomain = weightDomain;
    }

}