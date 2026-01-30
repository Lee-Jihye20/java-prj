package com.example.kintai.service;

import com.example.kintai.entity.AdminActionLog;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AdminActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminActionLogService {

    @Autowired
    private AdminActionLogRepository adminActionLogRepository;

    private static final int RECENT_ACTIONS_LIMIT = 100;

    @Transactional
    public void log(User admin, String actionType, String targetType, Long targetId, String detail) {
        if (admin == null || admin.getId() == null) return;
        AdminActionLog log = new AdminActionLog();
        log.setAdmin(admin);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        adminActionLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AdminActionLog> findRecentByAdminId(Long adminId) {
        return adminActionLogRepository.findTop100ByAdmin_IdOrderByCreatedAtDesc(adminId);
    }
}
