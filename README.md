# 🗝️ For the Key - 팀 프로젝트

## 🛠️ 1. 개발 환경 필독 (Prerequisites)
이 프로젝트는 **Java 21**을 기반으로 개발되었습니다.
팀원분들은 반드시 아래 버전을 설치하고 환경을 맞춰주세요. (버전 다르면 에러 남!)

* **Java:** **JDK 21** (Java SE 21)
* **IDE:** **Eclipse 2025-09 (4.37.0)** 또는 2023-12 이상 권장
    * *주의: 구버전 이클립스에서는 Java 21 컴파일러가 작동하지 않습니다.*
* **Text Encoding:** UTF-8

### ⚠️ 실행 시 주의사항
1. 프로젝트를 `Clone` 받은 후, 프로젝트 우클릭 > `Properties` > `Java Build Path`로 이동합니다.
2. `Libraries` 탭에서 **JRE System Library**가 `JavaSE-21`로 되어있는지 확인하세요.
3. 만약 에러가 있다면 더블클릭하여 본인의 **JDK 21** 경로로 다시 잡아주세요.

---

## 🤝 2. 협업 (Git Workflow)

우리는 **`main` 브랜치에 직접 푸시하지 않습니다.** (절대 금지!)
반드시 내 영어 이름으로 된 **'개인 작업장(Branch)'**에서 만들고 웹에서 합칩니다.

### ① 내 작업장(브랜치) 만들기
딱 한 번만, 내 영어 이름으로 작업장을 만듭니다.
* **서상원:** `sangwon`
* **김선욱:** `sunwook`
* **김민정:** `minjeong`
* **서충만:** `chungman`

---

### ② 개발 순서 (매일 반복하세요)

#### 1. 시작할 때 (최신 코드 받기)
터미널에 이 3줄을 순서대로 입력하세요.
```bash
git checkout main      # 1. 메인으로 이동
git pull origin main   # 2. 남들이 한 거 받아오기 (동기화)
git checkout 내이름     # 3. 내 작업장으로 이동 (예: git checkout sangwon)