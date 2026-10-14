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
    }
    post {
        success { echo 'Build OK' }
        failure { echo 'Build falhou' }
    }
}