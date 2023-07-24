package com.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BiResponse implements Serializable {

    private static final long serialVersionUID = -7832711488085925503L;
    /**
     * 图表信息
     */
    private String genChart ;

    /**
     * 分析结果
     */
    private String genResult ;


    /**
     * 图表id
     */
    private Long chartId ;
}
