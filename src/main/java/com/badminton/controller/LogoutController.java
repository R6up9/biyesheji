package com.badminton.controller;

import com.badminton.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT is stateless, just return success
        // Frontend will clear local storage
        return Result.success();
    }
}
