package com.example.websocket.biz.controller;

import javax.sql.DataSource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class IndexController {

    DataSource dataSource;


    public IndexController(DataSource dataSource) {
        this.dataSource = dataSource;

        log.debug("dataSource {}", dataSource);
    }


    @RequestMapping(value = { "", "/", "/index" })
    public String index() {
        log.debug("index START");

        return "/index";
    }


    @RequestMapping(value = { "/user" })
    public String user() {
        log.debug("user START");

        return "/user";
    }


    @RequestMapping(value = { "/call" })
    public String call() {
        log.debug("call START");

        return "/call";
    }


    @RequestMapping(value = { "/app" })
    public String app() {
        log.debug("app START");

        return "/dual/app";
    }


    @RequestMapping(value = { "/tm" })
    public String tm() {
        log.debug("tm START");

        return "/dual/tm";
    }

    @RequestMapping(value = { "/app2" })
    public String app2() {
        log.debug("app2 START");

        return "/dual/app2";
    }


    @RequestMapping(value = { "/tm2" })
    public String tm2() {
        log.debug("tm2 START");

        return "/dual/tm2";
    }
}
