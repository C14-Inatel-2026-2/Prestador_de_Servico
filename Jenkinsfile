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

