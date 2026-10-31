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
            steps {
                script {
                    // Ambiente de execução simulado: pasta fora do workspace que guarda a última versão aprovada
                    def destino = isUnix() ? "${env.JENKINS_HOME}/deploy/prestador-de-servicos" : "${env.JENKINS_HOME}\\deploy\\prestador-de-servicos"

                    if (isUnix()) {
                        sh "mkdir -p '${destino}'"
                        sh "cp target/Projeto_Eng_Software-1.0-SNAPSHOT.jar '${destino}/prestador-de-servicos.jar'"

                        // Executa o banco simulado apenas se a variável DB_URL estiver configurada
                        if (env.DB_URL) {
                            sh "java -jar '${destino}/prestador-de-servicos.jar'"
                        } else {
                            echo "DB_URL não configurada neste Jenkins. Smoke test pulado."
                        }
                    } else {
                        bat "if not exist \"${destino}\" mkdir \"${destino}\""
                        bat "copy /Y target\\Projeto_Eng_Software-1.0-SNAPSHOT.jar \"${destino}\\prestador-de-servicos.jar\""

                        // Executa o banco simulado apenas se a variável DB_URL estiver configurada
                        if (env.DB_URL) {
                            bat "java -jar \"${destino}\\prestador-de-servicos.jar\""
                        } else {
                            echo "DB_URL não configurada neste Jenkins. Smoke test pulado."
                        }
                    }

                    echo "Build #${env.BUILD_NUMBER} implantado em ${destino}"
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