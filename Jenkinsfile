pipeline {
  agent any
  environment {
    SUPABASE_URL_PROD  = credentials('SUPABASE_URL_PROD')
    SUPABASE_KEY_PROD  = credentials('SUPABASE_KEY_PROD')
    SUPABASE_URL_TEST  = credentials('SUPABASE_URL_TEST')
    SUPABASE_KEY_TEST  = credentials('SUPABASE_KEY_TEST')
  }
  stages {
    stage('Checkout') {
      steps {
        echo 'Código obtido do GitHub!'
      }
    }
    stage('Testes Java') {
      steps {
        sh '''
          cd /home/ubuntu/java-bd/java-bd
          mvn test \
            -DSUPABASE_URL_PROD=$SUPABASE_URL_PROD \
            -DSUPABASE_KEY_PROD=$SUPABASE_KEY_PROD \
            -DSUPABASE_URL_TEST=$SUPABASE_URL_TEST \
            -DSUPABASE_KEY_TEST=$SUPABASE_KEY_TEST
        '''
      }
    }
    stage('Build Image') {
      steps {
        sh 'cd /home/ubuntu/java-bd/java-bd && docker build -t java-bd:latest .'
      }
    }
    stage('Import para K3s') {
      steps {
        sh 'docker save java-bd:latest | k3s ctr images import -'
      }
    }
    stage('Deploy no K3s') {
      steps {
        sh 'kubectl apply -f /home/ubuntu/java-bd/java-bd/k8s/'
      }
    }
  }
  post {
    success {
      echo 'Deploy realizado com sucesso! ✅'
    }
    failure {
      echo 'Pipeline falhou! Deploy cancelado. ❌'
    }
  }
}
