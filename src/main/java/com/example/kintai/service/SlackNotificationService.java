package com.example.kintai.service;

import com.example.kintai.entity.CompanySettings;
import com.example.kintai.entity.User;
import com.example.kintai.repository.CompanySettingsRepository;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SlackNotificationService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private CompanySettingsRepository companySettingsRepository;

    @Autowired
    private UserRepository userRepository;

    public void sendAdminNotification(String message, Long companyId) {
        Optional<CompanySettings> settings = companySettingsRepository.findByCompanyId(companyId);
        settings.ifPresent(s -> {
            // Slack通知が有効で、Webhook URLが設定されている場合のみ送信
            if (Boolean.TRUE.equals(s.getSlackNotificationEnabled()) 
                    && s.getAdminSlackWebhookUrl() != null && !s.getAdminSlackWebhookUrl().isEmpty()) {
                sendSlackMessage(s.getAdminSlackWebhookUrl(), message);
            }
        });
    }

    public void sendAttendanceNotification(String message, Long companyId) {
        Optional<CompanySettings> settings = companySettingsRepository.findByCompanyId(companyId);
        settings.ifPresent(s -> {
            // Slack通知が有効で、ログチャンネルのWebhook URLが設定されている場合のみ送信
            if (Boolean.TRUE.equals(s.getSlackNotificationEnabled()) 
                    && s.getLogSlackWebhookUrl() != null && !s.getLogSlackWebhookUrl().isEmpty()) {
                sendSlackMessage(s.getLogSlackWebhookUrl(), message);
            }
        });
    }

    public void sendUserDM(String payload, Long userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
            if (u.getSlackWebhookUrl() != null && !u.getSlackWebhookUrl().isEmpty()) {
                sendSlackAttachmentMessage(u.getSlackWebhookUrl(), payload);
            }
        });
    }

    public void sendUserDM(Map<String, Object> payload, Long userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
            if (u.getSlackWebhookUrl() != null && !u.getSlackWebhookUrl().isEmpty()) {
                sendSlackAttachmentMessage(u.getSlackWebhookUrl(), payload);
            }
        });
    }

    /**
     * 管理者チャンネルにembed形式でメッセージを送信（異常検知・修正依頼通知用）
     */
    public void sendAdminNotificationWithAttachment(Map<String, Object> attachmentPayload, Long companyId) {
        Optional<CompanySettings> settings = companySettingsRepository.findByCompanyId(companyId);
        settings.ifPresent(s -> {
            // Slack通知が有効で、異常・修正依頼通知用のWebhook URLが設定されている場合のみ送信
            if (Boolean.TRUE.equals(s.getSlackNotificationEnabled()) 
                    && s.getAlertSlackWebhookUrl() != null && !s.getAlertSlackWebhookUrl().isEmpty()) {
                sendSlackAttachmentMessage(s.getAlertSlackWebhookUrl(), attachmentPayload);
            }
        });
    }

    private void sendSlackMessage(String webhookUrl, String message) {
        Map<String, String> slackMessage = new HashMap<>();
        slackMessage.put("text", message);

        try {
            webClientBuilder.build().post()
                    .uri(webhookUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(slackMessage)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> System.out.println("Slack message sent successfully: " + response),
                            error -> System.err.println("Failed to send Slack message: " + error.getMessage())
                    );
        } catch (Exception e) {
            System.err.println("Error sending Slack message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void sendSlackAttachmentMessage(String webhookUrl, String payload) {
        try {
            webClientBuilder.build().post()
                    .uri(webhookUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload) // ← textではなく full JSON をそのまま送信
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> System.out.println("Slack attachment message sent successfully: " + response),
                            error -> System.err.println("Failed to send Slack attachment message: " + error.getMessage())
                    );
        } catch (Exception e) {
            System.err.println("Error sending Slack attachment message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendSlackAttachmentMessage(String webhookUrl, Map<String, Object> payload) {
        try {
            webClientBuilder.build().post()
                    .uri(webhookUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload) // MapをJSONとして送信
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> System.out.println("Slack attachment message sent successfully: " + response),
                            error -> System.err.println("Failed to send Slack attachment message: " + error.getMessage())
                    );
        } catch (Exception e) {
            System.err.println("Error sending Slack attachment message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
