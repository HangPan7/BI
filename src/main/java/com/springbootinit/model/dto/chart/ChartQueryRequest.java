package com.springbootinit.model.dto.chart;

import com.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 分析目標
     */
    private String goal;

    /**
     * 圖表類型
     */
    private String chartType;

    /**
     * 图标名
     */
    private String chartName;


    /**
     * 用户 id
     */
    private Long userId;


    /**
     *
     */

    private static final long serialVersionUID = 1L;
}