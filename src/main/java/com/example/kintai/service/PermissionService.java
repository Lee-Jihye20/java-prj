package com.example.kintai.service;

import com.example.kintai.entity.User;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean hasPermission(User user, String permissionName) {
        if (user == null) {
            return false;
        }

        if ("ADMIN".equals(user.getRole())) {
            return true;
        }

        User loadedUser = userRepository.findByIdWithRoles(user.getId()).orElse(null);
        if (loadedUser == null) {
            return false;
        }

        return loadedUser.hasPermission(permissionName);
    }

    @Transactional(readOnly = true)
    public boolean canAccessAdminFeatures(User user) {
        if (user == null) {
            return false;
        }
        return "ADMIN".equals(user.getRole());
    }
}
