package com.springbootinit.manager;

import com.springbootinit.common.ErrorCode;
import com.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.YuCongMingClientConfig;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient ;

    public String doChat(String message){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setMessage(message);
        devChatRequest.setModelId(1676853055661879298L);
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if(response == null || response.getData()==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR ,"AI 响应错误");
        }
        return response.getData().getContent() ;
    }
}

