package com.common.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_role_dict")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
