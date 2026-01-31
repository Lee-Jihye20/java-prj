-- 既存のテーブルを削除
DROP TABLE IF EXISTS fact_based_evaluation CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS role_permission CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS admin_action_log CASCADE;
DROP TABLE IF EXISTS anomaly_approval CASCADE;
DROP TABLE IF EXISTS fix_request CASCADE;
DROP TABLE IF EXISTS leave_record CASCADE;
DROP TABLE IF EXISTS break_record CASCADE;
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS company_settings CASCADE;
DROP TABLE IF EXISTS companies CASCADE;

-- 企業テーブル
CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    company_code VARCHAR(50) UNIQUE NOT NULL
);

-- 企業設定テーブル
CREATE TABLE company_settings (
    id SERIAL PRIMARY KEY,
    company_id INTEGER UNIQUE NOT NULL,
    admin_slack_webhook_url VARCHAR(255),
    attendance_slack_webhook_url VARCHAR(255),
    slack_notification_enabled BOOLEAN DEFAULT false,
    log_slack_webhook_url VARCHAR(255),
    alert_slack_webhook_url VARCHAR(255),
    break_count_limit INTEGER DEFAULT 1,
    break_input_mode VARCHAR(20) DEFAULT 'FREE',
    auto_calculate_break_time BOOLEAN DEFAULT true,
    lunch_break_start_time TIME,
    lunch_break_end_time TIME,
    leave_default_type VARCHAR(20) DEFAULT 'DEDUCTION',
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- 権限テーブル
CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50) NOT NULL
);

-- ロールテーブル
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    company_id INTEGER NOT NULL,
    is_system_role BOOLEAN DEFAULT false,
    UNIQUE (name, company_id),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- ロールと権限の関連テーブル
CREATE TABLE role_permission (
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ユーザーテーブル
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('EMPLOYEE', 'ADMIN')), -- 後方互換性のため残す
    company_id INTEGER NOT NULL,
    slack_webhook_url VARCHAR(255),
    slack_user_id VARCHAR(50),
    work_type VARCHAR(20) DEFAULT 'FULLTIME' CHECK (work_type IN ('FULLTIME', 'FLEX')),
    start_time TIME DEFAULT '09:00:00',
    core_time_start TIME,
    core_time_end TIME,
    UNIQUE (username, company_id),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- ユーザーとロールの関連テーブル
CREATE TABLE user_role (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);


-- 勤怠テーブル
CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    check_in TIMESTAMP,
    check_out TIMESTAMP,
    break_start TIMESTAMP,
    break_end TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 休憩記録テーブル
CREATE TABLE break_record (
    id SERIAL PRIMARY KEY,
    attendance_id INTEGER NOT NULL,
    break_start TIMESTAMP NOT NULL,
    break_end TIMESTAMP,
    break_type VARCHAR(20) DEFAULT 'FREE',
    FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE
);

-- 中抜け記録テーブル
CREATE TABLE leave_record (
    id SERIAL PRIMARY KEY,
    attendance_id INTEGER NOT NULL,
    leave_start TIMESTAMP NOT NULL,
    leave_end TIMESTAMP,
    leave_type VARCHAR(20) DEFAULT 'DEDUCTION',
    FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE
);

-- 修正依頼テーブル
CREATE TABLE fix_request (
    id SERIAL PRIMARY KEY,
    attendance_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    request_type VARCHAR(20) NOT NULL CHECK (request_type IN ('CHECK_IN', 'CHECK_OUT', 'BREAK_START', 'BREAK_END', 'LEAVE_START', 'LEAVE_END', 'LEAVE_TYPE', 'OVERTIME_APPLICATION', 'CHECK_IN_AND_OUT', 'BREAK_START_AND_END')),
    new_value TIMESTAMP,
    new_value_2 TIMESTAMP,
    leave_record_id INTEGER,
    new_leave_type VARCHAR(20),
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_by_user_id INTEGER,
    FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (leave_record_id) REFERENCES leave_record(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 異常検知承認テーブル（理由・承認・補正を記録）
CREATE TABLE anomaly_approval (
    id SERIAL PRIMARY KEY,
    attendance_id INTEGER NOT NULL,
    anomaly_type VARCHAR(50) NOT NULL,
    reason TEXT,
    approved BOOLEAN NOT NULL DEFAULT false,
    approved_by INTEGER,
    approved_at TIMESTAMP,
    adjustment_hours DECIMAL(10, 2),
    adjustment_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 管理者操作履歴テーブル（管理者アカウントが行ったアクションの履歴）
CREATE TABLE admin_action_log (
    id SERIAL PRIMARY KEY,
    admin_id INTEGER NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 事実ベース評価テーブル（月別の評価スコア）
CREATE TABLE fact_based_evaluation (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL,
    year_month DATE NOT NULL, -- 年月（月初日）
    late_count INTEGER NOT NULL DEFAULT 0, -- 遅刻回数
    application_compliance_rate DECIMAL(5,2) NOT NULL DEFAULT 0.0, -- 申請遵守率（0-100）
    fix_request_count INTEGER NOT NULL DEFAULT 0, -- 打刻修正回数
    consecutive_work_days INTEGER NOT NULL DEFAULT 0, -- 連続勤務日数
    overtime_accuracy DECIMAL(5,2) NOT NULL DEFAULT 0.0, -- 残業申請の正確性（0-100）
    total_score DECIMAL(5,2) NOT NULL DEFAULT 0.0, -- 総合スコア（0-100）
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, year_month),
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE
);

