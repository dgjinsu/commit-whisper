package com.example.commitwhisper.dto.history;

import java.time.LocalDateTime;

public record GetCommitSummaryHistoryRes(
    Long id,
    Long userId,
    String userName,
    Long repoInfoId,
    String repoOwner,
    String repoName,
    String commitSha,
    String summary,
    LocalDateTime commitDate,
    LocalDateTime createdAt
) {

    public String htmlSummary() {
        if (summary == null || summary.isEmpty()) {
            return "";
        }

        String html = summary;

        // 제목 처리 (## 제목 -> <h3>제목</h3>)
        html = html.replaceAll("(?m)^## (.+)$", "<h3>$1</h3>");

        // 볼드 처리 (**텍스트** -> <strong>텍스트</strong>)
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");

        // 리스트 항목 처리 (- 항목 -> <li>항목</li>)
        html = html.replaceAll("(?m)^- (.+)$", "<li>$1</li>");

        // 연속된 리스트 항목들을 <ul>로 감싸기
        html = html.replaceAll("(?m)(<li>.*</li>\\n?)+", "<ul>$0</ul>");

        // 줄바꿈 처리
        html = html.replace("\n", "<br>");

        // 제목/리스트 뒤의 불필요한 <br> 제거
        html = html.replace("</h3><br>", "</h3>");
        html = html.replace("</ul><br>", "</ul>");
        html = html.replace("<br><br>", "<br>");

        return html;
    }
}

