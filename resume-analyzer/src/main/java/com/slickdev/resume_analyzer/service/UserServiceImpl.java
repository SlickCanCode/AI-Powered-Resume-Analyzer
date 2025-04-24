package com.slickdev.resume_analyzer.service;


import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.repositories.UserRepository;


import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService{
    
    private BCryptPasswordEncoder passwordEncoder;
    private UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User getUser(long id) {
        Optional<User> user = userRepository.findById(id);
        return unwrapUser(user, id);
    }

    @Override
    public User getUser(String username) {
        Optional<User> user = userRepository.findByUserNameOrEmail(username, username);
        return unwrapUser(user, 404L);
    }

    @Override
    public void deleteUser(Long id) {
         userRepository.deleteById(id);
    }

    static User unwrapUser(Optional<User> entity, Long id) {
        if (entity.isPresent()) return entity.get();
        else throw new EntityNotFoundException(id, User.class);
    }

    
}
