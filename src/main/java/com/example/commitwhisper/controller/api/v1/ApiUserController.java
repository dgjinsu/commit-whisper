package com.example.commitwhisper.controller.api.v1;

import com.example.commitwhisper.dto.user.CreateUserReq;
import com.example.commitwhisper.dto.user.GetUserSettingsRes;
import com.example.commitwhisper.dto.user.LoginUserRes;
import com.example.commitwhisper.dto.user.UpdateSlackWebhookReq;
import com.example.commitwhisper.security.UserPrincipal;
import com.example.commitwhisper.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ApiUserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody CreateUserReq signupReq) {
        userService.signup(signupReq);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoginUserRes.UserInfo> getCurrentUser(
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        LoginUserRes.UserInfo userInfo = new LoginUserRes.UserInfo(
            userPrincipal.getId(),
            userPrincipal.getLoginId(),
            userPrincipal.getName()
        );

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GetUserSettingsRes> getSettings(
        @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        GetUserSettingsRes settings = userService.getSettings(userPrincipal.getId());
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings/slack-webhook")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateSlackWebhook(
        @AuthenticationPrincipal UserPrincipal userPrincipal,
        @RequestBody UpdateSlackWebhookReq req
    ) {
        userService.updateSlackWebhookUrl(userPrincipal.getId(), req);
        return ResponseEntity.ok().build();
    }
}