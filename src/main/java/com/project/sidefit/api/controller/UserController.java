package com.project.sidefit.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    /*
     *//**
     * 전체 회원 목록 조회
     *//*
    @GetMapping("/users")
    public Response findAll() {


    }

    *//**
     * 회원 상세 조회
     *//*
    @GetMapping("/user/{id}")
    public Response findDetail(@PathVariable Long id) {

    }

    *//**
     * 비밀번호 변경
     *//*
    @PatchMapping("/user/password")
    public Response updatePassword() {

    }*/
}