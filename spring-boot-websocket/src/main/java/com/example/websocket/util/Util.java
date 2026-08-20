package com.example.websocket.util;


import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public final class Util {

    public static String toString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(object);

            return jsonString;
        } catch (Exception e) {
            log.error("Parsing Error", e);

            return null;
        }
    }
}
