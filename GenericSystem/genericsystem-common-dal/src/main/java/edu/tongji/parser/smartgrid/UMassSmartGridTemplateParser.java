/**
 * Tongji Edu.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package edu.tongji.parser.smartgrid;

import edu.tongji.parser.Parser;
import edu.tongji.parser.ParserTemplate;
import edu.tongji.util.ExceptionUtil;
import edu.tongji.util.StringUtil;
import edu.tongji.vo.MeterReadingVO;

/**
 * 
 * @author Hanke Chen
 * @version $Id: UMassSmartGridTemplateParser.java, v 0.1 2013-12-17 下午3:49:52 chench Exp $
 */
public class UMassSmartGridTemplateParser implements Parser {

    /** 分隔符正则表达式 */
    private final static String SAPERATOR_EXPRESSION = "\\,";

    /** 
     * @see edu.tongji.parser.Parser#parser(edu.tongji.parser.ParserTemplate)
     */
    @Override
    public Object parser(ParserTemplate template) {
        //获取模板内容
        String context = template.getAsString();
        if (StringUtil.isEmpty(context)) {
            return null;
        }

        try {
            //解析模板
            String[] elements = context.split(SAPERATOR_EXPRESSION);
            long timeVal = Long.valueOf(elements[1]) * 1000;
            int reading = Double.valueOf(elements[2]).intValue();

            MeterReadingVO meter = new MeterReadingVO(reading, null, timeVal);
            return meter;
        } catch (Exception e) {
            ExceptionUtil.caught(e, "解析ParserTemplate错误，内容: " + template.getAsString());
        }
        return null;
    }

    /** 
     * @see edu.tongji.parser.Parser#parse(java.lang.String)
     */
    @Override
    public Object parse(String template) {
        return null;
    }

}
