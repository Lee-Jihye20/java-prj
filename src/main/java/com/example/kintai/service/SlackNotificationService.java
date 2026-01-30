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
            if (s.getAdminSlackWebhookUrl() != null && !s.getAdminSlackWebhookUrl().isEmpty()) {
                sendSlackMessage(s.getAdminSlackWebhookUrl(), message);
            }
        });
    }

    public void sendAttendanceNotification(String message, Long companyId) {
        Optional<CompanySettings> settings = companySettingsRepository.findByCompanyId(companyId);
        settings.ifPresent(s -> {
            if (s.getAttendanceSlackWebhookUrl() != null && !s.getAttendanceSlackWebhookUrl().isEmpty()) {
                sendSlackMessage(s.getAttendanceSlackWebhookUrl(), message);
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
     * 管理者チャンネルにembed形式でメッセージを送信
     */
    public void sendAdminNotificationWithAttachment(Map<String, Object> attachmentPayload, Long companyId) {
        Optional<CompanySettings> settings = companySettingsRepository.findByCompanyId(companyId);
        settings.ifPresent(s -> {
            if (s.getAdminSlackWebhookUrl() != null && !s.getAdminSlackWebhookUrl().isEmpty()) {
                sendSlackAttachmentMessage(s.getAdminSlackWebhookUrl(), attachmentPayload);
            }
        });
    }

    private void sendSlackMessage(String webhookUrl, String message) {
        Map<String, String> slackMessage = new HashMap<>();
        slackMessage.put("text", message);

        webClientBuilder.build().post()
                .uri(webhookUrl)
                .bodyValue(slackMessage)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> System.out.println("Slack message sent successfully: " + response),
                        error -> System.err.println("Failed to send Slack message: " + error.getMessage())
                );
    }
    private void sendSlackAttachmentMessage(String webhookUrl, String payload) {
        webClientBuilder.build().post()
                .uri(webhookUrl)
                .bodyValue(payload) // ← textではなく full JSON をそのまま送信
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> System.out.println("Slack attachment message sent successfully: " + response),
                        error -> System.err.println("Failed to send Slack attachment message: " + error.getMessage())
                );
    }

    private void sendSlackAttachmentMessage(String webhookUrl, Map<String, Object> payload) {
        webClientBuilder.build().post()
                .uri(webhookUrl)
                .bodyValue(payload) // MapをJSONとして送信
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> System.out.println("Slack attachment message sent successfully: " + response),
                        error -> System.err.println("Failed to send Slack attachment message: " + error.getMessage())
                );
    }
}
