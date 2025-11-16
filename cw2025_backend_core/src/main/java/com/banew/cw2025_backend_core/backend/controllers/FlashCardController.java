package com.banew.cw2025_backend_core.backend.controllers;

import com.banew.cw2025_backend_common.dto.cards.FlashCardAnswer;
import com.banew.cw2025_backend_common.dto.cards.FlashCardBasicDto;
import com.banew.cw2025_backend_common.dto.cards.FlashCardDayStats;
import com.banew.cw2025_backend_core.backend.entities.UserProfile;
import com.banew.cw2025_backend_core.backend.services.interfaces.FlashCardService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@AllArgsConstructor
public class FlashCardController {

    private FlashCardService flashCardService;

    @GetMapping
    public List<FlashCardBasicDto> getCards(@AuthenticationPrincipal UserProfile currentUser) {
        return flashCardService.getCards(currentUser);
    }
    @PutMapping("/{flashCardId}/answer")
    public FlashCardBasicDto answer(@RequestBody FlashCardAnswer answer,
                                    @AuthenticationPrincipal UserProfile currentUser,
                                    @PathVariable Long flashCardId) {
        return flashCardService.answer(answer, currentUser, flashCardId);
    }
    @GetMapping("/stats")
    public FlashCardDayStats getDayStats(@AuthenticationPrincipal UserProfile currentUser) {
        return flashCardService.getDayStats(currentUser);
    }
}
