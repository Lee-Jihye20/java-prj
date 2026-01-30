# 勤怠管理システム

Spring Boot 3.2.0を使用した勤怠管理システムです。

## 技術スタック

- **フレームワーク**: Spring Boot 3.2.0
- **ビルドツール**: Maven
- **データベース**: H2 (開発用) / PostgreSQL (本番用)
- **ORM**: Spring Data JPA
- **セキュリティ**: Spring Security
- **テンプレートエンジン**: Thymeleaf
- **Java**: 17

## 機能

### 従業員機能
- 出勤/退勤打刻
- 休憩開始/終了打刻
- 勤怠履歴閲覧
- 勤怠修正依頼の作成
- 修正依頼の状態確認

### 管理者機能
- 全従業員の勤怠記録閲覧
- 修正依頼の承認/却下
- 異常検知
  - 残業時間検知(8時間超過)
  - 打刻漏れ検知(退勤未打刻)

## プロジェクト構造

```
kintai/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/kintai/
│   │   │       ├── KintaiApplication.java
│   │   │       ├── config/
│   │   │       │   └── SecurityConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── AdminController.java
│   │   │       │   ├── AttendanceController.java
│   │   │       │   ├── DashboardController.java
│   │   │       │   └── LoginController.java
│   │   │       ├── entity/
│   │   │       │   ├── Attendance.java
│   │   │       │   ├── FixRequest.java
│   │   │       │   └── User.java
│   │   │       ├── repository/
│   │   │       │   ├── AttendanceRepository.java
│   │   │       │   ├── FixRequestRepository.java
│   │   │       │   └── UserRepository.java
│   │   │       └── service/
│   │   │           ├── AttendanceService.java
│   │   │           └── FixRequestService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql
│   │       ├── data.sql
│   │       ├── static/
│   │       │   └── css/
│   │       │       └── style.css
│   │       └── templates/
│   │           ├── login.html
│   │           ├── employee_dashboard.html
│   │           ├── admin_dashboard.html
│   │           ├── attendance_history.html
│   │           ├── fix_request_form.html
│   │           ├── fix_request_list.html
│   │           └── anomaly_detection.html
│   └── test/
│       └── java/
└── README.md
```

## セットアップ

### 必要な環境
- Java 17以上
- Maven 3.6以上

### 起動方法

1. プロジェクトをクローン/ダウンロード

2. プロジェクトディレクトリに移動
```bash
cd kintai
```

3. Mavenでビルド
```bash
mvn clean install
```

4. アプリケーション起動
```bash
mvn spring-boot:run
```

5. ブラウザでアクセス
```
http://localhost:8080
```

## テストアカウント

### 従業員アカウント
- ユーザー名: `employee1`
- パスワード: `password`

### 管理者アカウント
- ユーザー名: `admin1`
- パスワード: `adminpass`

## データベース

### H2コンソール(開発用)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:kintaidb`
- ユーザー名: `sa`
- パスワード: (空欄)

### PostgreSQLへの切り替え

`application.properties`を編集:

```properties
# H2の設定をコメントアウト
# spring.datasource.url=jdbc:h2:mem:kintaidb
# spring.datasource.driverClassName=org.h2.Driver

# PostgreSQLの設定を有効化
spring.datasource.url=jdbc:postgresql://localhost:5432/kintaidb
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## API エンドポイント

### 認証
- `GET /login` - ログイン画面
- `POST /login` - ログイン処理
- `POST /logout` - ログアウト

### 従業員
- `GET /employee/dashboard` - 従業員ダッシュボード
- `POST /attendance/check-in` - 出勤打刻
- `POST /attendance/check-out` - 退勤打刻
- `POST /attendance/break-start` - 休憩開始
- `POST /attendance/break-end` - 休憩終了
- `GET /attendance/history` - 勤怠履歴
- `GET /attendance/fix-request/{id}` - 修正依頼フォーム
- `POST /attendance/fix-request` - 修正依頼送信
- `GET /attendance/fix-request-list` - 修正依頼一覧

### 管理者
- `GET /admin/dashboard` - 管理者ダッシュボード
- `GET /admin/fix-requests` - 修正依頼管理
- `POST /admin/fix-request/approve/{id}` - 修正依頼承認
- `POST /admin/fix-request/reject/{id}` - 修正依頼却下
- `GET /admin/anomaly-detection` - 異常検知

## セキュリティ設定

現在の設定では、パスワードは平文で保存されています(`NoOpPasswordEncoder`使用)。

**本番環境では必ず`BCryptPasswordEncoder`を使用してください:**

`SecurityConfig.java`を編集:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

初期データの変更も必要:
```sql
-- BCryptでハッシュ化したパスワードを使用
INSERT INTO users (username, password, role) VALUES
('employee1', '$2a$10$...', 'EMPLOYEE'),
('admin1', '$2a$10$...', 'ADMIN');
```

## ライセンス

このプロジェクトは教育目的で作成されました。
