package com.bayfi.entity;

import com.bayfi.enums.Oauth2ProviderType;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "authentication_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Oauth2AuthenticatonProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Oauth2ProviderType oauth2ProviderType; // GOOGLE, FACEBOOK, APPLE

    @Column(nullable = false, unique = true)
    private String providerId; // Unique ID from the provider

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}
