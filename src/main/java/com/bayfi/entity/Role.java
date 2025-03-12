package com.bayfi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
@Getter
@Setter
public class Role  implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String authority;


    @Override
    public String getAuthority() {
        return this.authority;
    }

    @ManyToMany(mappedBy = "roles")
    private Set<User> user;
}
