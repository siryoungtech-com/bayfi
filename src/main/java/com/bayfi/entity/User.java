    package com.bayfi.entity;

    import jakarta.persistence.*;
    import lombok.*;
    import org.hibernate.annotations.CreationTimestamp;
    import org.hibernate.annotations.UpdateTimestamp;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;

    import java.time.LocalDateTime;
    import java.util.*;

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "users")
    @Entity
    public class User  implements UserDetails {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "user_id")
        private UUID id;

        @Column(name = "firstname", nullable = false)
        private String firstname;

        @Column(name = "lastname", nullable = false)
        private String lastname;

        @Column(name = "email", unique = true, nullable = false)
        private String email;

        @Column( name = "username", unique = true, nullable = false)
        private String username;

        @Column(name = "password", nullable = false)
        private String password;

        @CreationTimestamp
        private LocalDateTime createdAt;

        @UpdateTimestamp
        private LocalDateTime updatedAt;

        private boolean isAccountNonExpired;

        private boolean isAccountNonLocked;

        public boolean isCredentialsNonExpired;

        public boolean isEnabled;

        //Biometric authentication(Face ID)
        private String faceIdToken;


        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private Set<Oauth2AuthenticatonProvider> oauth2AuthenticatonProviders = new HashSet<>();



        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id")
        )
        private Set<Role> roles = new HashSet<>();




        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return roles;
        }

        @Override
        public String getPassword() {
            return password;
        }


        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
