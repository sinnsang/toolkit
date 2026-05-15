# SmsForwarder 프로젝트 컨텍스트

> Claude Code는 이 파일을 **가장 먼저** 읽고 작업을 시작합니다.
> 이후 작업 중 결정이 모호하면 이 문서로 돌아와 기준을 잡으세요.

---

## 1. 프로젝트 한 줄 요약

안드로이드 폰으로 수신되는 SMS 중 **특정 발신번호** 또는 **특정 문구를 포함**한 메시지를
**다른 번호로 자동 포워딩**하는 앱. 메시지 내용 수정(치환) 후 전송도 지원.

## 2. 사용자(앱 소유자)의 목표

- 1차: 본인 폰에 sideload 설치하여 사용 (테스트)
- 2차: 추후 Google Play Store 등록 가능성 있음 (유료 모델 고려)

## 3. 기술 스택 (확정)

| 항목 | 값 |
|---|---|
| 언어 | Kotlin |
| UI | Jetpack Compose |
| 빌드 | Gradle (Kotlin DSL, `.kts`) |
| Minimum SDK | API 26 (Android 8.0) |
| Target SDK | 최신 안정 버전 |
| 개발 환경 | macOS (Apple Silicon) + Android Studio |
| 패키지명 | `com.example.smsforwarder` (변경 시 사용자에게 확인) |

## 4. 포워딩 채널 (단계별)

**중요: 채널 처리 코드는 처음부터 인터페이스로 추상화할 것.**
새 채널을 추가할 때 기존 코드 수정이 최소화되어야 합니다.

```kotlin
interface ForwardChannel {
    suspend fun send(message: ForwardMessage): Result<Unit>
}
```

| 우선순위 | 채널 | 비고 |
|---|---|---|
| 1차 (현재) | **SMS 재발송** (`SmsManager.sendTextMessage`) | 통신사 요금 발생 |
| 2차 | 텔레그램 봇 | 무료, Bot Token + chat_id 필요 |
| 3차 | 라인 Messaging API | LINE Notify는 종료(2025) |
| **제외** | 카카오톡 | 개인 메시지 전송 API 없음. 알림톡은 사업자등록 필요. 구현 불가. |

## 5. 단계별 개발 로드맵

각 단계는 **검증 가능한 목표**를 가집니다. 단계가 끝나면 빌드 통과 + 검증 항목 확인 후 다음 단계로.

### ✅ 1단계: SMS 수신 → Logcat 출력
- **목표**: 수신된 SMS의 발신번호와 본문을 Logcat에 출력
- **검증**: 다른 폰에서 테스트 SMS 발송 시 `[SMS] from=..., body=...` 로그 확인
- **구현 요소**:
  - `RECEIVE_SMS`, `READ_SMS` 권한 (매니페스트 + 런타임)
  - `BroadcastReceiver` (`SmsReceiver.kt`)
  - 권한 요청 UI (Compose)

### 2단계: 필터링 (특정 번호 / 특정 문구)
- **목표**: 사용자가 정의한 규칙에 매칭된 SMS만 처리
- **검증**: 매칭된 SMS만 로그 출력, 매칭 안 된 건 무시
- **구현 요소**:
  - `FilterRule` 데이터 클래스 (번호 패턴, 문구 패턴, AND/OR 로직)
  - 매칭 엔진
  - 일단 하드코딩된 규칙으로 시작 (UI는 4단계)

### 3단계: SMS 재발송 (1차 포워딩 채널)
- **목표**: 필터 통과한 SMS를 지정된 다른 번호로 SMS 재발송
- **검증**: 매칭된 SMS가 목적지 번호로 도착함 (실기기 테스트)
- **구현 요소**:
  - `SEND_SMS` 권한 추가
  - `ForwardChannel` 인터페이스 + `SmsForwardChannel` 구현
  - 멀티파트 SMS 처리 (`divideMessage`)
  - **무한 루프 방지**: 자기가 보낸 SMS를 자기가 받아 다시 포워딩하면 안 됨

### 4단계: 규칙 관리 UI + 영구 저장
- **목표**: 앱 내에서 규칙 CRUD, 재부팅 후에도 유지
- **검증**: 앱에서 규칙 추가/수정/삭제 가능, 앱 재시작 후 유지됨
- **구현 요소**:
  - Room DB (또는 DataStore) - 규칙 영속화
  - Compose UI: 규칙 목록, 추가/편집 화면
  - ViewModel + StateFlow

### 5단계: Foreground Service + 부팅 시 자동 시작
- **목표**: 앱이 강제 종료되거나 재부팅 후에도 SMS 감시 유지
- **검증**: 앱 스와이프 종료 후 / 재부팅 후에도 SMS 포워딩 작동
- **구현 요소**:
  - `ForegroundService` + 상주 알림
  - `BOOT_COMPLETED` 리시버
  - `FOREGROUND_SERVICE` 권한
  - 제조사별 배터리 최적화 가이드(삼성, 샤오미 등) - 사용자 안내 화면

### 6단계: 메시지 내용 수정 (치환 규칙)
- **목표**: 원본 SMS를 변환 후 포워딩 (예: 특정 단어 마스킹, 접두사 추가)
- **검증**: 원본과 다른 변환된 메시지가 목적지에 도착
- **구현 요소**:
  - 치환 규칙 데이터 모델 (정규식 또는 단순 치환)
  - 규칙당 변환 파이프라인

### 7단계: 포워딩 이력 + 디버깅
- **목표**: 언제, 어떤 SMS가, 어디로, 성공/실패 기록
- **검증**: 이력 화면에서 최근 포워딩 내역 확인 가능
- **구현 요소**:
  - 이력 Room 테이블
  - 이력 목록 화면

### 8단계 이후: 텔레그램 채널 추가, Play Store 준비 등

## 6. 절대 지켜야 할 원칙 (Anti-patterns 방지)

사용자가 명시한 코딩 원칙:

1. **단순함 우선**: 요청된 것 외 기능 금지. 추측성 추상화 금지.
2. **외과적 변경**: 기존 코드 스타일·서식 유지. 관련 없는 리팩토링 금지.
3. **검증 가능한 목표**: 각 단계마다 명확한 성공 기준이 있음. 통과 못하면 다음 단계로 가지 말 것.
4. **불명확하면 멈추고 질문**: 가정하지 말 것. 여러 해석 가능하면 사용자에게 제시.

## 7. 안드로이드 특화 주의사항

### 권한 관련
- `RECEIVE_SMS`, `READ_SMS`, `SEND_SMS`는 **런타임 권한** (사용자 명시 허용 필요)
- Android 12+ 부터 receiver에 `exported` 명시 의무
- SMS 권한은 Play Store 심사에서 **고위험 권한**으로 분류 → 정책 위반 소지 있는 구현 금지

### 백그라운드 제한
- Android 8.0+ 부터 백그라운드 서비스 강하게 제한
- 장기 실행은 **Foreground Service + 상주 알림** 필수
- 제조사별(삼성 One UI, 샤오미 MIUI 등) 배터리 최적화 화이트리스트 사용자가 직접 등록해야 함

### SMS 무한 루프 방지
포워딩 앱의 가장 흔한 버그:
- 앱이 보낸 SMS를 자기가 다시 수신 → 매칭 → 또 발송 → 무한 루프
- 대책:
  - 자기가 발송한 메시지에 식별자(예: 본문 끝에 zero-width space)를 추가하거나
  - 최근 발송 메시지를 캐시에 저장, 동일 본문 수신 시 무시
  - 발신번호가 본인 번호인 경우 스킵 (단, 본인 번호 알기 어려운 케이스 있음)

### Play Store 정책 (8단계 이후 대비)
- SMS 권한 사용 시 **Permissions Declaration Form** 제출 필수
- 카테고리: "SMS-based device backup/transfer/sync" 사용
- 필요 항목: 개인정보처리방침 URL, 권한 사용 목적 사전 안내 화면, 데모 영상
- 정책 위반 즉시 정지 사유:
  - 아이콘 숨김, 은밀한 동작
  - 다른 앱 알림 가로채기
  - 사용자 인지 없는 데이터 수집/전송

## 8. 법적 전제

이 앱은 **본인 폰의 본인 SMS를 본인이 관리하는 다른 번호/채널로 전달**하는 용도임.
다른 사람 SMS를 동의 없이 감시하는 용도가 아님.

## 9. 디렉토리 구조 (제안 — 단계 진행하며 진화)

```
app/src/main/
├── AndroidManifest.xml
└── java/com/example/smsforwarder/
    ├── MainActivity.kt
    ├── receiver/
    │   ├── SmsReceiver.kt
    │   └── BootReceiver.kt          # 5단계
    ├── service/
    │   └── ForwarderService.kt      # 5단계
    ├── channel/
    │   ├── ForwardChannel.kt        # 3단계
    │   ├── SmsForwardChannel.kt     # 3단계
    │   └── TelegramChannel.kt       # 8단계
    ├── filter/
    │   ├── FilterRule.kt            # 2단계
    │   └── FilterEngine.kt          # 2단계
    ├── data/
    │   ├── RuleDao.kt               # 4단계
    │   ├── RuleEntity.kt            # 4단계
    │   └── AppDatabase.kt           # 4단계
    └── ui/
        ├── MainScreen.kt
        ├── RuleListScreen.kt        # 4단계
        └── RuleEditScreen.kt        # 4단계
```

## 10. Claude Code에게 — 작업 방식

1. **사용자의 다음 지시를 기다린 후 시작.** 자체적으로 1단계부터 막 시작하지 말 것.
2. **단계 단위로 작업, 단계 사이에 사용자 확인.** 한 단계 완료 후 빌드 통과 + 사용자에게 "실기기에서 검증해보시고 결과 알려주세요" 요청.
3. **빌드 자동 검증**: 코드 작성 후 `./gradlew assembleDebug`로 빌드 통과 확인. 실패 시 자체 수정.
4. **사용자가 해야 하는 일은 명확히 안내**: 권한 허용, USB 연결, 실기기 SMS 테스트 등은 사람만 가능.
5. **모르거나 모호하면 멈추고 질문.** 추측으로 진행하지 말 것.
6. **이 문서를 수정해야 할 결정이 생기면 사용자에게 먼저 확인** 후 업데이트.

---

**현재 상태**: 1~7단계 완료. 8단계(텔레그램 외 채널 추가, Play Store 준비 등) 대기 중.
