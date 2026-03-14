package com.tourney.domain.notification;

public enum NotificationType {
    // Notifications for organizer
    PARTICIPANT_REGISTERED,
    ARMY_LIST_SUBMITTED,
    
    // Notifications for participant
    PARTICIPATION_CONFIRMED,
    PAYMENT_CONFIRMED,
    ARMY_LIST_APPROVED,
    ARMY_LIST_REJECTED,
    TOURNAMENT_STARTED,
    
    // Challenges (Tournament)
    CHALLENGE_RECEIVED,
    CHALLENGE_ACCEPTED,
    CHALLENGE_REJECTED,
    
    // League Challenges
    LEAGUE_CHALLENGE_RECEIVED,
    LEAGUE_CHALLENGE_ACCEPTED,
    LEAGUE_CHALLENGE_REJECTED,
    
    // Tournament results
    TOURNAMENT_RESULT
}
