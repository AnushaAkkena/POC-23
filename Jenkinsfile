
pipeline {
  agent { label 'docker' } // or any agent with Maven, Docker, AWS CLI

  environment {
    AWS_REGION        = 'ap-south-1'           
    AWS_ACCOUNT_ID    = '123456789012'         // your account id
    ECR_REPO_NAME     = 'cicd-demo'
    IMAGE_NAME        = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}"
    APP_VERSION       = "1.0.0-${env.BUILD_NUMBER}"
    // ECS clusters/services for review and prod
    ECS_CLUSTER_DEV   = 'cicd-demo-dev'
    ECS_SERVICE_DEV   = 'cicd-demo-svc-dev'
    ECS_CLUSTER_PROD  = 'cicd-demo-prod'
    ECS_SERVICE_PROD  = 'cicd-demo-svc-prod'
  }

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '30'))
    ansiColor('xterm')
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Unit Test') {
      steps {
        sh 'mvn -version'
        sh 'mvn -q clean verify'
      }
      post {
        always {
          junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
          archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
      }
    }

    stage('Login to AWS & ECR') {
      steps {
        withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
          sh """
            aws ecr describe-repositories --repository-names ${ECR_REPO_NAME} \
              || aws ecr create-repository --repository-name ${ECR_REPO_NAME}
            aws ecr get-login-password --region ${AWS_REGION} | \
              docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
          """
        }
      }
    }

    stage('Build & Push Image') {
      steps {
        script {
          def tag = "${IMAGE_NAME}:${APP_VERSION}"
          sh """
            docker build -t ${tag} .
            docker tag ${tag} ${IMAGE_NAME}:latest
            docker push ${tag}
            docker push ${IMAGE_NAME}:latest
          """
        }
      }
    }

    stage('Deploy Review App (DEV)') {
      when {
        expression { env.BRANCH_NAME != 'main' }  // feature/ branches
      }
      steps {
        withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
          sh """
            # Force ECS service in DEV to pull the new :latest image
            aws ecs update-service --cluster ${ECS_CLUSTER_DEV} --service ${ECS_SERVICE_DEV} --force-new-deployment
          """
        }
      }
    }

    stage('Approval for Production') {
      when { branch 'main' }
      steps {
        timeout(time: 10, unit: 'MINUTES') {
          input message: 'Review passed? Approve to deploy to PRODUCTION'
        }
      }
    }

    stage('Deploy to Production (PROD)') {
      when { branch 'main' }
      steps {
        withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
          sh """
            aws ecs update-service --cluster ${ECS_CLUSTER_PROD} --service ${ECS_SERVICE_PROD} --force-new-deployment
          """
        }
      }
    }
  }

  post {
    success {
      echo "Pipeline succeeded: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    }
    failure {
      echo "Pipeline failed: please check logs."
    }
  }
}
