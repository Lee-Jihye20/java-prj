package com.example.kintai.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username", "company_id"})
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String role; // EMPLOYEE or ADMIN

    @Column(name = "slack_webhook_url")
    private String slackWebhookUrl;

    @Column(name = "slack_user_id")
    private String slackUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToMany
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "work_type", length = 20)
    private String workType; // FULLTIME or FLEX

    @Column(name = "start_time", columnDefinition = "TIME DEFAULT '09:00:00'")
    private LocalTime startTime; // フルタイムの場合の始業時間

    @Column(name = "core_time_start")
    private LocalTime coreTimeStart; // フレックスの場合のコアタイム開始時刻

    @Column(name = "core_time_end")
    private LocalTime coreTimeEnd; // フレックスの場合のコアタイム終了時刻

    // Constructors
    public User() {
    }

    public User(Long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSlackWebhookUrl() {
        return slackWebhookUrl;
    }

    public void setSlackWebhookUrl(String slackWebhookUrl) {
        this.slackWebhookUrl = slackWebhookUrl;
    }

    public String getSlackUserId() {
        return slackUserId;
    }

    public void setSlackUserId(String slackUserId) {
        this.slackUserId = slackUserId;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /**
     * 指定された権限を持っているかチェック
     */
    public boolean hasPermission(String permissionName) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role != null && role.hasPermission(permissionName));
    }

    /**
     * 指定されたロールを持っているかチェック
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getCoreTimeStart() {
        return coreTimeStart;
    }

    public void setCoreTimeStart(LocalTime coreTimeStart) {
        this.coreTimeStart = coreTimeStart;
    }

    public LocalTime getCoreTimeEnd() {
        return coreTimeEnd;
    }

    public void setCoreTimeEnd(LocalTime coreTimeEnd) {
        this.coreTimeEnd = coreTimeEnd;
    }

    /**
     * 保存前にデフォルト値を設定
     */
    @PrePersist
    @PreUpdate
    protected void setDefaults() {
        // work_typeがFULLTIME（またはnull）でstart_timeがnullの場合、デフォルトで9時に設定
        // データベース側でもデフォルト値が設定されているが、アプリケーション側でも設定
        if ((workType == null || "FULLTIME".equals(workType)) && startTime == null) {
            startTime = LocalTime.of(9, 0);
        }
    }
}