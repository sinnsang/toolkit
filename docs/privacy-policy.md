# Privacy Policy — Noowar Toolkit

*Last updated: 2026-05-14*

## Overview

Noowar Toolkit ("the App") is a personal utility application for Android. The App's SMS forwarding feature reads incoming SMS messages on the user's device and forwards them to a destination specified by the user (another phone number or a Telegram bot).

---

## What data is accessed

| Data | Purpose |
|---|---|
| Incoming SMS messages (sender, body) | Forwarding according to user-defined rules |
| Contacts | Selecting recipient numbers from the address book |
| Notification content (Samsung Messages app) | Detecting SMS on Samsung devices where direct broadcast is restricted |

---

## What data is NOT collected

- The App does **not** transmit any data to the developer's servers.
- The App does **not** collect analytics, crash reports, or usage statistics.
- The App does **not** share any data with third parties, except as explicitly configured by the user (e.g., forwarding to a Telegram bot the user controls).

---

## Where data goes

Forwarding destinations are entirely under the user's control:

- **SMS forwarding**: Messages are sent via the device's own SMS function to a phone number entered by the user.
- **Telegram forwarding**: Messages are sent to a Telegram bot and chat ID entered by the user, using Telegram's official Bot API (`api.telegram.org`). The developer has no access to this bot or chat.

---

## Data stored on device

The App stores the following data locally on the device only:

- Forwarding rules (sender filter, keyword filter, destination)
- Bot token entered by the user (stored in SharedPreferences, not accessible to other apps)
- Forwarding history log (last 200 records)

---

## Permissions

| Permission | Reason |
|---|---|
| `RECEIVE_SMS` / `READ_SMS` | Detect and read incoming SMS |
| `SEND_SMS` | Forward SMS to another phone number |
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

If you have questions about this policy, contact: **sinn@hubilon.com**

---

# 개인정보처리방침 — Noowar Toolkit

*최종 수정일: 2026-05-14*

## 개요

Noowar Toolkit(이하 "앱")은 Android용 개인 유틸리티 앱입니다. SMS 포워딩 기능은 사용자 기기에 수신된 SMS를 사용자가 지정한 수신처(다른 전화번호 또는 텔레그램 봇)로 전달합니다.

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
- 사용자가 명시적으로 설정한 경우(예: 사용자 본인이 관리하는 텔레그램 봇으로 전달)를 제외하고 제3자와 데이터를 **공유하지 않습니다**.

---

## 데이터 전송 대상

포워딩 대상은 전적으로 사용자가 결정합니다:

- **SMS 포워딩**: 사용자가 입력한 전화번호로 기기의 SMS 기능을 통해 발송됩니다.
- **텔레그램 포워딩**: 사용자가 입력한 봇 토큰과 Chat ID를 사용해 텔레그램 공식 Bot API(`api.telegram.org`)로 전송됩니다. 개발자는 해당 봇이나 채팅에 접근할 수 없습니다.

---

## 기기 내 저장 데이터

앱은 기기 내에만 다음 데이터를 저장합니다:

- 포워딩 규칙 (발신번호 필터, 키워드, 수신처)
- 사용자가 입력한 봇 토큰 (SharedPreferences에 저장, 타 앱 접근 불가)
- 전송 이력 (최근 200건)

---

## 권한

| 권한 | 사유 |
|---|---|
| `RECEIVE_SMS` / `READ_SMS` | 수신 SMS 감지 및 읽기 |
| `SEND_SMS` | 다른 번호로 SMS 전달 |
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

개인정보처리방침 관련 문의: **sinn@hubilon.com**
