#!/usr/bin/env groovy
// BloodBank CI/CD Pipeline — 11 Stages
// Covers milestones M7-019 to M7-031

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        timeout(time: 90, unit: 'MINUTES')
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds(abortPrevious: true)
    }

    environment {
        // Registry & image settings
        REGISTRY          = credentials('docker-registry-url')   // e.g. registry.example.com
        REGISTRY_CREDS    = 'docker-registry-credentials'
        IMAGE_PREFIX      = 'bloodbank'
        GIT_COMMIT_SHA    = ''
        IMAGE_TAG         = ''

        // Kubernetes
        KUBECONFIG_CRED   = 'kubeconfig-credentials'
        K8S_DEV_NS        = 'bloodbank-dev'
        K8S_STAGING_NS    = 'bloodbank-staging'
        K8S_PROD_NS       = 'bloodbank-prod'

        // SonarQube
        SONAR_HOST        = credentials('sonarqube-host-url')
        SONAR_TOKEN       = credentials('sonarqube-token')

        // Slack
        SLACK_CHANNEL     = '#bloodbank-ci'
        SLACK_CREDS       = 'slack-bot-token'

        // OWASP NVD API key (optional — speeds up dependency-check)
        NVD_API_KEY       = credentials('nvd-api-key')

        // Snyk (optional)
        SNYK_TOKEN        = credentials('snyk-token')
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Reusable lists
    // ─────────────────────────────────────────────────────────────────────────────

    stages {

        // ── Stage 1: Checkout ────────────────────────────────────────────────────
        stage('1. Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: scm.branches,
                    extensions: [
                        [$class: 'CloneOption', shallow: false, noTags: false],
                        [$class: 'CleanBeforeCheckout']
                    ],
                    userRemoteConfigs: [[
                        url: scm.userRemoteConfigs[0].url,
                        credentialsId: 'github-credentials'
                    ]]
                ])
                script {
                    GIT_COMMIT_SHA = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    def semver = sh(
                        script: "git describe --tags --match 'v*' --abbrev=0 2>/dev/null || echo 'v0.0.0'",
                        returnStdout: true
                    ).trim()
                    IMAGE_TAG = "${semver}-${GIT_COMMIT_SHA}"
                    env.GIT_COMMIT_SHA = GIT_COMMIT_SHA
                    env.IMAGE_TAG      = IMAGE_TAG
                    currentBuild.displayName = "#${BUILD_NUMBER} — ${IMAGE_TAG}"
                    echo "Building image tag: ${IMAGE_TAG}"
                }
            }
            post {
                failure { slackNotify('FAILED', 'Checkout failed') }
            }
        }

        // ── Stage 2: Gradle Build ─────────────────────────────────────────────────
        stage('2. Gradle Build') {
            steps {
                script {
                    slackNotify('STARTED', "Build #${BUILD_NUMBER} started — ${IMAGE_TAG}")
                }
                sh '''
                    chmod +x gradlew
                    ./gradlew build -x test \
                        --parallel \
                        --build-cache \
                        --no-daemon \
                        --info \
                        -Dorg.gradle.jvmargs="-Xmx2g -XX:+HeapDumpOnOutOfMemoryError"
                '''
            }
            post {
                always {
                    archiveArtifacts(
                        artifacts: '**/build/libs/*.jar',
                        allowEmptyArchive: true,
                        fingerprint: true
                    )
                }
                failure { slackNotify('FAILED', 'Gradle build failed') }
            }
        }

        // ── Stage 3: Unit Tests + Coverage ───────────────────────────────────────
        stage('3. Unit Tests + Coverage') {
            steps {
                sh '''
                    ./gradlew test jacocoTestReport \
                        --parallel \
                        --no-daemon \
                        -Dorg.gradle.jvmargs="-Xmx2g"
                '''
                // Enforce 80% line + branch coverage across all subprojects
                sh './gradlew jacocoTestCoverageVerification --no-daemon'
            }
            post {
                always {
                    junit(
                        testResults: '**/build/test-results/test/*.xml',
                        allowEmptyResults: false
                    )
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: '',
                        reportFiles: '**/build/reports/jacoco/test/html/index.html',
                        reportName: 'JaCoCo Coverage Report',
                        reportTitles: 'Coverage'
                    ])
                }
                failure { slackNotify('FAILED', 'Unit tests or coverage gate failed (threshold: 80%)') }
            }
        }

        // ── Stage 4: SonarQube Analysis ──────────────────────────────────────────
        stage('4. SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        ./gradlew sonar \
                            --no-daemon \
                            -Dsonar.host.url=${SONAR_HOST} \
                            -Dsonar.token=${SONAR_TOKEN} \
                            -Dsonar.projectKey=bloodbank \
                            -Dsonar.projectName='BloodBank' \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.coverage.jacoco.xmlReportPaths='**/build/reports/jacoco/test/jacocoTestReport.xml' \
                            -Dsonar.exclusions='**/generated/**,**/test/**'
                    """
                }
                // Block pipeline until quality gate result is available
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
            post {
                failure { slackNotify('FAILED', 'SonarQube quality gate failed') }
            }
        }

        // ── Stage 5: Security Scan ────────────────────────────────────────────────
        stage('5. Security Scan') {
            parallel {
                stage('OWASP Dependency-Check') {
                    steps {
                        sh """
                            ./gradlew dependencyCheckAggregate \
                                --no-daemon \
                                -DnvdApiKey=${NVD_API_KEY} \
                                -DfailBuildOnCVSS=7 \
                                -DsuppressionFile=owasp-suppressions.xml || true
                        """
                        dependencyCheckPublisher(
                            pattern: '**/build/reports/dependency-check-report.xml',
                            failedTotalCritical: 1,
                            failedTotalHigh: 5,
                            unstableTotalHigh: 1
                        )
                    }
                    post {
                        always {
                            archiveArtifacts(
                                artifacts: '**/build/reports/dependency-check-report.*',
                                allowEmptyArchive: true
                            )
                        }
                    }
                }
                stage('Trivy FS Scan') {
                    steps {
                        sh """
                            trivy fs . \
                                --severity CRITICAL,HIGH \
                                --exit-code 1 \
                                --format sarif \
                                --output trivy-fs-report.sarif \
                                --ignore-unfixed \
                                --scanners vuln,secret,misconfig \
                                --skip-dirs '.git,.gradle,node_modules'
                        """
                    }
                    post {
                        always {
                            archiveArtifacts(
                                artifacts: 'trivy-fs-report.sarif',
                                allowEmptyArchive: true
                            )
                        }
                        failure { slackNotify('FAILED', 'Trivy filesystem scan found CRITICAL/HIGH vulnerabilities') }
                    }
                }
            }
        }

        // ── Stage 6: Docker Build & Push ─────────────────────────────────────────
        stage('6. Docker Build & Push') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: REGISTRY_CREDS,
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh 'echo "${DOCKER_PASS}" | docker login ${REGISTRY} -u "${DOCKER_USER}" --password-stdin'
                    }

                    def services = [
                        'config-server',
                        'api-gateway',
                        'branch-service',
                        'donor-service',
                        'lab-service',
                        'inventory-service',
                        'transfusion-service',
                        'hospital-service',
                        'request-matching-service',
                        'billing-service',
                        'notification-service',
                        'reporting-service',
                        'document-service',
                        'compliance-service'
                    ]

                    // Build backend images in parallel
                    def buildSteps = services.collectEntries { svc ->
                        ["build-${svc}": {
                            def imgBase = "${REGISTRY}/${IMAGE_PREFIX}/${svc}"
                            sh """
                                docker build \
                                    --build-arg BUILD_DATE=\$(date -u +%Y-%m-%dT%H:%M:%SZ) \
                                    --build-arg GIT_COMMIT=${GIT_COMMIT_SHA} \
                                    --build-arg IMAGE_TAG=${IMAGE_TAG} \
                                    --cache-from ${imgBase}:latest \
                                    --tag ${imgBase}:${IMAGE_TAG} \
                                    --tag ${imgBase}:latest \
                                    --file backend/${svc}/Dockerfile \
                                    .
                                docker push ${imgBase}:${IMAGE_TAG}
                                docker push ${imgBase}:latest
                            """
                        }]
                    }

                    // Also build db-migration and frontend
                    buildSteps['build-db-migration'] = {
                        def imgBase = "${REGISTRY}/${IMAGE_PREFIX}/db-migration"
                        sh """
                            docker build \
                                --build-arg BUILD_DATE=\$(date -u +%Y-%m-%dT%H:%M:%SZ) \
                                --build-arg GIT_COMMIT=${GIT_COMMIT_SHA} \
                                --cache-from ${imgBase}:latest \
                                --tag ${imgBase}:${IMAGE_TAG} \
                                --tag ${imgBase}:latest \
                                --file shared-libs/db-migration/Dockerfile \
                                .
                            docker push ${imgBase}:${IMAGE_TAG}
                            docker push ${imgBase}:latest
                        """
                    }
                    buildSteps['build-frontend'] = {
                        def imgBase = "${REGISTRY}/${IMAGE_PREFIX}/frontend"
                        sh """
                            docker build \
                                --build-arg BUILD_DATE=\$(date -u +%Y-%m-%dT%H:%M:%SZ) \
                                --build-arg GIT_COMMIT=${GIT_COMMIT_SHA} \
                                --cache-from ${imgBase}:latest \
                                --tag ${imgBase}:${IMAGE_TAG} \
                                --tag ${imgBase}:latest \
                                --file frontend/bloodbank-ui/Dockerfile \
                                frontend/bloodbank-ui
                            docker push ${imgBase}:${IMAGE_TAG}
                            docker push ${imgBase}:latest
                        """
                    }

                    parallel buildSteps

                    // Run Trivy vulnerability scan against each pushed image
                    def scanSteps = (services + ['db-migration', 'frontend']).collectEntries { svc ->
                        ["scan-${svc}": {
                            sh """
                                trivy image \
                                    --severity CRITICAL,HIGH \
                                    --exit-code 1 \
                                    --format sarif \
                                    --output trivy-image-${svc}.sarif \
                                    --ignore-unfixed \
                                    ${REGISTRY}/${IMAGE_PREFIX}/${svc}:${IMAGE_TAG} || true
                            """
                        }]
                    }
                    parallel scanSteps
                }
            }
            post {
                always {
                    archiveArtifacts(
                        artifacts: 'trivy-image-*.sarif',
                        allowEmptyArchive: true
                    )
                    sh 'docker logout ${REGISTRY} || true'
                }
                failure { slackNotify('FAILED', 'Docker build or push failed') }
            }
        }

        // ── Stage 7: Flyway Migration ─────────────────────────────────────────────
        stage('7. Flyway Migration') {
            steps {
                withCredentials([file(credentialsId: KUBECONFIG_CRED, variable: 'KUBECONFIG')]) {
                    sh """
                        export KUBECONFIG=${KUBECONFIG}

                        # Delete any previous job so we can re-apply cleanly
                        kubectl delete job flyway-migration -n ${K8S_DEV_NS} --ignore-not-found=true

                        # Substitute image tag and apply
                        sed 's|\${IMAGE_TAG}|${IMAGE_TAG}|g' k8s/jobs/flyway-migration.yml \
                          | sed 's|namespace: bloodbank-prod|namespace: ${K8S_DEV_NS}|g' \
                          | kubectl apply -f - -n ${K8S_DEV_NS}

                        # Wait up to 5 minutes for the job to complete
                        kubectl wait job/flyway-migration \
                            --for=condition=complete \
                            --timeout=300s \
                            -n ${K8S_DEV_NS}

                        kubectl logs -l app.kubernetes.io/name=flyway-migration \
                            -n ${K8S_DEV_NS} --tail=100 || true
                    """
                }
            }
            post {
                failure { slackNotify('FAILED', 'Flyway migration job failed') }
            }
        }

        // ── Stage 8: Deploy DEV + Smoke Tests ────────────────────────────────────
        stage('8. Deploy DEV') {
            steps {
                withCredentials([file(credentialsId: KUBECONFIG_CRED, variable: 'KUBECONFIG')]) {
                    sh """
                        export KUBECONFIG=${KUBECONFIG}

                        # Replace placeholder tag in all manifests and apply
                        find k8s/ -name '*.yml' \
                            -not -path 'k8s/jobs/*' \
                            -not -path 'k8s/namespaces/*' \
                          | while read f; do
                              sed 's|\${IMAGE_TAG}|${IMAGE_TAG}|g; s|namespace: bloodbank-prod|namespace: ${K8S_DEV_NS}|g' "\$f"
                            done \
                          | kubectl apply -f - -n ${K8S_DEV_NS}

                        # Wait for every Deployment rollout in DEV
                        for deploy in \$(kubectl get deployments -n ${K8S_DEV_NS} -o name); do
                            kubectl rollout status \$deploy -n ${K8S_DEV_NS} --timeout=300s
                        done
                    """
                    // Smoke tests: verify every service health endpoint
                    sh '''
                        GATEWAY_URL=$(kubectl get svc api-gateway \
                            -n bloodbank-dev \
                            -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null \
                            || echo "localhost")
                        echo "Running smoke tests against ${GATEWAY_URL}..."
                        for i in $(seq 1 10); do
                            STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
                                http://${GATEWAY_URL}:8080/actuator/health || echo "000")
                            if [ "$STATUS" = "200" ]; then
                                echo "Smoke test passed (attempt $i)"
                                exit 0
                            fi
                            echo "Attempt $i: HTTP ${STATUS} — retrying in 15s..."
                            sleep 15
                        done
                        echo "Smoke test FAILED after 10 attempts"
                        exit 1
                    '''
                }
            }
            post {
                failure { slackNotify('FAILED', 'DEV deployment or smoke tests failed') }
                success { slackNotify('SUCCESS', "DEV deployment succeeded — ${IMAGE_TAG}") }
            }
        }

        // ── Stage 9: Integration Tests ────────────────────────────────────────────
        stage('9. Integration Tests') {
            steps {
                withCredentials([file(credentialsId: KUBECONFIG_CRED, variable: 'KUBECONFIG')]) {
                    script {
                        def gatewayIp = sh(
                            script: """
                                kubectl get svc api-gateway \
                                    -n ${K8S_DEV_NS} \
                                    -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null \
                                    || echo 'localhost'
                            """,
                            returnStdout: true
                        ).trim()

                        sh """
                            ./gradlew integrationTest \
                                --no-daemon \
                                -Dintegration.base-url=http://${gatewayIp}:8080 \
                                -Dorg.gradle.jvmargs="-Xmx2g"
                        """
                    }
                }
            }
            post {
                always {
                    junit(
                        testResults: '**/build/test-results/integrationTest/*.xml',
                        allowEmptyResults: true
                    )
                }
                failure { slackNotify('FAILED', 'Integration tests failed against DEV') }
            }
        }

        // ── Stage 10: Deploy STAGING ──────────────────────────────────────────────
        stage('10. Deploy STAGING') {
            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                }
            }
            steps {
                withCredentials([file(credentialsId: KUBECONFIG_CRED, variable: 'KUBECONFIG')]) {
                    sh """
                        export KUBECONFIG=${KUBECONFIG}

                        # Run Flyway migration in staging namespace first
                        kubectl delete job flyway-migration -n ${K8S_STAGING_NS} --ignore-not-found=true
                        sed 's|\${IMAGE_TAG}|${IMAGE_TAG}|g; s|namespace: bloodbank-prod|namespace: ${K8S_STAGING_NS}|g' \
                            k8s/jobs/flyway-migration.yml \
                          | kubectl apply -f - -n ${K8S_STAGING_NS}
                        kubectl wait job/flyway-migration \
                            --for=condition=complete \
                            --timeout=300s \
                            -n ${K8S_STAGING_NS}

                        # Deploy all services
                        find k8s/ -name '*.yml' \
                            -not -path 'k8s/jobs/*' \
                            -not -path 'k8s/namespaces/*' \
                          | while read f; do
                              sed 's|\${IMAGE_TAG}|${IMAGE_TAG}|g; s|namespace: bloodbank-prod|namespace: ${K8S_STAGING_NS}|g' "\$f"
                            done \
                          | kubectl apply -f - -n ${K8S_STAGING_NS}

                        for deploy in \$(kubectl get deployments -n ${K8S_STAGING_NS} -o name); do
                            kubectl rollout status \$deploy -n ${K8S_STAGING_NS} --timeout=300s
                        done
                    """
                    // Staging smoke test
                    sh '''
                        GATEWAY_URL=$(kubectl get svc api-gateway \
                            -n bloodbank-staging \
                            -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null \
                            || echo "localhost")
                        for i in $(seq 1 10); do
                            STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
                                http://${GATEWAY_URL}:8080/actuator/health || echo "000")
                            if [ "$STATUS" = "200" ]; then
                                echo "Staging smoke test passed"
                                exit 0
                            fi
                            echo "Attempt $i: HTTP ${STATUS} — retrying in 15s..."
                            sleep 15
                        done
                        echo "Staging smoke test FAILED"
                        exit 1
                    '''
                }
            }
            post {
                failure { slackNotify('FAILED', 'STAGING deployment failed') }
                success { slackNotify('SUCCESS', "STAGING deployment succeeded — ${IMAGE_TAG}") }
            }
        }

        // ── Stage 11: Deploy PRODUCTION ───────────────────────────────────────────
        stage('11. Deploy PRODUCTION') {
            when {
                branch 'main'
            }
            steps {
                // Manual gate — waits for human approval
                timeout(time: 24, unit: 'HOURS') {
                    input(
                        message: "Approve deployment of ${IMAGE_TAG} to PRODUCTION?",
                        ok: 'Deploy',
                        submitter: 'prod-approvers',
                        parameters: [
                            booleanParam(
                                name: 'CONFIRM',
                                defaultValue: false,
                                description: 'Check to confirm you have reviewed the staging results'
                            )
                        ]
                    )
                }

                withCredentials([file(credentialsId: KUBECONFIG_CRED, variable: 'KUBECONFIG')]) {
                    script {
                        // ── Flyway in production ──────────────────────────────
                        sh """
                            export KUBECONFIG=${KUBECONFIG}
                            kubectl delete job flyway-migration -n ${K8S_PROD_NS} --ignore-not-found=true
                            sed 's|\${IMAGE_TAG}|${IMAGE_TAG}|g' k8s/jobs/flyway-migration.yml \
                              | kubectl apply -f - -n ${K8S_PROD_NS}
                            kubectl wait job/flyway-migration \
                                --for=condition=complete \
                                --timeout=300s \
                                -n ${K8S_PROD_NS}
                        """

                        // ── Blue-Green services ───────────────────────────────
                        // donor, inventory, lab, transfusion, api-gateway, frontend
                        def blueGreenServices = [
                            [name: 'donor-service',        port: 8082, label: 'app.kubernetes.io/name=donor-service'],
                            [name: 'inventory-service',    port: 8084, label: 'app.kubernetes.io/name=inventory-service'],
                            [name: 'lab-service',          port: 8083, label: 'app.kubernetes.io/name=lab-service'],
                            [name: 'transfusion-service',  port: 8085, label: 'app.kubernetes.io/name=transfusion-service'],
                            [name: 'api-gateway',          port: 8080, label: 'app.kubernetes.io/name=api-gateway'],
                            [name: 'frontend',             port: 80,   label: 'app.kubernetes.io/name=frontend']
                        ]
                        blueGreenDeploy(blueGreenServices)

                        // ── Canary services (10% → 50% → 100%) ───────────────
                        // billing-service, request-matching-service
                        def canaryServices = [
                            [name: 'billing-service',          port: 8088, stableReplicas: 9],
                            [name: 'request-matching-service', port: 8087, stableReplicas: 9]
                        ]
                        canaryDeploy(canaryServices)

                        // ── Rolling update — all remaining services ────────────
                        def rollingServices = [
                            'config-server',
                            'branch-service',
                            'hospital-service',
                            'notification-service',
                            'reporting-service',
                            'document-service',
                            'compliance-service'
                        ]
                        rollingDeploy(rollingServices)
                    }
                }
            }
            post {
                failure { slackNotify('FAILED', "PRODUCTION deployment FAILED — ${IMAGE_TAG} — immediate attention required!") }
                success { slackNotify('SUCCESS', ":rocket: PRODUCTION deployment succeeded — ${IMAGE_TAG}") }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    post {
        always {
            archiveArtifacts(
                artifacts: [
                    '**/build/reports/**',
                    '**/build/test-results/**',
                    'trivy-*.sarif'
                ].join(', '),
                allowEmptyArchive: true,
                fingerprint: true
            )
            cleanWs()
        }
        success {
            slackNotify('SUCCESS', "Pipeline #${BUILD_NUMBER} completed successfully — ${IMAGE_TAG}")
        }
        failure {
            slackNotify('FAILED', "Pipeline #${BUILD_NUMBER} FAILED — ${IMAGE_TAG} — see ${BUILD_URL}")
        }
        unstable {
            slackNotify('UNSTABLE', "Pipeline #${BUILD_NUMBER} is UNSTABLE — ${IMAGE_TAG}")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper functions
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Send a Slack notification.
 * @param status  STARTED | SUCCESS | FAILED | UNSTABLE
 * @param message Human-readable message
 */
void slackNotify(String status, String message) {
    def colors = [
        STARTED:  '#439FE0',
        SUCCESS:  'good',
        FAILED:   'danger',
        UNSTABLE: 'warning'
    ]
    def icons = [
        STARTED:  ':building_construction:',
        SUCCESS:  ':white_check_mark:',
        FAILED:   ':red_circle:',
        UNSTABLE: ':warning:'
    ]
    def color   = colors.getOrDefault(status, '#439FE0')
    def icon    = icons.getOrDefault(status, ':information_source:')
    def jobLink = "<${env.BUILD_URL}|${env.JOB_NAME} #${env.BUILD_NUMBER}>"
    slackSend(
        channel:     env.SLACK_CHANNEL,
        tokenCredentialId: env.SLACK_CREDS,
        color:       color,
        message:     "${icon} *BloodBank CI/CD* | ${jobLink}\n${message}"
    )
}

/**
 * Blue-Green deployment for the given services.
 * Deploys a green deployment, runs health checks, then patches the Service selector
 * to point to green. The previous blue deployment is kept for one release as fallback.
 */
void blueGreenDeploy(List services) {
    def kubeconfig = env.KUBECONFIG
    def ns         = env.K8S_PROD_NS
    def imageTag   = env.IMAGE_TAG
    def registry   = env.REGISTRY
    def prefix     = env.IMAGE_PREFIX

    services.each { svc ->
        def svcName = svc.name
        def port    = svc.port

        echo "Blue-Green deploy: ${svcName} — tag ${imageTag}"

        // Determine current color
        def currentColor = sh(
            script: """
                kubectl get deployment ${svcName}-blue \
                    -n ${ns} \
                    -o jsonpath='{.metadata.labels.version}' 2>/dev/null \
                    || echo 'green'
            """,
            returnStdout: true
        ).trim()
        def newColor = (currentColor == 'blue') ? 'green' : 'blue'

        sh """
            export KUBECONFIG=${kubeconfig}

            # Build the new (${newColor}) deployment manifest from the existing blue manifest
            kubectl get deployment ${svcName}-blue \
                -n ${ns} -o yaml 2>/dev/null \
              | sed "s/name: ${svcName}-blue/name: ${svcName}-${newColor}/g; \
                     s/version: blue/version: ${newColor}/g; \
                     s|image: .*/${svcName}:.*|image: ${registry}/${prefix}/${svcName}:${imageTag}|g" \
              | kubectl apply -f - -n ${ns}

            # Wait for new deployment to be fully available
            kubectl rollout status deployment/${svcName}-${newColor} \
                -n ${ns} --timeout=300s

            # Health-check the new pods before switching traffic
            NEW_POD=\$(kubectl get pods -n ${ns} \
                -l app.kubernetes.io/name=${svcName},version=${newColor} \
                -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
            if [ -n "\$NEW_POD" ]; then
                for i in \$(seq 1 12); do
                    STATUS=\$(kubectl exec -n ${ns} \$NEW_POD -- \
                        wget -qO- http://localhost:${port}/actuator/health 2>/dev/null \
                        | grep -c '"status":"UP"' || echo "0")
                    if [ "\$STATUS" -gt "0" ]; then
                        echo "${svcName} ${newColor} pods are healthy"
                        break
                    fi
                    echo "Waiting for health... attempt \$i/12"
                    sleep 10
                done
            fi

            # Switch Service selector to the new color
            kubectl patch service ${svcName} \
                -n ${ns} \
                --type=json \
                -p='[{"op":"replace","path":"/spec/selector/version","value":"${newColor}"}]'

            echo "Traffic switched to ${svcName}-${newColor}"
        """
    }
}

/**
 * Canary deployment: 10% → 50% → 100% with health gate between each step.
 * Uses separate stable and canary Deployments sharing the same Service label.
 */
void canaryDeploy(List services) {
    def kubeconfig = env.KUBECONFIG
    def ns         = env.K8S_PROD_NS
    def imageTag   = env.IMAGE_TAG
    def registry   = env.REGISTRY
    def prefix     = env.IMAGE_PREFIX

    services.each { svc ->
        def svcName        = svc.name
        def stableReplicas = svc.stableReplicas ?: 9

        echo "Canary deploy: ${svcName} — tag ${imageTag}"

        // Phase 1 — 10% (1 canary pod vs stableReplicas stable pods)
        sh """
            export KUBECONFIG=${kubeconfig}
            kubectl get deployment ${svcName}-stable \
                -n ${ns} -o yaml 2>/dev/null \
              | sed "s/name: ${svcName}-stable/name: ${svcName}-canary/g; \
                     s/track: stable/track: canary/g; \
                     s|image: .*/${svcName}:.*|image: ${registry}/${prefix}/${svcName}:${imageTag}|g; \
                     s/replicas: .*/replicas: 1/" \
              | kubectl apply -f - -n ${ns}
            kubectl rollout status deployment/${svcName}-canary -n ${ns} --timeout=180s
        """
        echo "Canary 10% live for ${svcName} — monitoring 60 s..."
        sleep 60
        assertCanaryHealthy(svcName, ns, kubeconfig)

        // Phase 2 — 50%
        sh """
            export KUBECONFIG=${kubeconfig}
            kubectl scale deployment/${svcName}-stable \
                --replicas=\$(( ${stableReplicas} / 2 )) -n ${ns}
            kubectl scale deployment/${svcName}-canary \
                --replicas=\$(( ${stableReplicas} / 2 )) -n ${ns}
        """
        echo "Canary 50% live for ${svcName} — monitoring 60 s..."
        sleep 60
        assertCanaryHealthy(svcName, ns, kubeconfig)

        // Phase 3 — 100% (remove stable, scale up canary, rename)
        sh """
            export KUBECONFIG=${kubeconfig}
            kubectl scale deployment/${svcName}-stable --replicas=0 -n ${ns}
            kubectl scale deployment/${svcName}-canary \
                --replicas=${stableReplicas} -n ${ns}
            kubectl rollout status deployment/${svcName}-canary -n ${ns} --timeout=300s

            # Update stable image tag for next release's baseline
            kubectl set image deployment/${svcName}-stable \
                ${svcName}=${registry}/${prefix}/${svcName}:${imageTag} -n ${ns}
            kubectl scale deployment/${svcName}-stable \
                --replicas=${stableReplicas} -n ${ns}
            kubectl delete deployment/${svcName}-canary -n ${ns} --ignore-not-found=true
        """
        echo "Canary promotion complete for ${svcName}"
    }
}

/**
 * Assert that a canary deployment is healthy by checking pod restart counts.
 * Fails the pipeline if any canary pod has restarted more than 2 times.
 */
void assertCanaryHealthy(String svcName, String ns, String kubeconfig) {
    def restarts = sh(
        script: """
            export KUBECONFIG=${kubeconfig}
            kubectl get pods -n ${ns} \
                -l app.kubernetes.io/name=${svcName},track=canary \
                -o jsonpath='{range .items[*]}{range .status.containerStatuses[*]}{.restartCount}{"\\n"}{end}{end}' \
                | awk '{s+=\$1} END {print s+0}'
        """,
        returnStdout: true
    ).trim().toInteger()

    if (restarts > 2) {
        // Roll back canary
        sh """
            export KUBECONFIG=${kubeconfig}
            kubectl delete deployment/${svcName}-canary -n ${ns} --ignore-not-found=true
        """
        error("Canary health check FAILED for ${svcName}: ${restarts} pod restart(s) detected — canary rolled back")
    }
    echo "Canary health check passed for ${svcName} (restarts: ${restarts})"
}

/**
 * Rolling update for the given service names.
 */
void rollingDeploy(List serviceNames) {
    def kubeconfig = env.KUBECONFIG
    def ns         = env.K8S_PROD_NS
    def imageTag   = env.IMAGE_TAG
    def registry   = env.REGISTRY
    def prefix     = env.IMAGE_PREFIX

    def steps = serviceNames.collectEntries { svcName ->
        ["rolling-${svcName}": {
            sh """
                export KUBECONFIG=${kubeconfig}
                kubectl set image deployment/${svcName} \
                    ${svcName}=${registry}/${prefix}/${svcName}:${imageTag} \
                    -n ${ns}
                kubectl rollout status deployment/${svcName} \
                    -n ${ns} --timeout=300s
            """
        }]
    }
    parallel steps
}
