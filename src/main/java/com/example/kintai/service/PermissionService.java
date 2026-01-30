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

    /**
     * ユーザーが指定された権限を持っているかチェック
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(User user, String permissionName) {
        if (user == null) {
            return false;
        }

        // ADMINロールの場合は全権限を持つ
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }

        // ユーザーのrolesとpermissionsを一緒にロード
        User loadedUser = userRepository.findByIdWithRoles(user.getId()).orElse(null);
        if (loadedUser == null) {
            return false;
        }

        // カスタムロールで権限をチェック
        return loadedUser.hasPermission(permissionName);
    }

    /**
     * ユーザーが管理者機能にアクセスできるかチェック
     * 管理タイプ（ADMINロール）のみアクセス可能。従業員タイプは管理画面にアクセスできない。
     */
    @Transactional(readOnly = true)
    public boolean canAccessAdminFeatures(User user) {
        if (user == null) {
            return false;
        }
        return "ADMIN".equals(user.getRole());
    }
}
