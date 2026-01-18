package com.tourney.controller.user;

import com.common.security.UserPrincipal;
import com.tourney.dto.user.UserLookupDTO;
import com.tourney.service.user.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserLookupController {

    private final UserLookupService userLookupService;

    /**
     * Przykład:
     * GET /api/users/search?q=Mich&limit=10
     *
     * Zwraca [] jeśli q ma mniej niż 4 znaki.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserLookupDTO>> searchUsers(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "limit", required = false) Integer limit,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(userLookupService.searchByName(q, currentUser.getId(), limit));
    }
}