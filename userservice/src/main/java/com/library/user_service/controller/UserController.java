package com.library.user_service.controller;

import com.library.user_service.dto.UpdateProfileDto;
import com.library.user_service.dto.UserDto;
import com.library.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(Authentication authentication,
                                                 @RequestBody UpdateProfileDto updateProfileDto) {
        return ResponseEntity.ok(userService.updateUserProfile(authentication.getName(), updateProfileDto));
    }

    // ============================================================
    // חדש - endpoints פנימיים בלבד (server-to-server, בלי JWT).
    // נועדו לשימוש רק ע"י שירותים אחרים בתוך רשת ה-Docker הפנימית
    // (smart-library-ai, bookandloaningservice) - לא ל-frontend!
    // בכוונה תחת /api/users/internal/** ולא תחת /api/users/{id} כדי
    // שלא יתנגש/יחשוף בטעות את /api/users/profile.
    // ============================================================

    @GetMapping("/internal/{id}")
    public ResponseEntity<UserDto> getUserByIdInternal(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/internal")
    public ResponseEntity<List<UserDto>> getAllUsersInternal() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
