package com.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class ChartAddRequest implements Serializable {
    /**
     * 图标名
     */
    private String chartName;

    /**
     * 分析目標
     */
    private String goal;

    /**
     * 内容
     */
    private String chartData;

    /**
     * 标签列表
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}