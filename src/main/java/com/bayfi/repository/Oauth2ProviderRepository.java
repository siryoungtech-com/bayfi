package com.bayfi.repository;

import com.bayfi.entity.Oauth2AuthenticatonProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Oauth2ProviderRepository extends JpaRepository<Oauth2AuthenticatonProvider, Long> {
}
