package com.example.commitwhisper.service;

import com.example.commitwhisper.dto.github.GitHubCommitDetailRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
            2025-11-22 16:45:25

            ## 변경된 파일 목록
            - [생성] src/main/java/com/example/commitwhisper/security/CustomOAuth2UserService.java
            - [생성] src/main/java/com/example/commitwhisper/security/OAuth2AuthenticationSuccessHandler.java
            - [수정] build.gradle
            - [수정] src/main/java/com/example/commitwhisper/config/SecurityConfig.java
            - [수정] src/main/java/com/example/commitwhisper/controller/UserController.java
            - [수정] src/main/java/com/example/commitwhisper/controller/api/v1/ApiUserController.java
            - [수정] src/main/java/com/example/commitwhisper/entity/User.java
            - [수정] src/main/java/com/example/commitwhisper/repository/UserRepository.java
            - [수정] src/main/java/com/example/commitwhisper/security/UserPrincipal.java
            - [수정] src/main/resources/application.yml
            - [수정] src/main/resources/static/css/login.css
            - [수정] src/main/resources/templates/login.html

            ## 변경 내용 요약

            1. 추가된 기능 또는 변경사항
            - OAuth2.0 기반 소셜 로그인 기능 도입: Google 계정으로의 로그인이 가능해져 사용자의 로그인 흐름이 다양해졌습니다. 이로써 기존 로그인 방식과 함께 외부 인증을 활용할 수 있어 편의성과 보안성이 향상됩니다.
            - 보안 흐름 구성과 인증 처리의 모듈화: OAuth2 로그인 흐름을 전문 클래스로 분리했고, 로그인 성공/실패 시의 동작을 명확히 제어합니다. 실패 시 로그인 페이지로 리다이렉트하는 안정적인 UX를 제공합니다.
            - 사용자 데이터 관리 확장: OAuth2 인증을 통해 얻은 정보를 바탕으로 로컬 사용자 기록을 생성하고 연결하는 로직이 추가되었습니다. 이를 통해 외부 인증과 내부 사용자 계정의 매핑이 일관되게 이뤄집니다.
            - 데이터 모델 및 리포지토리 보강: OAuth2 공급자 정보(provider, providerId, email 등)를 저장하고, 공급자 기반으로 사용자를 조회하는 기능이 추가되어 다중 인증 경로를 안정적으로 지원합니다.
            - UI/UX 개선 및 로그인 편의성: 로그인 화면에 Google 로그인 버튼을 추가하고, 시각적으로 깔끔하게 구분되는 OAuth 로그인 옵션을 제공해 사용성이 향상됩니다.
            - 의존성 및 설정 업데이트: OAuth2 클라이언트 의존성을 프로젝트에 추가하고, 애플리케이션 설정에 Google 로그인 정보를 등록하여 외부 인증 기능을 활성화합니다.

            2. 주요 변화점
            - 코드 구조와 아키텍처의 변화
              - OAuth2 인증 흐름을 담당하는 신규 서비스와 성공 핸들러를 도입해 보안 로직을 명확하게 분리했습니다.
              - 사용자 권한 부여와 로그인 흐름에서 OAuth2User를 기존 사용자 정보와 연결하는 방식으로 확장했습니다.
            - 새로운 의존성 및 라이브러리 추가
              - OAuth2 클라이언트 의존성을 프로젝트에 포함시키며 Google 계정 로그인을 사용할 수 있게 했습니다.
            - 설정 파일 변경으로 영향
              - 외부 인증 공급자(예: Google) 정보를 설정 파일에 등록하여 인증 파이프라인이 정상 작동하도록 했습니다.
            - 데이터 모델 변화 및 저장 로직
              - 사용자 엔티티에 인증 공급자 정보와 관련 아이디를 저장하는 필드를 추가하고, OAuth2 사용자도 로컬 계정으로 관리할 수 있도록 저장 로직을 보강했습니다.
            - 프런트엔드 및 UX 변화
              - 로그인 화면에 Google 로그인 버튼을 추가하고, 버튼 스타일 및 구분 요소를 개선했습니다. OAuth 로그인 경로도 사용자에게 명확하게 노출됩니다.
            - 보안 및 접근 제어의 영향
              - OAuth2 로그인 경로를 허용하는 설정이 추가되어 외부 인증으로의 접근이 활성화되었고, 성공 시 기본 페이지로의 원활한 이동이 가능해졌습니다.
            """;
    }
}
