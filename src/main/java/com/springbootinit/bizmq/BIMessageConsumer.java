package com.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.springbootinit.common.ErrorCode;
import com.springbootinit.constant.BIMqConstant;
import com.springbootinit.exception.BusinessException;
import com.springbootinit.manager.AiManager;
import com.springbootinit.model.entity.Chart;
import com.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.IOException;


@Slf4j
@Component
public class BIMessageConsumer {
    @Resource
    private ChartService chartService ;

    @Resource
    private AiManager aiManager ;

    /**
     * 处理AI任务的队列
     * message 是监听到的消息
     * channel 可以用来确认消息和设置一次处理的消息数
     * deliveryTag 参数在手动消息确认模式下用于标识和确认消息的处理状态。
     */
    @RabbitListener(queues = "bi_queue_name" , ackMode = "MANUAL")
    public void receiveMessage(String message , Channel channel , @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        try {
            if(StringUtils.isBlank(message)){
                channel.basicNack(deliveryTag , false , false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR , "请求参数为空") ;
            }
            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            if(chart == null){
                handleChartUpdateError(chartId , "更新图表的失败状态失败");
                channel.basicNack(deliveryTag , false , false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR , "请求参数为空") ;
            }
            //将任务更改为执行中
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            //如果提交失败，代表数据库出错
            if (!b) {
                handleChartUpdateError(chartId , "更新图表的失败状态失败");
                channel.basicNack(deliveryTag , false , false);
                return;
                }
            //调用ai
            String result = null;
            try {
                result = aiManager.doChat(buildUserInput(chart));
            } catch (Exception e) {
                channel.basicNack(deliveryTag , false , false);
                handleChartUpdateError(chartId, "更新图表的成功状态失败");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR ,"AI生成错误") ;
            }

            //对返回结果做拆分，按照5个中括号进行拆分
            String[] splits = result.split("【【【【【");
            //拆分之后还要进行校验
            if (splits.length != 3) {
                handleChartUpdateError(chartId , "更新图表的失败状态失败");
                channel.basicNack(deliveryTag , false , false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            //得到ai结果后也要更新数据库
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chartId);
            updateChartResult.setGenChart(genChart);
            updateChartResult.setStatus("succeed");
            updateChartResult.setGenResult(genResult);
            boolean b1 = chartService.updateById(updateChartResult);
            if (!b1) {
                channel.basicNack(deliveryTag , false , false);
                handleChartUpdateError(chartId, "更新图表的成功状态失败");
            }
            channel.basicAck(deliveryTag , false);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR , "系统内部异常") ;
        }
    }

    /**
     * 死信队列
     * message 是监听到的消息
     * channel 可以用来确认消息和设置一次处理的消息数
     * deliveryTag 参数在手动消息确认模式下用于标识和确认消息的处理状态。
     *
     */
    @RabbitListener(queues = {BIMqConstant.DLX_QUEUE_NAME} , ackMode = "MANUAL")
    public void delReceiveMessage(String message , Channel channel , @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        try {
            if(StringUtils.isBlank(message)){
                channel.basicNack(deliveryTag , false , false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR , "请求参数为空") ;
            }
            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            if(ObjectUtils.isNotEmpty(chart)){
                channel.basicNack(deliveryTag , false , false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR , "请求参数为空") ;
            }
            //将任务更改为执行中
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            //如果提交失败，代表数据库出错
            if (!b) {
                handleChartUpdateError(chartId , "更新图表的失败状态失败");
                channel.basicNack(deliveryTag , false , false);
                return;
            }
            //调用ai
            String result = null;
            try {
                result = aiManager.doChat(buildUserInput(chart));
            } catch (Exception e) {
                channel.basicNack(deliveryTag , false , false);
                handleChartUpdateError(chartId, "更新图表的成功状态失败");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR ,"AI生成错误") ;
            }

            //对返回结果做拆分，按照5个中括号进行拆分
            String[] splits = result.split("【【【【【");
            //拆分之后还要进行校验
            if (splits.length != 3) {
                channel.basicNack(deliveryTag , false , false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI生成错误");
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            //得到ai结果后也要更新数据库
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chartId);
            updateChartResult.setGenChart(genChart);
            updateChartResult.setStatus("succeed");
            updateChartResult.setGenResult(genResult);
            boolean b1 = chartService.updateById(updateChartResult);
            if (!b1) {
                channel.basicNack(deliveryTag , false , false);
                handleChartUpdateError(chartId, "更新图表的成功状态失败");
            }
            channel.basicAck(deliveryTag , false);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR , "系统内部异常") ;
        }
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

    private String buildUserInput(Chart chart){
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String chartData = chart.getChartData();
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
        userInput.append(chartData).append("\n");
        return userInput.toString() ;
    }
}
