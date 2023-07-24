package com.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springbootinit.constant.CommonConstant;
import com.springbootinit.mapper.ChartMapper;
import com.springbootinit.model.dto.chart.ChartQueryRequest;
import com.springbootinit.model.dto.post.PostQueryRequest;
import com.springbootinit.model.entity.Chart;
import com.springbootinit.model.entity.Post;
import com.springbootinit.service.ChartService;
import com.springbootinit.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author stupidBoy
* @description 针对表【chart(帖子)】的数据库操作Service实现
* @createDate 2023-06-30 16:33:59
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}




