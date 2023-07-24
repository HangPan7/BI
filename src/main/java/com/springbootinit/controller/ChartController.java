package com.springbootinit.controller;
import java.util.Date;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.springbootinit.bizmq.BIMessageProducer;
import com.springbootinit.common.BaseResponse;
import com.springbootinit.common.DeleteRequest;
import com.springbootinit.common.ErrorCode;
import com.springbootinit.common.ResultUtils;
import com.springbootinit.constant.CommonConstant;
import com.springbootinit.manager.AiManager;
import com.springbootinit.manager.RedisLimiterManager;
import com.springbootinit.model.dto.chart.*;
import com.springbootinit.model.entity.Chart;
import com.springbootinit.model.entity.User;
import com.springbootinit.annotation.AuthCheck;
import com.springbootinit.constant.UserConstant;
import com.springbootinit.exception.BusinessException;
import com.springbootinit.exception.ThrowUtils;
import com.springbootinit.model.vo.BiResponse;
import com.springbootinit.service.ChartService;
import com.springbootinit.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import com.springbootinit.utils.ExcelUtils;
import com.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static com.springbootinit.constant.CommonConstant.ONE_MB;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager ;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor ;

    @Resource
    private BIMessageProducer biMessageProducer ;

    private final static Gson GSON = new Gson();


    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }


    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAI(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        /**
         * 1.校验文件大小
         * 2.校验文件后缀名
         */
        long size = multipartFile.getSize() ;
        ThrowUtils.throwIf(size>ONE_MB ,ErrorCode.PARAMS_ERROR,"文件超过1M");
        //获取文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //获取文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        //定义文件的合法后缀
        List<String> strings = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!strings.contains(suffix) , ErrorCode.PARAMS_ERROR ,"非法文件");
        String chartName = genChartByAIRequest.getChartName();
        String chartType = genChartByAIRequest.getChartType();
        String goal = genChartByAIRequest.getGoal();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        //判断用户是否登录，登录后才能访问
        User loginUser = userService.getLoginUser(request);
        //限流 防止用户非法调用
        redisLimiterManager.doRateLimit("genChartByAI_" + loginUser.getId());
        //excel表格数据变成cvs数据
        String s = ExcelUtils.excelToCsv(multipartFile);
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //拼接分析目标
        String userGoal = goal;
        //如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
        //就将分析目标拼接上“请使用”+图表类型
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //传入压缩后的数据
        userInput.append(s).append("\n");
        //拿到返回结果
        String result = aiManager.doChat(userInput.toString());
        //对返回结果做拆分，按照5个中括号进行拆分
        String[] splits = result.split("【【【【【");
        //拆分之后还要进行校验
        if (splits.length != 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        //插入到数据库
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(s);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus("succeed");
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse) ;
    }


    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAIAsync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        /**
         * 1.校验文件大小
         * 2.校验文件后缀名
         */
        long size = multipartFile.getSize() ;
        ThrowUtils.throwIf(size>ONE_MB ,ErrorCode.PARAMS_ERROR,"文件超过1M");
        //获取文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //获取文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        //定义文件的合法后缀
        List<String> strings = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!strings.contains(suffix) , ErrorCode.PARAMS_ERROR ,"非法文件");
        String chartName = genChartByAIRequest.getChartName();
        String chartType = genChartByAIRequest.getChartType();
        String goal = genChartByAIRequest.getGoal();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        //判断用户是否登录，登录后才能访问
        User loginUser = userService.getLoginUser(request);
        //限流 防止用户非法调用
        redisLimiterManager.doRateLimit("genChartByAI_" + loginUser.getId());
        //excel表格数据变成cvs数据
        String s = ExcelUtils.excelToCsv(multipartFile);
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //拼接分析目标
        String userGoal = goal;
        //如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            //就将分析目标拼接上“请使用”+图表类型
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //传入压缩后的数据
        userInput.append(s).append("\n");
        //先把图表存入数据库
        Chart chart1 = new Chart();
        chart1.setChartName(chartName);
        chart1.setGoal(goal);
        chart1.setChartData(s);
        chart1.setChartType(chartType);
        //这两个等生成图表后再传入
/*        chart1.setGenResult();
        chart1.setGenChart();*/
        chart1.setUserId(loginUser.getId());
        chart1.setStatus("wait");
        boolean save = chartService.save(chart1);
        ThrowUtils.throwIf(!save ,ErrorCode.PARAMS_ERROR ,"图表保存失败");

        //将ai的任务塞入线程池的任务队列，等待完成后返回结果
        //todo 可以处理任务队列满后处理异常的情况，也可以设计超时重传

// 创建一个 ScheduledExecutorService 用于超时检测
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

// 创建一个 CompletableFuture
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // 将任务更改为执行中
            Chart updateChart = new Chart();
            updateChart.setId(chart1.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            // 如果提交失败，代表数据库出错
            if (!b) {
                handleChartUpdateError(chart1.getId(), "更新图表的失败状态失败");
                return;
            }

            // 调用 AI
            String result = aiManager.doChat(userInput.toString());
            // 对返回结果进行拆分，按照5个中括号进行拆分
            String[] splits = result.split("【【【【【");
            // 拆分之后还要进行校验
            if (splits.length != 3) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();

            // 更新数据库中的结果
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart1.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setStatus("succeed");
            updateChartResult.setGenResult(genResult);
            boolean b1 = chartService.updateById(updateChartResult);
            if (!b1) {
                handleChartUpdateError(chart1.getId(), "更新图表的成功状态失败");
            }
        } , threadPoolExecutor);

        // 超时时间设置为 1 分钟
        long timeout = 1;
        TimeUnit timeUnit = TimeUnit.MINUTES;
        // 使用 ScheduledExecutorService.schedule 方法进行超时检测
        scheduler.schedule(() -> {
            try {
                future.get(1 ,TimeUnit.MINUTES) ;
            } catch (Exception e) {
                handleChartUpdateError(chart1.getId(), "AI响应超时");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
            }
        }, timeout, timeUnit);


 /*       CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            //将任务更改为执行中
            Chart updateChart = new Chart();
            updateChart.setId(chart1.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            //如果提交失败，代表数据库出错
            if (!b) {
                handleChartUpdateError(chart1.getId(), "更新图表的失败状态失败");
                return;
            }
            //调用ai
            String result = aiManager.doChat(userInput.toString());

            //对返回结果做拆分，按照5个中括号进行拆分
            String[] splits = result.split("【【【【【");
            //拆分之后还要进行校验
            if (splits.length != 3) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            //得到ai结果后也要更新数据库
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart1.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setStatus("succeed");
            updateChartResult.setGenResult(genResult);
            boolean b1 = chartService.updateById(updateChartResult);
            if (!b1) {
                handleChartUpdateError(chart1.getId(), "更新图表的成功状态失败");
            }
        }, threadPoolExecutor);*/

        //插入到数据库

        BiResponse biResponse = new BiResponse();

        biResponse.setChartId(chart1.getId());
        return ResultUtils.success(biResponse) ;
    }

    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAIAsyncMQ(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        /**
         * 1.校验文件大小
         * 2.校验文件后缀名
         */
        long size = multipartFile.getSize() ;
        ThrowUtils.throwIf(size>ONE_MB ,ErrorCode.PARAMS_ERROR,"文件超过1M");
        //获取文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //获取文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        //定义文件的合法后缀
        List<String> strings = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!strings.contains(suffix) , ErrorCode.PARAMS_ERROR ,"非法文件");
        String chartName = genChartByAIRequest.getChartName();
        String chartType = genChartByAIRequest.getChartType();
        String goal = genChartByAIRequest.getGoal();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        //判断用户是否登录，登录后才能访问
        User loginUser = userService.getLoginUser(request);
        //限流 防止用户非法调用
        redisLimiterManager.doRateLimit("genChartByAI_" + loginUser.getId());
        //excel表格数据变成cvs数据
        String s = ExcelUtils.excelToCsv(multipartFile);
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //拼接分析目标
        String userGoal = goal;
        //如果图表类型不为空
        if (StringUtils.isNotBlank(chartType)) {
            //就将分析目标拼接上“请使用”+图表类型
            userGoal += ",请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        //传入压缩后的数据
        userInput.append(s).append("\n");
        //先把图表存入数据库
        Chart chart1 = new Chart();
        chart1.setChartName(chartName);
        chart1.setGoal(goal);
        chart1.setChartData(s);
        chart1.setChartType(chartType);
        //这两个等生成图表后再传入
/*        chart1.setGenResult();
        chart1.setGenChart();*/
        chart1.setUserId(loginUser.getId());
        chart1.setStatus("wait");
        boolean save = chartService.save(chart1);
        ThrowUtils.throwIf(!save ,ErrorCode.PARAMS_ERROR ,"图表保存失败");

        //将ai的任务塞入线程池的任务队列，等待完成后返回结果
      biMessageProducer.sedMessage(String.valueOf(chart1.getId()));
        //插入到数据库

        BiResponse biResponse = new BiResponse();

        biResponse.setChartId(chart1.getId());
        return ResultUtils.success(biResponse) ;
    }

    /**
     * 包裝類搜索
     *
     * @param chartQueryRequest
     * @return
     */
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String chartType = chartQueryRequest.getChartType();
        String chartName = chartQueryRequest.getChartName();
        String goal = chartQueryRequest.getGoal();
        Long id = chartQueryRequest.getId();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();


        queryWrapper.eq(id!=null, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(chartName), "charName", chartName);
        queryWrapper.ne(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    //定义的工具异常类
    private void handleChartUpdateError(long chartId ,String execMessage) {
        Chart upChartResult = new Chart();
        upChartResult.setId(chartId);
        upChartResult.setStatus("failed");
        upChartResult.setExecMessage(execMessage);
        boolean b = chartService.updateById(upChartResult);
        if(!b){
            log.error("图表更新失败" + chartId+","+ execMessage);
        }
    }
}
