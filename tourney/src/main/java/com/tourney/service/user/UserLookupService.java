package com.tourney.service.user;

import com.common.security.UserPrincipal;
import com.tourney.dto.user.UserLookupDTO;
import com.tourney.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLookupService {

    private static final int MIN_QUERY_LEN = 4;
    private static final int DEFAULT_LIMIT = 10;

    private final UserRepository userRepository;

    public List<UserLookupDTO> searchByName(String query, Long currentUserId, Integer limit) {
        String q = query == null ? "" : query.trim();

        if (q.length() < MIN_QUERY_LEN) {
            return List.of();
        }

        int size = (limit == null || limit <= 0 || limit > 20) ? DEFAULT_LIMIT : limit;

        return userRepository
                .findByNameContainingIgnoreCaseOrderByNameAsc(q, PageRequest.of(0, size))
                .stream()
                // żeby nie podpowiadać samego siebie przy wyborze przeciwnika:
                .filter(u -> currentUserId == null || !u.getId().equals(currentUserId))
                .map(u -> UserLookupDTO.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .build())
                .toList();
    }
}