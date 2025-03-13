package com.tourney.user_service.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_role_dict")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
