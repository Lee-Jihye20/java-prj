package com.example.kintai.service;

import com.example.kintai.entity.Company;
import com.example.kintai.entity.Permission;
import com.example.kintai.entity.Role;
import com.example.kintai.entity.User;
import com.example.kintai.repository.PermissionRepository;
import com.example.kintai.repository.RoleRepository;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 企業の全ロールを取得（権限情報を含む）
     */
    public List<Role> getRolesByCompanyId(Long companyId) {
        return roleRepository.findByCompany_IdWithPermissions(companyId);
    }

    /**
     * ロールを取得
     */
    public Optional<Role> getRoleById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    /**
     * ロールを作成
     */
    @Transactional
    public Role createRole(String name, String description, Long companyId, Set<Long> permissionIds) {
        // 同じ企業内でロール名の重複チェック
        Optional<Role> existingRole = roleRepository.findByNameAndCompany_Id(name, companyId);
        if (existingRole.isPresent()) {
            throw new IllegalArgumentException("このロール名は既に使用されています");
        }

        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        Company company = new Company();
        company.setId(companyId);
        role.setCompany(company);
        role.setIsSystemRole(false);

        // 権限を設定
        if (permissionIds != null && !permissionIds.isEmpty()) {
            Set<Permission> permissions = permissionIds.stream()
                    .map(permissionRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    /**
     * ロールを更新
     */
    @Transactional
    public Role updateRole(Long roleId, String name, String description, Set<Long> permissionIds) {
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if (optionalRole.isEmpty()) {
            throw new IllegalArgumentException("ロールが見つかりません");
        }

        Role role = optionalRole.get();

        // システムロールは編集不可
        if (role.getIsSystemRole()) {
            throw new IllegalArgumentException("システムロールは編集できません");
        }

        // ロール名の重複チェック（自分自身を除く）
        Optional<Role> existingRole = roleRepository.findByNameAndCompany_Id(name, role.getCompanyId());
        if (existingRole.isPresent() && !existingRole.get().getId().equals(roleId)) {
            throw new IllegalArgumentException("このロール名は既に使用されています");
        }

        role.setName(name);
        role.setDescription(description);

        // 権限を更新
        if (permissionIds != null) {
            Set<Permission> permissions = permissionIds.stream()
                    .map(permissionRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    /**
     * ロールを削除
     */
    @Transactional
    public void deleteRole(Long roleId) {
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if (optionalRole.isEmpty()) {
            throw new IllegalArgumentException("ロールが見つかりません");
        }

        Role role = optionalRole.get();

        // システムロールは削除不可
        if (role.getIsSystemRole()) {
            throw new IllegalArgumentException("システムロールは削除できません");
        }

        // ユーザーに割り当てられている場合は削除不可
        if (!role.getUsers().isEmpty()) {
            throw new IllegalArgumentException("このロールはユーザーに割り当てられているため削除できません");
        }

        roleRepository.delete(role);
    }

    /**
     * 全権限を取得
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * カテゴリ別に権限を取得
     */
    public List<Permission> getPermissionsByCategory(String category) {
        return permissionRepository.findByCategory(category);
    }

    /**
     * ユーザーにロールを割り当て
     */
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("ユーザーが見つかりません");
        }

        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if (optionalRole.isEmpty()) {
            throw new IllegalArgumentException("ロールが見つかりません");
        }

        User user = optionalUser.get();
        Role role = optionalRole.get();

        // 同じ企業に属しているか確認
        if (!user.getCompany().getId().equals(role.getCompanyId())) {
            throw new IllegalArgumentException("ユーザーとロールが異なる企業に属しています");
        }

        user.getRoles().add(role);
        userRepository.save(user);
    }

    /**
     * ユーザーからロールを削除
     */
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("ユーザーが見つかりません");
        }

        User user = optionalUser.get();
        user.getRoles().removeIf(role -> role.getId().equals(roleId));
        userRepository.save(user);
    }

    /**
     * ユーザーのロールを更新
     */
    @Transactional
    public void updateUserRoles(Long userId, Set<Long> roleIds) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("ユーザーが見つかりません");
        }

        User user = optionalUser.get();

        // 新しいロールセットを作成
        Set<Role> newRoles = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                Optional<Role> optionalRole = roleRepository.findById(roleId);
                if (optionalRole.isPresent()) {
                    Role role = optionalRole.get();
                    // 同じ企業に属しているか確認
                    if (user.getCompany().getId().equals(role.getCompanyId())) {
                        newRoles.add(role);
                    }
                }
            }
        }

        user.setRoles(newRoles);
        userRepository.save(user);
    }
}
