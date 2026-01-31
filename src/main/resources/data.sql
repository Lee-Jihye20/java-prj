
INSERT INTO companies (name, company_code) VALUES
('Test Inc.', 'testinc');

INSERT INTO company_settings (company_id, admin_slack_webhook_url, attendance_slack_webhook_url, slack_notification_enabled, log_slack_webhook_url, alert_slack_webhook_url) VALUES
(1, NULL, NULL, true, NULL, NULL);

INSERT INTO permissions (name, description, category) VALUES
('EXPORT_DATA', 'データエクスポート（日別・月次・ログ）', 'EXPORT'),
('VIEW_LOG', 'ログの閲覧', 'VIEW'),
('VIEW_USER_LIST', 'ユーザー一覧の閲覧', 'VIEW'),
('MANAGE_USER', 'ユーザーの作成・編集・削除', 'MANAGE'),
('MANAGE_ROLE', 'ロールの作成・編集・削除', 'MANAGE'),
('APPROVE_FIX_REQUEST', '修正依頼の承認・却下', 'MANAGE'),
('VIEW_ATTENDANCE_CALENDAR', '勤怠カレンダーの閲覧', 'VIEW'),
('VIEW_MONTHLY_REPORT', '月次レポートの閲覧', 'VIEW'),
('MANAGE_COMPANY_SETTINGS', '企業設定の変更', 'MANAGE');

INSERT INTO roles (name, description, company_id, is_system_role) VALUES
('ADMIN', '管理者（全権限）', 1, true),
('EMPLOYEE', '従業員（基本権限のみ）', 1, true);

INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permissions;

INSERT INTO role_permission (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name IN ('VIEW_ATTENDANCE_CALENDAR', 'VIEW_MONTHLY_REPORT');

INSERT INTO users (username, password, role, company_id, slack_webhook_url, slack_user_id) VALUES
('employee1', 'password', 'EMPLOYEE', 1, NULL, NULL),
('employee2', 'password', 'EMPLOYEE', 1, NULL, NULL),
('employee3', 'password', 'EMPLOYEE', 1, NULL, NULL),
('admin1', 'adminpass', 'ADMIN', 1, NULL, NULL),
('admin2', 'adminpass', 'ADMIN', 1, NULL, NULL);

INSERT INTO attendance (user_id, check_in, check_out, status) VALUES

(1, CURRENT_DATE - INTERVAL '30 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '30 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '29 days' + TIME '09:15:00', CURRENT_DATE - INTERVAL '29 days' + TIME '18:30:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '28 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '28 days' + TIME '17:45:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '27 days' + TIME '09:30:00', CURRENT_DATE - INTERVAL '27 days' + TIME '19:00:00', 'APPROVED'), 
(1, CURRENT_DATE - INTERVAL '26 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '26 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '25 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '25 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '24 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '24 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '23 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '23 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '22 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '22 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '21 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '21 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '20 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '20 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '19 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '19 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '18 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '18 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '17 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '17 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '16 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '16 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '15 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '15 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '14 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '14 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '13 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '13 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '12 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '12 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '11 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '11 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '10 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '10 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '9 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '9 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '8 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '8 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '7 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '7 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '6 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '6 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '5 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '5 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '4 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '4 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '3 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '3 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '2 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '2 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '1 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '1 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE + TIME '09:00:00', NULL, 'APPROVED'); 

INSERT INTO attendance (user_id, check_in, check_out, status) VALUES
(1, CURRENT_DATE - INTERVAL '6 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '6 days' + TIME '19:00:00', 'APPROVED');

INSERT INTO attendance (user_id, check_in, check_out, status) VALUES
(2, CURRENT_DATE - INTERVAL '10 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '10 days' + TIME '18:00:00', 'APPROVED'),
(2, CURRENT_DATE - INTERVAL '9 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '9 days' + TIME '18:00:00', 'APPROVED'),
(2, CURRENT_DATE - INTERVAL '8 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '8 days' + TIME '18:00:00', 'APPROVED'),
(2, CURRENT_DATE - INTERVAL '7 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '7 days' + TIME '18:00:00', 'APPROVED'),
(2, CURRENT_DATE - INTERVAL '6 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '6 days' + TIME '18:00:00', 'APPROVED'),
(2, CURRENT_DATE - INTERVAL '5 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '5 days' + TIME '18:00:00', 'APPROVED'),
(2, CURRENT_DATE - INTERVAL '4 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '4 days' + TIME '18:00:00', 'APPROVED'),
(2, CURRENT_DATE - INTERVAL '3 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '3 days' + TIME '18:00:00', 'APPROVED'),
(2, CURRENT_DATE - INTERVAL '2 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '2 days' + TIME '18:00:00', 'APPROVED'),
(2, CURRENT_DATE - INTERVAL '1 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '1 days' + TIME '18:00:00', 'APPROVED');

INSERT INTO attendance (user_id, check_in, check_out, status) VALUES
(3, CURRENT_DATE - INTERVAL '5 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '5 days' + TIME '18:00:00', 'APPROVED'),
(3, CURRENT_DATE - INTERVAL '4 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '4 days' + TIME '18:00:00', 'APPROVED'),
(3, CURRENT_DATE - INTERVAL '3 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '3 days' + TIME '18:00:00', 'APPROVED'),
(3, CURRENT_DATE - INTERVAL '2 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '2 days' + TIME '18:00:00', 'APPROVED'),
(3, CURRENT_DATE - INTERVAL '1 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '1 days' + TIME '18:00:00', 'APPROVED');

INSERT INTO attendance (user_id, check_in, check_out, status) VALUES
(1, CURRENT_DATE - INTERVAL '3 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '3 days' + TIME '19:00:00', 'APPROVED');

INSERT INTO break_record (attendance_id, break_start, break_end, break_type) VALUES

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '30 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '30 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '30 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '29 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '29 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '29 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '28 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '28 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '28 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '28 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '28 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '28 days' + TIME '15:15:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '27 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '27 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '27 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '26 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '26 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '26 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '25 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '25 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '25 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '24 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '24 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '24 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '23 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '23 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '23 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '22 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '22 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '22 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '21 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '21 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '21 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '20 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '20 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '20 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '19 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '19 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '19 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '18 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '18 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '18 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '17 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '17 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '17 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '16 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '16 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '16 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '15 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '15 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '15 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '14 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '14 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '14 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '13 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '13 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '13 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '12 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '12 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '12 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '11 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '11 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '11 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '10 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '10 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '10 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '9 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '9 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '9 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '8 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '8 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '8 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '7 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '7 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '6 days' AND check_out::time = TIME '18:00:00' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '6 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '6 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '5 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '4 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '4 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '4 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' AND check_out::time = TIME '18:00:00' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' AND check_out::time = TIME '19:00:00' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '2 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '2 days' + TIME '13:00:00', 'FREE'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '1 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '1 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '13:00:00', 'FREE');

INSERT INTO break_record (attendance_id, break_start, break_end, break_type) VALUES
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '10 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '10 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '10 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '9 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '9 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '9 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '8 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '8 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '8 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '7 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '7 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '6 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '6 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '6 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '5 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '4 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '4 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '4 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '2 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '2 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '1 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '1 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '13:00:00', 'FREE');

INSERT INTO break_record (attendance_id, break_start, break_end, break_type) VALUES
((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '5 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '4 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '4 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '4 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '2 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '2 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '1 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '1 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '13:00:00', 'FREE');

INSERT INTO leave_record (attendance_id, leave_start, leave_end) VALUES

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '30 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '30 days' + TIME '14:00:00', 
 CURRENT_DATE - INTERVAL '30 days' + TIME '14:30:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '29 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '29 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '29 days' + TIME '15:20:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '28 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '28 days' + TIME '10:30:00', 
 CURRENT_DATE - INTERVAL '28 days' + TIME '10:45:00'),
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '28 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '28 days' + TIME '16:00:00', 
 CURRENT_DATE - INTERVAL '28 days' + TIME '16:15:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '27 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '27 days' + TIME '14:30:00', 
 CURRENT_DATE - INTERVAL '27 days' + TIME '15:00:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '26 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '26 days' + TIME '11:00:00', 
 CURRENT_DATE - INTERVAL '26 days' + TIME '11:30:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '20 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '20 days' + TIME '14:00:00', 
 CURRENT_DATE - INTERVAL '20 days' + TIME '14:30:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '15 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '15 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '15 days' + TIME '15:20:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '10 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '10 days' + TIME '14:00:00', 
 CURRENT_DATE - INTERVAL '10 days' + TIME '14:45:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '7 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '7 days' + TIME '10:30:00', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '11:00:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '5 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '15:30:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '14:15:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '14:30:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '2 days' + TIME '16:00:00', 
 CURRENT_DATE - INTERVAL '2 days' + TIME '16:15:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '1 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '1 days' + TIME '11:00:00', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '11:20:00');

INSERT INTO leave_record (attendance_id, leave_start, leave_end) VALUES
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '10 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '10 days' + TIME '14:00:00', 
 CURRENT_DATE - INTERVAL '10 days' + TIME '14:30:00'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '7 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '7 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '15:15:00'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '5 days' + TIME '10:30:00', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '11:00:00'),
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '14:00:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '14:20:00');

INSERT INTO leave_record (attendance_id, leave_start, leave_end) VALUES
((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '5 days' + TIME '14:00:00', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '14:30:00'),
((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '4 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '4 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '4 days' + TIME '15:15:00'),
((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '11:00:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '11:30:00');

INSERT INTO fix_request (attendance_id, user_id, request_type, new_value, reason, status, created_at) VALUES

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 1, 'CHECK_IN', CURRENT_DATE - INTERVAL '5 days' + TIME '08:45:00', 
 '出勤時刻を間違えて打刻してしまいました。正しい時刻に修正をお願いします。', 'PENDING', 
 CURRENT_DATE - INTERVAL '4 days' + TIME '10:00:00'),

((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' ORDER BY id LIMIT 1), 
 2, 'CHECK_OUT', CURRENT_DATE - INTERVAL '3 days' + TIME '18:30:00', 
 '退勤時刻を打ち忘れました。実際の退勤時刻に修正をお願いします。', 'PENDING', 
 CURRENT_DATE - INTERVAL '2 days' + TIME '09:00:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 1, 'BREAK_START', CURRENT_DATE - INTERVAL '2 days' + TIME '12:15:00', 
 '休憩開始時刻を間違えて記録してしまいました。', 'PENDING', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '10:30:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '4 days' ORDER BY id LIMIT 1), 
 1, 'LEAVE_START', CURRENT_DATE - INTERVAL '4 days' + TIME '14:10:00', 
 '中抜け開始時刻を間違えて記録してしまいました。', 'PENDING', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '09:00:00'),

((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 2, 'LEAVE_END', CURRENT_DATE - INTERVAL '2 days' + TIME '14:35:00', 
 '中抜け終了時刻を打ち忘れました。', 'PENDING', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '10:00:00');

INSERT INTO fix_request (attendance_id, user_id, request_type, new_value, reason, status, created_at) VALUES

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '10 days' ORDER BY id LIMIT 1), 
 1, 'CHECK_IN', CURRENT_DATE - INTERVAL '10 days' + TIME '08:50:00', 
 '出勤時刻を間違えて打刻しました。正しい時刻は8:50です。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '9 days' + TIME '10:00:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '8 days' ORDER BY id LIMIT 1), 
 1, 'CHECK_OUT', CURRENT_DATE - INTERVAL '8 days' + TIME '18:15:00', 
 '退勤時刻を打ち忘れていました。実際の退勤時刻は18:15です。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '09:30:00'),

((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '7 days' ORDER BY id LIMIT 1), 
 2, 'CHECK_IN', CURRENT_DATE - INTERVAL '7 days' + TIME '09:10:00', 
 '出勤時刻を間違えて打刻しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '6 days' + TIME '11:00:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '6 days' ORDER BY id LIMIT 1), 
 1, 'BREAK_END', CURRENT_DATE - INTERVAL '6 days' + TIME '13:10:00', 
 '休憩終了時刻を間違えて記録しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '14:00:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '12 days' ORDER BY id LIMIT 1), 
 1, 'LEAVE_START', CURRENT_DATE - INTERVAL '12 days' + TIME '14:05:00', 
 '中抜け開始時刻を間違えて記録しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '11 days' + TIME '10:00:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '11 days' ORDER BY id LIMIT 1), 
 1, 'LEAVE_END', CURRENT_DATE - INTERVAL '11 days' + TIME '14:40:00', 
 '中抜け終了時刻を間違えて記録しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '10 days' + TIME '11:00:00'),

((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '8 days' ORDER BY id LIMIT 1), 
 2, 'LEAVE_START', CURRENT_DATE - INTERVAL '8 days' + TIME '14:10:00', 
 '中抜け開始時刻を間違えて記録しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '10:30:00');

INSERT INTO fix_request (attendance_id, user_id, request_type, new_value, reason, status, created_at) VALUES

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '15 days' ORDER BY id LIMIT 1), 
 1, 'CHECK_IN', CURRENT_DATE - INTERVAL '15 days' + TIME '08:30:00', 
 '出勤時刻を間違えて打刻しました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '14 days' + TIME '10:00:00'),

((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '6 days'), 
 2, 'CHECK_OUT', CURRENT_DATE - INTERVAL '6 days' + TIME '19:00:00', 
 '退勤時刻を打ち忘れました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '09:00:00'),

((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '4 days' ORDER BY id LIMIT 1), 
 3, 'CHECK_IN', CURRENT_DATE - INTERVAL '4 days' + TIME '09:20:00', 
 '出勤時刻を間違えて打刻しました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '11:00:00'),

((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '18 days'), 
 1, 'LEAVE_START', CURRENT_DATE - INTERVAL '18 days' + TIME '14:00:00', 
 '中抜け開始時刻を間違えて記録しました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '17 days' + TIME '10:00:00'),

((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '9 days'), 
 2, 'LEAVE_END', CURRENT_DATE - INTERVAL '9 days' + TIME '15:30:00', 
 '中抜け終了時刻を打ち忘れました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '8 days' + TIME '09:00:00');

INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(1, DATE_TRUNC('month', CURRENT_DATE)::date, 
 2, 
 85.5, 
 3, 
 15, 
 92.0, 
 82.5, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(1, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date, 
 1, 
 90.0, 
 2, 
 20, 
 95.0, 
 88.0, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(1, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '2 months')::date, 
 0, 
 100.0, 
 1, 
 22, 
 98.0, 
 92.0, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(2, DATE_TRUNC('month', CURRENT_DATE)::date, 
 0, 
 100.0, 
 0, 
 10, 
 100.0, 
 94.0, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(2, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date, 
 1, 
 80.0, 
 2, 
 12, 
 88.0, 
 80.0, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(2, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '2 months')::date, 
 3, 
 75.0, 
 4, 
 8, 
 85.0, 
 72.0, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(3, DATE_TRUNC('month', CURRENT_DATE)::date, 
 1, 
 95.0, 
 1, 
 5, 
 90.0, 
 85.0, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(3, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date, 
 2, 
 88.0, 
 2, 
 7, 
 92.0, 
 82.0, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(3, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '2 months')::date, 
 0, 
 100.0, 
 0, 
 18, 
 100.0, 
 96.0, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
