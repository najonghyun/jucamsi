package com.inet.juchamsi.domain.chat.api;

import com.inet.juchamsi.domain.chat.application.ChatService;
import com.inet.juchamsi.domain.chat.dto.request.ChatMessageRequest;
import com.inet.juchamsi.domain.chat.dto.request.SystemMessageRequest;
import com.inet.juchamsi.global.api.ApiResult;
import com.inet.juchamsi.global.error.NotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

import static com.inet.juchamsi.global.api.ApiResult.ERROR;
import static com.inet.juchamsi.global.api.ApiResult.OK;

@RestController
@RequiredArgsConstructor
@Api(tags = "메세지")
public class MessageApiController {
    
    private final SimpMessageSendingOperations sendingOperations;
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public ApiResult<Void> enter(ChatMessageRequest request) {
        String roomId = request.getRoomId();
        if (ChatMessageRequest.MessageType.ENTER.equals(request.getType())) {
            request.setMessage(request.getSender() + "님이 입장하셨습니다.");
        }
        // 메세지 내용 저장
        try {
            chatService.createChat(request);
        } catch (NotFoundException e) {
            ERROR("사용자를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        
        // topic-1대다, queue-1대1
        sendingOperations.convertAndSend("/topic/chat/room/"+ roomId,request);
        
        
        return OK(null);
    }

    @MessageMapping("/system/message/{roomId}")
    public ApiResult<Void> system(@DestinationVariable String roomId, SystemMessageRequest request) {
        // 시스템에서 발생한 메시지를 클라이언트로 전송합니다.
        sendingOperations.convertAndSend("/topic/system/room/" + roomId, request);
        
        // 메세지 내용 저장
        chatService.createSystemChat(roomId, request);

        return OK(null);
    }
}
