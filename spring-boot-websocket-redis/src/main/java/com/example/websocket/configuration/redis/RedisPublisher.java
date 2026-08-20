package com.example.websocket.configuration.redis;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.example.websocket.configuration.websocket.session.dto.RelayMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic                  channelTopic;

    public void publish(RelayMessage relayMessage) {
        redisTemplate.convertAndSend(channelTopic.getTopic(), relayMessage);
    }
}
