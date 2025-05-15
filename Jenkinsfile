// Jenkinsfile (Declarative Pipeline 문법)

// 파이프라인이 실행될 에이전트 (빌드를 수행할 환경)
// 'any'는 Jenkins 환경에 사용 가능한 아무 에이전트에서나 실행하라는 의미
// 특정 환경이 필요하면 label을 지정할 수 있습니다. 예: agent { label 'my-linux-agent' }
agent any

// 빌드에 필요한 도구 설정
// Jenkins 관리 -> Global Tool Configuration 에서 설정한 JDK, Gradle의 'Name'을 여기에 적습니다.
// Wrapper 사용 시 엄밀히 필수는 아니지만, 명시적으로 해주는 것도 좋습니다.
tools {
    // 여러분이 Global Tool Configuration에서 JDK 설정 시 지정한 'Name'을 여기에 입력
    jdk 'OpenJDK_17' // 예: 'JDK_11', 'AdoptOpenJDK_17' 등
    // 여러분이 Global Tool Configuration에서 Gradle 설정 시 지정한 'Name'을 여기에 입력
    // Wrapper를 사용한다면 이 줄을 생략해도 됩니다. (아래 Build 단계 참고)
    // gradle 'Gradle_Tool' // 예: 'MyGradleTool'
}

// 환경 변수 정의 (선택 사항)
// 파이프라인 전반에 사용될 변수를 정의할 수 있습니다.
environment {
    // Docker 이미지 이름 설정 (여러분의 Docker Hub ID 또는 Private Registry 주소 포함)
    DOCKER_IMAGE_NAME = 'djjung/demo' // 예: 'myusername/my-gradle-app'
    // Docker 레지스트리 Credential ID (Jenkins Credential 관리에서 설정한 ID)
    DOCKER_CREDENTIAL_ID = 'dockerhub-credential' // 예: 'dockerhub-credential'
    // 배포 서버 SSH Credential ID (Jenkins Credential 관리에서 설정한 ID)
    SSH_CREDENTIAL_ID = 'my-server-ssh' // 예: 'deploy-server-ssh'
    // 배포 서버 사용자 이름 및 IP 주소
    DEPLOY_SERVER_USER = 'djjung' // 예: 'ubuntu'
    DEPLOY_SERVER_IP = '172.0.0.1' // 예: '192.168.1.100'
    // 배포할 컨테이너 이름 (임의로 지정)
    APP_CONTAINER_NAME = 'demo' // 예: 'my-gradle-app'
}


// 파이프라인의 각 단계를 정의
stages {
    // 1. 소스코드 Checkout 단계
    stage('Checkout') {
        steps {
            // Pipeline SCM 설정에서 Git 저장소를 정의했으므로,
            // checkout scm 스텝이 자동으로 해당 저장소 코드를 가져옵니다.
            checkout scm
            echo "소스코드 체크아웃 완료"
        }
    }

    // 2. 빌드 단계 (Gradle)
    stage('Build') {
        steps {
            echo "Gradle 빌드 시작"
            // Gradle Wrapper를 사용하여 빌드 실행
            // 프로젝트 루트에 gradlew (또는 gradlew.bat) 스크립트가 있어야 합니다.
            // -x test 는 빌드 과정에서 테스트 실행을 건너뛰는 옵션 (테스트 단계에서 따로 실행할 경우)
            sh './gradlew build -x test' // 리눅스/맥 에이전트
            // bat 'gradlew build -x test' // 윈도우 에이전트 사용 시

            // 빌드 결과물(예: jar/war 파일)을 나중에 사용할 수 있도록 보관
            // archiveArtifacts artifacts: 'build/libs/*.jar' // 여러분의 빌드 결과물 경로에 맞게 수정
            echo "Gradle 빌드 완료"
        }
    }

    // 3. 테스트 단계 (Gradle)
    stage('Test') {
        steps {
             echo "Gradle 테스트 시작"
             // ./gradlew test 명령으로 테스트 실행
             sh './gradlew test' // 리눅스/맥 에이전트
             // bat 'gradlew test' // 윈도우 에이전트 사용 시
             echo "Gradle 테스트 완료"

             // 테스트 결과 리포트 게시 (선택 사항, JUnit 등 테스트 결과 플러그인 필요)
             // junit '**/TEST-*.xml' // 여러분의 테스트 리포트 파일 경로에 맞게 수정
        }
    }

    // 4. Docker 이미지 빌드 단계
    // 프로젝트 루트에 애플리케이션을 실행하기 위한 Dockerfile이 필요합니다.
    stage('Build Docker Image') {
        steps {
            script {
                echo "Docker 이미지 빌드 시작"
                // 이미지 이름과 태그 설정 (빌드 번호를 태그로 사용하는 것을 권장)
                def imageTag = env.BUILD_NUMBER
                def fullImageName = "${env.DOCKER_IMAGE_NAME}:${imageTag}"

                // Dockerfile을 사용하여 이미지 빌드 (Dockerfile이 . (현재 워크스페이스 루트)에 있다고 가정)
                docker.build(fullImageName, '.') // '.'은 Dockerfile 경로

                echo "Docker 이미지 빌드 완료: ${fullImageName}"
            }
        }
    }

    // 5. Docker 이미지 레지스트리 Push 단계
    stage('Push Docker Image') {
        steps {
            script {
                echo "Docker 이미지 레지스트리 푸시 시작"
                def imageTag = env.BUILD_NUMBER
                def fullImageName = "${env.DOCKER_IMAGE_NAME}:${imageTag}"

                // Jenkins Credential 관리에서 등록한 Docker 레지스트리 자격 증명을 사용하여 푸시
                // withRegistry '레지스트리 URL', 'Credential ID'
                // Docker Hub의 경우 URL 생략 가능 (또는 'https://index.docker.io/v1/' 사용)
                docker.withRegistry('', env.DOCKER_CREDENTIAL_ID) { // Docker Hub 예시
                // docker.withRegistry('https://여러분의_Private_Registry.com', env.DOCKER_CREDENTIAL_ID) { // Private Registry 예시
                    docker.image(fullImageName).push()
                }

                echo "Docker 이미지 레지스트리 푸시 완료"
            }
        }
    }

    // 6. 배포 단계 (이 단계는 여러분의 Docker 환경에 따라 크게 달라집니다!)
    stage('Deploy') {
         steps {
             echo "배포 단계 시작"
             script {
                 // === 가장 일반적인 예시: SSH로 배포 서버에 접속하여 Docker 컨테이너 업데이트 ===
                 // Jenkins Credential 관리에서 등록한 배포 서버 SSH 자격 증명을 사용합니다.
                 sshagent([env.SSH_CREDENTIAL_ID]) {
                     echo "배포 서버 ${env.DEPLOY_SERVER_IP}에 SSH 접속 시도"
                     // SSH로 배포 서버에 접속하여 Docker 명령 실행

                     // 1. 최신 이미지 풀 (다운로드)
                     sh "ssh ${env.DEPLOY_SERVER_USER}@${env.DEPLOY_SERVER_IP} 'docker pull ${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}'"
                     echo "Docker 이미지 풀 완료"

                     // 2. 기존 컨테이너 중지 및 삭제 (실행 중이 아닐 경우 오류 방지)
                     sh "ssh ${env.DEPLOY_SERVER_USER}@${env.DEPLOY_SERVER_IP} 'docker stop ${env.APP_CONTAINER_NAME} || true'"
                     sh "ssh ${env.DEPLOY_SERVER_USER}@${env.DEPLOY_SERVER_IP} 'docker rm ${env.APP_CONTAINER_NAME} || true'"
                     echo "기존 컨테이너 중지 및 삭제 완료"

                     // 3. 새로운 컨테이너 실행
                     // -d: 백그라운드 실행, --name: 컨테이너 이름 지정, -p: 포트 포워딩 (호스트 포트:컨테이너 포트)
                     // 다른 옵션들 (--network, --env, --volume 등)은 여러분의 Docker 실행 설정에 맞게 추가해야 합니다.
                     sh "ssh ${env.DEPLOY_SERVER_USER}@${env.DEPLOY_SERVER_IP} 'docker run -d --name ${env.APP_CONTAINER_NAME} -p 8080:8080 ${env.DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}'"
                     echo "새로운 Docker 컨테이너 실행 완료"
                 }
                 // ======================================================

                 // === 다른 배포 방법 예시 (위 SSH 예시 대신 필요에 따라 사용) ===
                 // * Docker Compose 사용: SSH 접속 후 docker-compose.yml 파일 위치로 이동하여 'docker-compose pull && docker-compose up -d' 명령 실행
                 // * Kubernetes (K8s) 사용: Jenkins 에이전트에 kubectl 설치 후 'kubectl apply -f deployment.yaml' 또는 'kubectl set image ...' 명령 실행, 또는 Kubernetes 플러그인 사용
                 // * 클라우드 서비스(AWS ECS, EKS, Azure AKS 등)의 배포 도구(aws cli, az cli 등) 사용
                 // ========================================
             }
             echo "배포 단계 완료"
         }
    }
}

// 파이프라인 실행 후 작업 (성공/실패 알림 등)
post {
    always { // 항상 실행 (성공, 실패, 중단 등)
        echo "파이프라인 실행 종료."
    }
    success { // 파이프라인 성공 시 실행
        echo '✨ 파이프라인이 성공적으로 완료되었습니다! ✨'
        // 성공 알림 설정 (예: Slack, Email 플러그인 사용)
    }
    failure { // 파이프라인 실패 시 실행
        echo '🚨 파이프라인 실행 중 오류가 발생했습니다! 🚨'
        // 실패 알림 설정
    }
}
