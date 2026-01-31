package com.example.kintai.service;

import com.example.kintai.entity.User;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        throw new UnsupportedOperationException("loadUserByUsername is not supported. Use loadUserByUsernameAndCompanyCode instead.");
    }

    public UserDetails loadUserByUsernameAndCompanyCode(String username, String companyCode) throws UsernameNotFoundException {
        return userRepository.findByUsernameAndCompanyCompanyCode(username, companyCode)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username + " and companyCode: " + companyCode));
    }
}
