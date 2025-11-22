from openai import OpenAI
import json
import os

# 환경변수에서 API 키 가져오기
api_key = os.getenv("OPENAI_API_KEY", "YOUR_API_KEY")
client = OpenAI(api_key=api_key)

def summarize_commit(commit_json: dict) -> str:
    prompt = f"""
당신은 GitHub 커밋 변경 내용을 분석해서 팀원들에게 공유하기 위한 요약 문서를 작성하는 AI입니다.
이 요약은 커밋이나 merge 시 발생한 변화와 새로운 기능을 팀원들이 쉽게 이해할 수 있도록 작성되어야 합니다.

커밋 데이터 구조:
- commit.message: 커밋 메시지
- commit.author.date: 커밋 날짜 (ISO 8601 형식)
- files: 변경된 파일 배열
  - filename: 파일 경로
  - patch: diff 패치 내용 (파일이 생성/수정/삭제된 내용)

당신의 출력은 반드시 다음 형식을 정확히 따라야 합니다:

## 날짜
(commit.author.date 값 YYYY-MM-DD HH:MM:SS 형식으로 출력)

## 변경된 파일 목록
각 파일의 변경 유형을 판단하여 다음 형식으로 출력:
- [생성] 파일경로 (patch에 @@ -0,0로 시작하는 경우)
- [수정] 파일경로 (patch에 기존 라인 번호가 있는 경우)
- [삭제] 파일경로 (patch에 @@ -N,0로 시작하고 추가 라인이 없는 경우)

파일 목록은 줄바꿈으로 구분하여 나열하세요.

## 변경 내용 요약
이 섹션은 팀원들이 "무엇이 달라졌는지", "어떤 기능이 추가되거나 변경되었는지"를 빠르게 파악할 수 있도록 작성해야 합니다.

다음 항목들을 포함하여 작성하세요:

1. **추가된 기능 또는 변경사항**
   - 새로 생긴 기능이나 개선된 기능을 명확히 설명
   - 사용자나 시스템에 미치는 영향
   - 예: "사용자 로그인 기능 추가", "데이터베이스 연결 설정 변경"

2. **주요 변화점**
   - 코드 구조나 아키텍처의 변화
   - 새로운 의존성이나 라이브러리 추가
   - 설정 파일 변경으로 인한 영향

작성 시 주의사항:
- 기술적인 코드 세부사항(변수명, 함수명 등)은 제외
- "무엇을" 했는지보다 "왜" 그리고 "어떤 영향"이 있는지에 초점
- 팀원들이 코드를 보지 않아도 이해할 수 있도록 작성
- 간결하고 명확하게, 불필요한 설명은 제외

아래는 분석 대상 커밋 데이터입니다:

{json.dumps(commit_json, ensure_ascii=False, indent=2)}
"""

    response = client.chat.completions.create(
        model="gpt-5-nano",
        messages=[
            {"role": "system", "content": "당신은 개발 팀의 커밋 변경사항을 팀원들에게 공유하기 위한 요약 문서를 작성하는 전문가입니다. 기술적 세부사항보다는 변화와 기능에 초점을 맞춰 이해하기 쉽게 작성해야 합니다. 주어진 형식을 정확히 따라야 합니다."},
            {"role": "user", "content": prompt}
        ]
        # temperature 파라미터 제거: gpt-5-nano는 기본값(1)만 지원
    )

    return response.choices[0].message.content


# 예시 실행
if __name__ == "__main__":
    import sys
    import io
    
    # Windows 콘솔 인코딩 문제 해결: stdout을 UTF-8로 강제 설정
    if sys.platform == 'win32':
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
        sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')
    
    # stdin에서 JSON 데이터 읽기 (UTF-8 서로게이트 문자 처리)
    try:
        # sys.stdin을 바이너리 모드로 읽고 UTF-8로 디코딩 (에러 처리 포함)
        input_bytes = sys.stdin.buffer.read()
        # 서로게이트 문자를 무시하거나 대체하여 디코딩
        input_data = input_bytes.decode('utf-8', errors='replace')
        commit_data = json.loads(input_data)
        summary = summarize_commit(commit_data)
        print(summary, flush=True)  # flush=True로 즉시 출력
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON input: {e}", file=sys.stderr, flush=True)
        sys.exit(1)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr, flush=True)
        sys.exit(1)

