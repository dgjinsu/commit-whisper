package com.example.commitwhisper.service;

import com.example.commitwhisper.dto.github.GitHubCommitDetailRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private final ObjectMapper objectMapper;

    @Value("${python.script.path}")
    private String pythonScriptPath;

    @Value("${python.executable}")
    private String pythonExecutable;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.mock.enabled}")
    private boolean mockEnabled;

    public String summarizeCommit(GitHubCommitDetailRes commitDetail) {
        // Mock 모드일 경우 실제 API 호출 없이 고정된 응답 반환
        if (mockEnabled) {
            log.info("OpenAI API 호출 목킹 모드 - 고정된 응답 반환");
            return getMockedSummary();
        }

        try {
            // commitDetail을 JSON으로 변환 (서로게이트 문자 처리)
            String jsonInput = objectMapper.writeValueAsString(commitDetail);
            
            // 서로게이트 문자를 제거하여 UTF-8 인코딩 오류 방지
            jsonInput = jsonInput.replaceAll("[\uD800-\uDFFF]", "");
            
            log.debug("Python 스크립트에 전달할 JSON 데이터 크기: {} bytes", jsonInput.length());

            // Python 스크립트 실행
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonExecutable,
                    pythonScriptPath
            );

            // 작업 디렉토리 설정 (프로젝트 루트)
            processBuilder.directory(Paths.get(".").toAbsolutePath().toFile());

            // OpenAI API 키를 환경변수로 전달
            if (openaiApiKey != null && !openaiApiKey.isEmpty()) {
                processBuilder.environment().put("OPENAI_API_KEY", openaiApiKey);
                log.debug("OpenAI API 키가 환경변수로 설정되었습니다.");
            } else {
                log.warn("OpenAI API 키가 설정되지 않았습니다. 환경변수 OPENAI_API_KEY를 확인하세요.");
            }

            Process process = processBuilder.start();

            // stdin으로 JSON 데이터 전달
            try (var writer = process.getOutputStream()) {
                writer.write(jsonInput.getBytes(StandardCharsets.UTF_8));
                writer.flush();
            }

            // stdout에서 결과 읽기
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                 BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("Python 스크립트 실행 실패. Exit code: {}, Error: {}", 
                        exitCode, errorOutput.toString());
                throw new RuntimeException("Python 스크립트 실행 실패: " + errorOutput.toString());
            }

            String result = output.toString().trim();
            log.info("LLM 요약 완료. 결과 길이: {} characters", result.length());
            return result;

        } catch (IOException e) {
            log.error("Python 스크립트 실행 중 IO 오류 발생", e);
            throw new RuntimeException("Python 스크립트 실행 중 오류 발생", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Python 스크립트 실행 중 인터럽트 발생", e);
            throw new RuntimeException("Python 스크립트 실행 중 인터럽트 발생", e);
        } catch (Exception e) {
            log.error("커밋 요약 생성 실패", e);
            throw new RuntimeException("커밋 요약 생성 실패", e);
        }
    }

    private String getMockedSummary() {
        return """
                ## 날짜
                
                2025-11-22 04:51:35
                
                
                ## 변경된 파일 목록
                
                - [수정] src/main/java/com/example/commitwhisper/client/GitHubClient.java
                
                - [수정] src/main/java/com/example/commitwhisper/dto/GetRepoInfoRes.java
                
                - [생성] src/main/java/com/example/commitwhisper/dto/GitHubCommitDetailRes.java
                
                - [수정] src/main/java/com/example/commitwhisper/dto/GitHubCommitRes.java
                
                - [수정] src/main/java/com/example/commitwhisper/entity/RepoInfo.java
                
                - [수정] src/main/java/com/example/commitwhisper/service/CommitCheckService.java
                
                - [수정] src/main/java/com/example/commitwhisper/service/RepoInfoService.java
                
                - [수정] src/main/resources/application.yml
                
                
                ## 변경 내용 요약
                
                
                1. **추가된 기능 또는 변경사항**
                
                   - 새로운 커밋 상세 정보 조회 기능을 추가했습니다. whisper 커밋의 변경 파일 목록과 패치를 한꺼번에 확인할 수 있어 추적과 분석이 쉬워졌습니다.
                
                   - 저장소별 whisper 커밋 시간을 기록하고 관리하는 로직이 추가되었습니다. 이를 통해 이미 처리한 커밋을 재처리하지 않도록 안전하게 관리합니다.
                
                   - 응답에서 불필요한 작성자 이름·이메일 정보를 제거해 응답 표기가 간소화되었습니다.
                
                   - 개발 편의를 위한 데이터베이스 초기화 설정이 변경되었습니다. startup 시 DB 스키마를 새로 생성(create)하도록 설정되어 있어, 로컬 개발 환경에서의 재구성이 쉬워졌습니다.
                
                
                
                2. **주요 변화점**
                
                   - whisper 커밋 감지 흐름이 시스템에 본격적으로 통합되어, 새로 등장하는 whisper 커밋을 자동으로 식별하고 상세 정보를 수집합니다.
                
                   - RepoInfo에 lastWhisperCommitTime 필드가 추가되어 저장소의 최근 처리 시점을 기록합니다. 이로써 중복 처리 방지와 시점 관리가 가능해졌습니다.
                
                   - 커밋 상세 정보 조회를 위한 새로운 데이터 구조가 도입되어, 파일별 변경 내용과 패치를 구조적으로 다룰 수 있습니다.
                
                   - 시스템 구성(Product/환경) 측면에서 데이터베이스 초기화 방식 변경으로 초기화 영향이 커지므로 배포 시 주의가 필요합니다.
                
                
                
                3. **팀원이 알아야 할 사항**
                
                   - whisper 커밋 감지 로직이 새로 도입되었으므로 저장소별 설정에 따라 동작이 달라질 수 있습니다. 특히 lastWhisperCommitTime이 정확히 업데이트되어야 다음 감지가 정상적으로 이뤄집니다.
                
                   - 데이터베이스 초기화 방식이 변경되어 로컬 개발 환경에서 매 실행 시 DB가 재생성될 수 있습니다. 이를 production 환경에 적용할 경우 주의가 필요합니다(필요 시 ddl-auto를 업데이트로 변경 권장).
                
                   - 작성자 이름과 이메일 필드의 노출이 제거되었으므로, 외부 시스템이나 UI에서 작성자 정보를 기대하는 부분은 업데이트가 필요할 수 있습니다.
                
                   - 커밋 상세 정보가 수집되면서 파일 목록과 패치 내용이 로깅되므로 로그 관리 정책을 확인하고 민감 정보가 로그에 남지 않도록 주의하시기 바랍니다.
                """;
    }
}
