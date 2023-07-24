package com.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenChartByAIRequest implements Serializable {

    /**
     * 图标名
     */
    private String chartName;

    /**
     * 分析目標
     */
    private String goal;

    /**
     * 标签列表
     */
    private String chartType;

    private static final long serialVersionUID = 4602285575457143935L;
}
