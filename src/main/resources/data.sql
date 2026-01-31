-- 初期企業データ
INSERT INTO companies (name, company_code) VALUES
('Test Inc.', 'testinc');

-- 初期企業設定データ（Slack通知設定）
-- 注意: 実際のWebhook URLは設定画面から設定してください
INSERT INTO company_settings (company_id, admin_slack_webhook_url, attendance_slack_webhook_url, slack_notification_enabled, log_slack_webhook_url, alert_slack_webhook_url) VALUES
(1, NULL, NULL, true, NULL, NULL);

-- 権限データ
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

-- デフォルトロール（システムロール）
INSERT INTO roles (name, description, company_id, is_system_role) VALUES
('ADMIN', '管理者（全権限）', 1, true),
('EMPLOYEE', '従業員（基本権限のみ）', 1, true);

-- 管理者ロールに全権限を付与
INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permissions;

-- 従業員ロールに基本権限を付与
INSERT INTO role_permission (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name IN ('VIEW_ATTENDANCE_CALENDAR', 'VIEW_MONTHLY_REPORT');

-- 初期ユーザーデータ (パスワードは平文で保存、実運用ではBCryptを使用すべき)
-- employee1 / password
-- admin1 / adminpass

INSERT INTO users (username, password, role, company_id, slack_webhook_url, slack_user_id) VALUES
('employee1', 'password', 'EMPLOYEE', 1, NULL, NULL),
('employee2', 'password', 'EMPLOYEE', 1, NULL, NULL),
('employee3', 'password', 'EMPLOYEE', 1, NULL, NULL),
('admin1', 'adminpass', 'ADMIN', 1, NULL, NULL),
('admin2', 'adminpass', 'ADMIN', 1, NULL, NULL);

-- ダミー勤怠データ（過去30日分）
-- employee1の勤怠データ
INSERT INTO attendance (user_id, check_in, check_out, status) VALUES
-- 今日から30日前まで
(1, CURRENT_DATE - INTERVAL '30 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '30 days' + TIME '18:00:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '29 days' + TIME '09:15:00', CURRENT_DATE - INTERVAL '29 days' + TIME '18:30:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '28 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '28 days' + TIME '17:45:00', 'APPROVED'),
(1, CURRENT_DATE - INTERVAL '27 days' + TIME '09:30:00', CURRENT_DATE - INTERVAL '27 days' + TIME '19:00:00', 'APPROVED'), -- 残業あり
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
(1, CURRENT_DATE + TIME '09:00:00', NULL, 'APPROVED'); -- 今日は出勤済み、退勤未打刻

-- 過剰残業のダミーデータ（異常検知用）
-- employee1の6日前に10時間勤務（9:00-19:00、休憩なしで実働10時間 = 600分）を追加
-- 既存データと重複しないよう、6日前に追加
INSERT INTO attendance (user_id, check_in, check_out, status) VALUES
(1, CURRENT_DATE - INTERVAL '6 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '6 days' + TIME '19:00:00', 'APPROVED');

-- employee2の勤怠データ（過去10日分）
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

-- employee3の勤怠データ（過去5日分）
INSERT INTO attendance (user_id, check_in, check_out, status) VALUES
(3, CURRENT_DATE - INTERVAL '5 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '5 days' + TIME '18:00:00', 'APPROVED'),
(3, CURRENT_DATE - INTERVAL '4 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '4 days' + TIME '18:00:00', 'APPROVED'),
(3, CURRENT_DATE - INTERVAL '3 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '3 days' + TIME '18:00:00', 'APPROVED'),
(3, CURRENT_DATE - INTERVAL '2 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '2 days' + TIME '18:00:00', 'APPROVED'),
(3, CURRENT_DATE - INTERVAL '1 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '1 days' + TIME '18:00:00', 'APPROVED');

-- 過剰残業のダミーデータ（異常検知用）
-- employee1の3日前に10時間勤務（9:00-19:00、休憩1時間で実働9時間 = 540分）を追加
INSERT INTO attendance (user_id, check_in, check_out, status) VALUES
(1, CURRENT_DATE - INTERVAL '3 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '3 days' + TIME '19:00:00', 'APPROVED');

-- 休憩記録データ（すべての勤怠記録に休憩時間を追加）
-- employee1の休憩記録（過去30日分すべて）
INSERT INTO break_record (attendance_id, break_start, break_end, break_type) VALUES
-- 30日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '30 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '30 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '30 days' + TIME '13:00:00', 'FREE'),
-- 29日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '29 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '29 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '29 days' + TIME '13:00:00', 'FREE'),
-- 28日前の休憩（複数回）
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '28 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '28 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '28 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '28 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '28 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '28 days' + TIME '15:15:00', 'FREE'),
-- 27日前の休憩（残業日）
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '27 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '27 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '27 days' + TIME '13:00:00', 'FREE'),
-- 26日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '26 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '26 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '26 days' + TIME '13:00:00', 'FREE'),
-- 25日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '25 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '25 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '25 days' + TIME '13:00:00', 'FREE'),
-- 24日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '24 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '24 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '24 days' + TIME '13:00:00', 'FREE'),
-- 23日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '23 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '23 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '23 days' + TIME '13:00:00', 'FREE'),
-- 22日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '22 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '22 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '22 days' + TIME '13:00:00', 'FREE'),
-- 21日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '21 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '21 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '21 days' + TIME '13:00:00', 'FREE'),
-- 20日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '20 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '20 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '20 days' + TIME '13:00:00', 'FREE'),
-- 19日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '19 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '19 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '19 days' + TIME '13:00:00', 'FREE'),
-- 18日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '18 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '18 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '18 days' + TIME '13:00:00', 'FREE'),
-- 17日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '17 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '17 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '17 days' + TIME '13:00:00', 'FREE'),
-- 16日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '16 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '16 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '16 days' + TIME '13:00:00', 'FREE'),
-- 15日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '15 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '15 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '15 days' + TIME '13:00:00', 'FREE'),
-- 14日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '14 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '14 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '14 days' + TIME '13:00:00', 'FREE'),
-- 13日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '13 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '13 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '13 days' + TIME '13:00:00', 'FREE'),
-- 12日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '12 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '12 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '12 days' + TIME '13:00:00', 'FREE'),
-- 11日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '11 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '11 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '11 days' + TIME '13:00:00', 'FREE'),
-- 10日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '10 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '10 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '10 days' + TIME '13:00:00', 'FREE'),
-- 9日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '9 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '9 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '9 days' + TIME '13:00:00', 'FREE'),
-- 8日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '8 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '8 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '8 days' + TIME '13:00:00', 'FREE'),
-- 7日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '7 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '7 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '13:00:00', 'FREE'),
-- 6日前の休憩（過剰残業日、休憩なしのデータも存在するが、通常の勤怠データには休憩を追加）
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '6 days' AND check_out::time = TIME '18:00:00' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '6 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '6 days' + TIME '13:00:00', 'FREE'),
-- 5日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '5 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '13:00:00', 'FREE'),
-- 4日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '4 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '4 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '4 days' + TIME '13:00:00', 'FREE'),
-- 3日前の休憩（残業日、複数のattendanceレコードがあるため注意）
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' AND check_out::time = TIME '18:00:00' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '13:00:00', 'FREE'),
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' AND check_out::time = TIME '19:00:00' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '13:00:00', 'FREE'),
-- 2日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '2 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '2 days' + TIME '13:00:00', 'FREE'),
-- 1日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '1 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '1 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '13:00:00', 'FREE');

-- employee2の休憩記録（過去10日分すべて）
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

-- employee3の休憩記録（過去5日分すべて）
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

-- 中抜け記録データ（employee1の過去数日分）
INSERT INTO leave_record (attendance_id, leave_start, leave_end) VALUES
-- 30日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '30 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '30 days' + TIME '14:00:00', 
 CURRENT_DATE - INTERVAL '30 days' + TIME '14:30:00'),
-- 29日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '29 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '29 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '29 days' + TIME '15:20:00'),
-- 28日前の中抜け（複数回）
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '28 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '28 days' + TIME '10:30:00', 
 CURRENT_DATE - INTERVAL '28 days' + TIME '10:45:00'),
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '28 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '28 days' + TIME '16:00:00', 
 CURRENT_DATE - INTERVAL '28 days' + TIME '16:15:00'),
-- 27日前の中抜け（残業日）
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '27 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '27 days' + TIME '14:30:00', 
 CURRENT_DATE - INTERVAL '27 days' + TIME '15:00:00'),
-- 26日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '26 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '26 days' + TIME '11:00:00', 
 CURRENT_DATE - INTERVAL '26 days' + TIME '11:30:00'),
-- 20日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '20 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '20 days' + TIME '14:00:00', 
 CURRENT_DATE - INTERVAL '20 days' + TIME '14:30:00'),
-- 15日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '15 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '15 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '15 days' + TIME '15:20:00'),
-- 10日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '10 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '10 days' + TIME '14:00:00', 
 CURRENT_DATE - INTERVAL '10 days' + TIME '14:45:00'),
-- 7日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '7 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '7 days' + TIME '10:30:00', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '11:00:00'),
-- 5日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '5 days' + TIME '15:00:00', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '15:30:00'),
-- 3日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '14:15:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '14:30:00'),
-- 2日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '2 days' + TIME '16:00:00', 
 CURRENT_DATE - INTERVAL '2 days' + TIME '16:15:00'),
-- 1日前の中抜け
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '1 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '1 days' + TIME '11:00:00', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '11:20:00');

-- employee2の中抜け記録（過去数日分）
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

-- employee3の中抜け記録（過去数日分）
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

-- 修正依頼データ
-- PENDING（保留中）の修正依頼
INSERT INTO fix_request (attendance_id, user_id, request_type, new_value, reason, status, created_at) VALUES
-- employee1の修正依頼（保留中）
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 1, 'CHECK_IN', CURRENT_DATE - INTERVAL '5 days' + TIME '08:45:00', 
 '出勤時刻を間違えて打刻してしまいました。正しい時刻に修正をお願いします。', 'PENDING', 
 CURRENT_DATE - INTERVAL '4 days' + TIME '10:00:00'),
-- employee2の修正依頼（保留中）
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' ORDER BY id LIMIT 1), 
 2, 'CHECK_OUT', CURRENT_DATE - INTERVAL '3 days' + TIME '18:30:00', 
 '退勤時刻を打ち忘れました。実際の退勤時刻に修正をお願いします。', 'PENDING', 
 CURRENT_DATE - INTERVAL '2 days' + TIME '09:00:00'),
-- employee1の修正依頼（保留中）- 休憩開始時刻
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 1, 'BREAK_START', CURRENT_DATE - INTERVAL '2 days' + TIME '12:15:00', 
 '休憩開始時刻を間違えて記録してしまいました。', 'PENDING', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '10:30:00'),
-- employee1の修正依頼（保留中）- 中抜け開始時刻
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '4 days' ORDER BY id LIMIT 1), 
 1, 'LEAVE_START', CURRENT_DATE - INTERVAL '4 days' + TIME '14:10:00', 
 '中抜け開始時刻を間違えて記録してしまいました。', 'PENDING', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '09:00:00'),
-- employee2の修正依頼（保留中）- 中抜け終了時刻
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '2 days' ORDER BY id LIMIT 1), 
 2, 'LEAVE_END', CURRENT_DATE - INTERVAL '2 days' + TIME '14:35:00', 
 '中抜け終了時刻を打ち忘れました。', 'PENDING', 
 CURRENT_DATE - INTERVAL '1 days' + TIME '10:00:00');

-- APPROVED（承認済み）の修正依頼
INSERT INTO fix_request (attendance_id, user_id, request_type, new_value, reason, status, created_at) VALUES
-- employee1の修正依頼（承認済み）
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '10 days' ORDER BY id LIMIT 1), 
 1, 'CHECK_IN', CURRENT_DATE - INTERVAL '10 days' + TIME '08:50:00', 
 '出勤時刻を間違えて打刻しました。正しい時刻は8:50です。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '9 days' + TIME '10:00:00'),
-- employee1の修正依頼（承認済み）- 退勤時刻
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '8 days' ORDER BY id LIMIT 1), 
 1, 'CHECK_OUT', CURRENT_DATE - INTERVAL '8 days' + TIME '18:15:00', 
 '退勤時刻を打ち忘れていました。実際の退勤時刻は18:15です。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '09:30:00'),
-- employee2の修正依頼（承認済み）
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '7 days' ORDER BY id LIMIT 1), 
 2, 'CHECK_IN', CURRENT_DATE - INTERVAL '7 days' + TIME '09:10:00', 
 '出勤時刻を間違えて打刻しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '6 days' + TIME '11:00:00'),
-- employee1の修正依頼（承認済み）- 休憩終了時刻
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '6 days' ORDER BY id LIMIT 1), 
 1, 'BREAK_END', CURRENT_DATE - INTERVAL '6 days' + TIME '13:10:00', 
 '休憩終了時刻を間違えて記録しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '14:00:00'),
-- employee1の修正依頼（承認済み）- 中抜け開始時刻
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '12 days' ORDER BY id LIMIT 1), 
 1, 'LEAVE_START', CURRENT_DATE - INTERVAL '12 days' + TIME '14:05:00', 
 '中抜け開始時刻を間違えて記録しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '11 days' + TIME '10:00:00'),
-- employee1の修正依頼（承認済み）- 中抜け終了時刻
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '11 days' ORDER BY id LIMIT 1), 
 1, 'LEAVE_END', CURRENT_DATE - INTERVAL '11 days' + TIME '14:40:00', 
 '中抜け終了時刻を間違えて記録しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '10 days' + TIME '11:00:00'),
-- employee2の修正依頼（承認済み）- 中抜け開始時刻
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '8 days' ORDER BY id LIMIT 1), 
 2, 'LEAVE_START', CURRENT_DATE - INTERVAL '8 days' + TIME '14:10:00', 
 '中抜け開始時刻を間違えて記録しました。', 'APPROVED', 
 CURRENT_DATE - INTERVAL '7 days' + TIME '10:30:00');

-- REJECTED（却下）の修正依頼
INSERT INTO fix_request (attendance_id, user_id, request_type, new_value, reason, status, created_at) VALUES
-- employee1の修正依頼（却下）
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '15 days' ORDER BY id LIMIT 1), 
 1, 'CHECK_IN', CURRENT_DATE - INTERVAL '15 days' + TIME '08:30:00', 
 '出勤時刻を間違えて打刻しました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '14 days' + TIME '10:00:00'),
-- employee2の修正依頼（却下）
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '6 days'), 
 2, 'CHECK_OUT', CURRENT_DATE - INTERVAL '6 days' + TIME '19:00:00', 
 '退勤時刻を打ち忘れました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '09:00:00'),
-- employee3の修正依頼（却下）
((SELECT id FROM attendance WHERE user_id = 3 AND check_in::date = CURRENT_DATE - INTERVAL '4 days' ORDER BY id LIMIT 1), 
 3, 'CHECK_IN', CURRENT_DATE - INTERVAL '4 days' + TIME '09:20:00', 
 '出勤時刻を間違えて打刻しました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '11:00:00'),
-- employee1の修正依頼（却下）- 中抜け開始時刻
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '18 days'), 
 1, 'LEAVE_START', CURRENT_DATE - INTERVAL '18 days' + TIME '14:00:00', 
 '中抜け開始時刻を間違えて記録しました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '17 days' + TIME '10:00:00'),
-- employee2の修正依頼（却下）- 中抜け終了時刻
((SELECT id FROM attendance WHERE user_id = 2 AND check_in::date = CURRENT_DATE - INTERVAL '9 days'), 
 2, 'LEAVE_END', CURRENT_DATE - INTERVAL '9 days' + TIME '15:30:00', 
 '中抜け終了時刻を打ち忘れました。', 'REJECTED', 
 CURRENT_DATE - INTERVAL '8 days' + TIME '09:00:00');

-- 事実ベース評価ダミーデータ（各従業員の過去数ヶ月分）
-- employee1の評価データ（今月）
INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(1, DATE_TRUNC('month', CURRENT_DATE)::date, 
 2, -- 遅刻回数
 85.5, -- 申請遵守率（%）
 3, -- 打刻修正回数
 15, -- 連続勤務日数
 92.0, -- 残業申請の正確性（%）
 82.5, -- 総合スコア
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee1の評価データ（前月）
INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(1, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date, 
 1, -- 遅刻回数
 90.0, -- 申請遵守率（%）
 2, -- 打刻修正回数
 20, -- 連続勤務日数
 95.0, -- 残業申請の正確性（%）
 88.0, -- 総合スコア
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee1の評価データ（2ヶ月前）
INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(1, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '2 months')::date, 
 0, -- 遅刻回数
 100.0, -- 申請遵守率（%）
 1, -- 打刻修正回数
 22, -- 連続勤務日数
 98.0, -- 残業申請の正確性（%）
 92.0, -- 総合スコア
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee2の評価データ（今月）
INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(2, DATE_TRUNC('month', CURRENT_DATE)::date, 
 0, -- 遅刻回数
 100.0, -- 申請遵守率（%）
 0, -- 打刻修正回数
 10, -- 連続勤務日数
 100.0, -- 残業申請の正確性（%）
 94.0, -- 総合スコア
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee2の評価データ（前月）
INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(2, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date, 
 1, -- 遅刻回数
 80.0, -- 申請遵守率（%）
 2, -- 打刻修正回数
 12, -- 連続勤務日数
 88.0, -- 残業申請の正確性（%）
 80.0, -- 総合スコア
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee2の評価データ（2ヶ月前）
INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(2, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '2 months')::date, 
 3, -- 遅刻回数
 75.0, -- 申請遵守率（%）
 4, -- 打刻修正回数
 8, -- 連続勤務日数
 85.0, -- 残業申請の正確性（%）
 72.0, -- 総合スコア
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee3の評価データ（今月）
INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(3, DATE_TRUNC('month', CURRENT_DATE)::date, 
 1, -- 遅刻回数
 95.0, -- 申請遵守率（%）
 1, -- 打刻修正回数
 5, -- 連続勤務日数
 90.0, -- 残業申請の正確性（%）
 85.0, -- 総合スコア
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee3の評価データ（前月）
INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(3, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date, 
 2, -- 遅刻回数
 88.0, -- 申請遵守率（%）
 2, -- 打刻修正回数
 7, -- 連続勤務日数
 92.0, -- 残業申請の正確性（%）
 82.0, -- 総合スコア
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee3の評価データ（2ヶ月前）
INSERT INTO fact_based_evaluation (employee_id, year_month, late_count, application_compliance_rate, fix_request_count, consecutive_work_days, overtime_accuracy, total_score, created_at, updated_at) VALUES
(3, (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '2 months')::date, 
 0, -- 遅刻回数
 100.0, -- 申請遵守率（%）
 0, -- 打刻修正回数
 18, -- 連続勤務日数
 100.0, -- 残業申請の正確性（%）
 96.0, -- 総合スコア
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

