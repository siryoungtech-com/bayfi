package com.bayfi.service.implementation;

import com.bayfi.entity.User;
import com.bayfi.repository.UserRepository;
import com.bayfi.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

     private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void processOauth2User(String email, String provider, String providerId) {
            User user = userRepository.findByEmail(email)
                    .orElse(new User());
            user.setEmail(email);
            userRepository.save(user);
        }


}
