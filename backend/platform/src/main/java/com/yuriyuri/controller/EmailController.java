package com.yuriyuri.controller;

import com.yuriyuri.common.Result;
import com.yuriyuri.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@Tag(name = "邮件相关")
public class EmailController {

    @Autowired
    private UserService userService;

    @Operation(summary = "发送验证码")
    @PostMapping("/send")
    public Result<Void> sendEmail(@RequestParam @Email String email){
        userService.sendEmail(email);
        return Result.success();
    }
}
