pipeline {
    agent any

    tools {
        maven 'Maven3'   // names must match Jenkins -> Manage Jenkins -> Tools
        jdk 'JDK21'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build & Test') {
            steps {
                sh 'mvn -B clean install'   // compiles + runs unit tests (Surefire)
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'web/target/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
        success {
            echo 'Build succeeded — artifact archived.'
        }
        failure {
            echo 'Build failed.'
        }
    }
}
