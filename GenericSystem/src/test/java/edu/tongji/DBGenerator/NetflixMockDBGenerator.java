/**
 * Tongji Edu.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package edu.tongji.DBGenerator;

import java.util.Map;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.tongji.dao.RatingDAO;
import edu.tongji.engine.DefaultRecommendationEngine;
import edu.tongji.model.Rating;
import edu.tongji.orm.NetflixDataSource;
import edu.tongji.util.ExceptionUtil;

/**
 * 
 * @author chenkh
 * @version $Id: NetflixMockDBGenerator.java, v 0.1 2013-9-16 下午5:16:19 chenkh Exp $
 */
public class NetflixMockDBGenerator {

    @Test
    public void test() {
        ClassPathXmlApplicationContext ctx = null;
        try {
            ctx = new ClassPathXmlApplicationContext(
                "META-INF/spring/application-context-netflix.xml");

            DefaultRecommendationEngine engine = (DefaultRecommendationEngine) ctx
                .getBean("RecommendationEngine");
            RatingDAO ratingDao = (RatingDAO) ctx.getBean("ratingDAO");
            NetflixDataSource dataSource = (NetflixDataSource) engine.getDataSource();

            int start = 1;
            int step = 100;
            int end = 100;
            int countOfMovieFiles = dataSource.getCountOfMovieFiles();

            for (; start <= end;) {
                dataSource.setIndexOfMoviesFront(start);
                dataSource.setCountOfMovieFiles(end);
                dataSource.setLazy(true);
                dataSource.reload();
                Map<String, Rating> ratingsContext = dataSource.getRatingContexts();
                for (Rating rating : ratingsContext.values()) {
                    ratingDao.insert(rating);
                }

                start = end +1;
                end = (start + step) <= countOfMovieFiles ? (start + step) : countOfMovieFiles;
            }
        } catch (Exception e) {
            ExceptionUtil.caught(e, "edu.tongji.engine.TestEngine 测试用例发生错误");
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

}