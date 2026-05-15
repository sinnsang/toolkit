package com.noowar.smsforwarder.filter

// 하드코딩 규칙 — 4단계에서 DB 기반 UI로 교체됩니다
// 검증 방법:
//   PASS: 규칙에 매칭되는 SMS 발송 → Logcat에 [SMS] from=... 출력
//   SKIP: 매칭 안 되는 SMS 발송 → Logcat에 [SMS] skip from=... 출력
object FilterEngine {

    val RULES: List<FilterRule> = listOf(

        // 예시 A — 발신번호에 "010" 포함 (한국 휴대폰 번호 전체 통과)
        FilterRule(senderPattern = "010", matchMode = FilterRule.MatchMode.OR),

        // 예시 B — 본문에 특정 문구 포함 (주석 해제해서 테스트)
        // FilterRule(bodyPattern = "인증", matchMode = FilterRule.MatchMode.OR),

        // 예시 C — 발신번호 AND 본문 모두 일치해야 통과
        // FilterRule(
        //     senderPattern = "01012345678",
        //     bodyPattern = "인증",
        //     matchMode = FilterRule.MatchMode.AND
        // ),
    )

    fun matches(sender: String, body: String): Boolean =
        RULES.filter { it.isEnabled }.any { it.matches(sender, body) }
}
