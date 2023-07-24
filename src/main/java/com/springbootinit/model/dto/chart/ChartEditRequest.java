package com.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class ChartEditRequest implements Serializable {
    /**
     * 图标名
     */
    private String chartName;

    /**
     * id
     */
    private Long id;

    /**
     * 分析目標
     */
    private String goal;

    /**
     * 内容
     */
    private String charData;

    /**
     * 圖表類型
     */
    private List<String> chartType;

    private static final long serialVersionUID = 1L;
}