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

