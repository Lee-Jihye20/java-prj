-- 初期企業データ
INSERT INTO companies (name, company_code) VALUES
('Test Inc.', 'testinc');

-- 初期企業設定データ（Slack Webhook URL は管理画面または環境変数で設定）
INSERT INTO company_settings (company_id, admin_slack_webhook_url, attendance_slack_webhook_url) VALUES
(1, NULL, NULL);

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

-- 休憩記録データ（employee1の過去数日分）
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
-- 10日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '10 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '10 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '10 days' + TIME '13:00:00', 'FREE'),
-- 5日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '5 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '5 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '5 days' + TIME '13:00:00', 'FREE'),
-- 3日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '3 days' ORDER BY id LIMIT 1), 
 CURRENT_DATE - INTERVAL '3 days' + TIME '12:00:00', 
 CURRENT_DATE - INTERVAL '3 days' + TIME '13:00:00', 'FREE'),
-- 1日前の休憩
((SELECT id FROM attendance WHERE user_id = 1 AND check_in::date = CURRENT_DATE - INTERVAL '1 days' ORDER BY id LIMIT 1), 
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

-- 評価ダミーデータ（前月の週ごと）
-- 前月の最初の月曜日を計算（簡易版：前月1日の前の月曜日）
-- employee1の評価データ（4週分）
INSERT INTO weekly_evaluation (employee_id, evaluator_id, week_start_date, week_end_date, rating, comment, created_at, updated_at) VALUES
(1, 4, 
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7),
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 6,
 'S', '素晴らしい週でした！', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 7,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 13,
 'A', '良好な週でした。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 14,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 20,
 'B', '標準的な週でした。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 21,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 27,
 'A', '良い週でした。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee2の評価データ（4週分）
INSERT INTO weekly_evaluation (employee_id, evaluator_id, week_start_date, week_end_date, rating, comment, created_at, updated_at) VALUES
(2, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7),
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 6,
 'A', '良い週でした。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 7,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 13,
 'B', '標準的な週でした。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 14,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 20,
 'B', '標準的な週でした。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 21,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 27,
 'C', '改善の余地があります。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- employee3の評価データ（4週分）
INSERT INTO weekly_evaluation (employee_id, evaluator_id, week_start_date, week_end_date, rating, comment, created_at, updated_at) VALUES
(3, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7),
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 6,
 'B', '標準的な週でした。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 7,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 13,
 'B', '標準的な週でした。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 14,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 20,
 'C', '改善の余地があります。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 4,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 21,
 (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date - MOD((EXTRACT(DOW FROM (DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month')::date)::integer - 1 + 7) % 7, 7) + 27,
 'B', '標準的な週でした。', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
