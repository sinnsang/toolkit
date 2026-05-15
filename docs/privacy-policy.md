# Privacy Policy — Noowar SendMagic

*Last updated: 2026-05-15*

## Overview

Noowar SendMagic ("the App") is a personal utility application for Android. The App reads incoming SMS messages on the user's device and forwards them to a destination specified by the user (another phone number or a Telegram bot that the user controls).

All forwarding destinations and rules are configured solely by the user. The developer has no access to any message content or user data.

---

## What data is accessed

| Data | Purpose |
|---|---|
| Incoming SMS messages (sender number, body) | Forwarding according to user-defined rules |
| Contacts | Selecting recipient numbers from the address book |
| Notification content (Samsung Messages app) | Detecting SMS on Samsung devices where direct broadcast is restricted |

---

## What data is NOT collected

- The App does **not** transmit any data to the developer's servers.
- The App does **not** collect analytics, crash reports, or usage statistics.
- The App does **not** share any data with third parties, except as explicitly configured by the user.

---

## Where data goes

Forwarding destinations are entirely under the user's control:

- **SMS forwarding**: Messages are sent via the device's own SMS function to a phone number entered by the user.
- **Telegram forwarding**: Messages are sent to Telegram's servers. See the dedicated section below.

---

## Telegram forwarding — detailed disclosure

When the user configures a Telegram forwarding rule, the App transmits the following data to Telegram's Bot API (`https://api.telegram.org`):

| Data transmitted | Details |
|---|---|
| SMS sender number | Included in the forwarded message text |
| SMS message body | Included in the forwarded message text |
| Bot token | Used to authenticate with the Telegram Bot API (stored locally, never sent to the developer) |
| Chat ID | Identifies the destination chat (stored locally, entered by the user) |

**Important notes:**
- Transmission is performed over HTTPS (TLS-encrypted).
- The developer has **no access** to the user's bot, chat, or any messages sent through it.
- The bot token and chat ID are stored only on the user's device and are never shared with the developer.
- Telegram's servers are operated by Telegram FZ-LLC. Once data reaches Telegram's servers, it is subject to [Telegram's Privacy Policy](https://telegram.org/privacy).
- The user is responsible for the security of their own bot token and chat ID.

---

## Data stored on device

The App stores the following data locally on the device only:

| Data | Storage |
|---|---|
| Forwarding rules (sender filter, keyword, destination) | Room database (app-private storage) |
| Telegram bot token | SharedPreferences (app-private, not accessible to other apps) |
| Forwarding history log (last 200 records) | Room database (app-private storage) |

No data is uploaded to any cloud service or developer server.

---

## Permissions

| Permission | Reason |
|---|---|
| `RECEIVE_SMS` / `READ_SMS` | Detect and read incoming SMS for forwarding |
| `SEND_SMS` | Forward SMS to another phone number as configured by the user |
| `READ_CONTACTS` | Let user pick recipient from address book |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Detect SMS on Samsung One UI devices |
| `FOREGROUND_SERVICE` | Keep the forwarding service running in the background |
| `RECEIVE_BOOT_COMPLETED` | Auto-start after device reboot |
| `INTERNET` | Send messages via Telegram Bot API |
| `POST_NOTIFICATIONS` | Show persistent foreground service notification |

---

## Children's privacy

The App is not directed at children under 13 and does not knowingly collect data from children.

---

## Changes to this policy

If this policy changes, the updated version will be published at this URL with a new "Last updated" date.

---

## Contact

If you have questions about this policy, contact: **sinnsang@naver.com**

---

---

# 개인정보처리방침 — Noowar SendMagic

*최종 수정일: 2026-05-15*

## 개요

Noowar SendMagic(이하 "앱")은 Android용 개인 유틸리티 앱입니다. 앱은 사용자 기기에 수신된 SMS를 사용자가 지정한 수신처(다른 전화번호 또는 사용자 본인이 관리하는 텔레그램 봇)로 전달합니다.

포워딩 대상과 규칙은 전적으로 사용자가 설정합니다. 개발자는 메시지 내용이나 사용자 데이터에 접근할 수 없습니다.

---

## 접근하는 데이터

| 데이터 | 목적 |
|---|---|
| 수신 SMS (발신번호, 본문) | 사용자 정의 규칙에 따른 포워딩 |
| 연락처 | 주소록에서 수신번호 선택 |
| 알림 내용 (삼성 메시지 앱) | 삼성 기기에서 SMS 감지 |

---

## 수집하지 않는 데이터

- 앱은 개발자 서버로 어떠한 데이터도 **전송하지 않습니다**.
- 앱은 분석, 충돌 보고, 사용 통계를 **수집하지 않습니다**.
- 사용자가 명시적으로 설정한 경우를 제외하고 제3자와 데이터를 **공유하지 않습니다**.

---

## 데이터 전송 대상

포워딩 대상은 전적으로 사용자가 결정합니다:

- **SMS 포워딩**: 사용자가 입력한 전화번호로 기기의 SMS 기능을 통해 발송됩니다.
- **텔레그램 포워딩**: 텔레그램 서버로 전송됩니다. 아래 전용 항목을 참조하세요.

---

## 텔레그램 포워딩 — 상세 고지

사용자가 텔레그램 포워딩 규칙을 설정한 경우, 앱은 텔레그램 Bot API(`https://api.telegram.org`)로 다음 데이터를 전송합니다:

| 전송 데이터 | 내용 |
|---|---|
| SMS 발신번호 | 전달 메시지 본문에 포함 |
| SMS 메시지 본문 | 전달 메시지 본문에 포함 |
| 봇 토큰 | 텔레그램 Bot API 인증에 사용 (기기 내 저장, 개발자에게 전송되지 않음) |
| Chat ID | 전송 대상 채팅 식별 (기기 내 저장, 사용자가 직접 입력) |

**중요 사항:**
- 전송은 HTTPS(TLS 암호화)를 통해 이루어집니다.
- 개발자는 사용자의 봇, 채팅, 전송된 메시지에 **접근할 수 없습니다**.
- 봇 토큰과 Chat ID는 사용자 기기에만 저장되며 개발자와 공유되지 않습니다.
- 텔레그램 서버는 Telegram FZ-LLC가 운영합니다. 데이터가 텔레그램 서버에 도달한 이후에는 [텔레그램 개인정보처리방침](https://telegram.org/privacy)이 적용됩니다.
- 봇 토큰과 Chat ID의 보안 관리는 사용자 본인의 책임입니다.

---

## 기기 내 저장 데이터

앱은 기기 내에만 다음 데이터를 저장합니다:

| 데이터 | 저장 위치 |
|---|---|
| 포워딩 규칙 (발신번호 필터, 키워드, 수신처) | Room 데이터베이스 (앱 전용 저장소) |
| 텔레그램 봇 토큰 | SharedPreferences (앱 전용, 타 앱 접근 불가) |
| 전송 이력 (최근 200건) | Room 데이터베이스 (앱 전용 저장소) |

어떠한 데이터도 클라우드 서비스나 개발자 서버에 업로드되지 않습니다.

---

## 권한

| 권한 | 사유 |
|---|---|
| `RECEIVE_SMS` / `READ_SMS` | 포워딩을 위한 수신 SMS 감지 및 읽기 |
| `SEND_SMS` | 사용자가 설정한 번호로 SMS 전달 |
| `READ_CONTACTS` | 주소록에서 수신번호 선택 |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 삼성 One UI 기기에서 SMS 감지 |
| `FOREGROUND_SERVICE` | 백그라운드에서 포워딩 서비스 유지 |
| `RECEIVE_BOOT_COMPLETED` | 재부팅 후 자동 시작 |
| `INTERNET` | 텔레그램 Bot API로 메시지 전송 |
| `POST_NOTIFICATIONS` | 포그라운드 서비스 알림 표시 |

---

## 아동 개인정보

앱은 만 13세 미만 아동을 대상으로 하지 않으며, 아동의 데이터를 의도적으로 수집하지 않습니다.

---

## 방침 변경

방침이 변경될 경우 이 URL에 새로운 "최종 수정일"과 함께 업데이트됩니다.

---

## 문의

개인정보처리방침 관련 문의: **sinnsang@naver.com**
