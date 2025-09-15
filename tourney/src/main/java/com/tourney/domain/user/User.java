package com.tourney.domain.user;

import com.tourney.domain.tournament.Tournament;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
    private Set<Tournament> organizedTournaments = new HashSet<>();

    @ManyToMany(mappedBy = "participants")
    private Set<Tournament> joinedTournaments = new HashSet<>();
}