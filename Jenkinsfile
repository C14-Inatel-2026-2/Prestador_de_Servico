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
             junit(
                 testResults: 'target/surefire-reports/TEST-*.xml',
                 allowEmptyResults: true
             )

             archiveArtifacts(
                 artifacts: 'target/site/jacoco/**',
                 allowEmptyArchive: true
             )

             echo 'Publicacao dos resultados finalizada.'
         }

         success {
             echo 'Build e testes executados com sucesso!'
         }

         failure {
             echo 'Build ou testes falharam. Confira os relatorios.'
         }
     }
}

