pipeline {
    agent any
    tools { maven 'Maven3' }
    stages {
        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B clean package -DskipTests'
                    } else {
                        bat 'mvn -B clean package -DskipTests'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B verify'
                    } else {
                        bat 'mvn -B verify'
                    }
                }
            }
        }

        stage('Cobertura') {
            steps {
                script {
                    // Reprova o build se a cobertura de linhas ficar abaixo do mínimo definido no pom.xml
                    if (isUnix()) {
                        sh 'mvn -B jacoco:check'
                    } else {
                        bat 'mvn -B jacoco:check'
                    }
                }
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                script {
                    // Aciona o webhook do Render para fazer o deploy na nuvem automaticamente
                    if (env.RENDER_DEPLOY_HOOK) {
                        echo "Gatilho do Render detectado! Avisando a nuvem para iniciar o deploy da nova versão..."

                        if (isUnix()) {
                            sh "curl -fsS -X POST ${env.RENDER_DEPLOY_HOOK}"
                        } else {
                            bat "curl -fsS -X POST ${env.RENDER_DEPLOY_HOOK}"
                        }

                        echo "Sinal de deploy enviado com sucesso. O Render construirá a imagem a partir do Dockerfile."
                    } else {
                        error "Variável RENDER_DEPLOY_HOOK não configurada neste Jenkins."
                    }
                }
            }
        }
    }

    post {
        always {
            // Publica os resultados dos testes JUnit
            junit(
                    testResults: 'target/surefire-reports/TEST-*.xml',
                    allowEmptyResults: true
            )

            // Publica a cobertura de código do JaCoCo
            recordCoverage(
                    tools: [[
                                    parser: 'JACOCO',
                                    pattern: 'target/site/jacoco/jacoco.xml'
                            ]]
            )

            // Mantém os arquivos HTML e XML disponíveis
            archiveArtifacts(
                    artifacts: 'target/site/jacoco/**',
                    allowEmptyArchive: true
            )
        }

        success {
            echo 'Build e testes executados com sucesso!'
        }

        failure {
            echo 'Build ou testes falharam. Confira os relatórios.'
        }
    }
}