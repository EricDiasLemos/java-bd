# ☕ java-bd — API Java com Pipeline CI/CD e Kubernetes

Aplicação Java com servidor HTTP nativo integrada ao Supabase, com pipeline CI/CD automatizado via Jenkins, Docker e Kubernetes (K3s).

---

## 🏗️ Arquitetura

```
EC2 c7i-flex.large (AWS)
├── K3s             → orquestra os containers
│   └── Pod java-bd → aplicação Java + variáveis do Secret
├── Docker          → build da imagem via pipeline
└── Jenkins         → CI/CD automatizado
    ├── Testes Java (Maven)
    ├── Docker build
    ├── Import para K3s
    └── kubectl apply
```

### Fluxo do Pipeline

```
git push (GitHub)
      ↓
GitHub Webhook notifica o Jenkins
      ↓
Jenkins cria .env temporário com credenciais
      ↓
Maven executa os testes Java
      ↓
.env temporário é removido
      ↓
Docker builda imagem (multi-stage)
      ↓
Imagem importada para o K3s
      ↓
kubectl aplica os manifests
      ↓
Pod sobe com variáveis do Kubernetes Secret ✅
```

---

## 🛠️ Tecnologias Utilizadas

### Java + HttpServer nativo
Servidor HTTP implementado com a biblioteca nativa do Java (`com.sun.net.httpserver`), sem dependência de frameworks externos como Spring. Recebe requisições POST e persiste dados no Supabase.

### Maven
Gerenciador de dependências e build do projeto Java. Responsável por compilar o código, baixar as dependências e executar os testes automatizados antes do deploy.

**Por que `-DskipTests` no Dockerfile?**
Os testes precisam das credenciais do Supabase, que não estão disponíveis durante o build da imagem Docker. Por isso os testes rodam antes, no Jenkins, com as credenciais injetadas de forma segura.

### Supabase
Banco de dados PostgreSQL como serviço. A aplicação persiste dados via API REST do Supabase, utilizando as credenciais injetadas pelo Kubernetes Secret em tempo de execução.

### Docker Multi-stage Build
O Dockerfile utiliza dois estágios:
- **Stage 1 (builder):** instala o Maven, compila o código e gera os artefatos
- **Stage 2 (runtime):** copia apenas os artefatos necessários para uma imagem menor e mais segura

```dockerfile
# Stage 1 — compilação
FROM eclipse-temurin:17 AS builder
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY . .
RUN mvn package -DskipTests

# Stage 2 — execução
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target /app/target
COPY --from=builder /app/public /app/public
COPY --from=builder /app/pom.xml /app/pom.xml
CMD ["java", "-cp", "target/classes:target/dependency/*", "Servidor"]
```

### Kubernetes Secrets
As credenciais do Supabase são armazenadas como Secrets no Kubernetes, nunca em arquivos do repositório. O pod recebe as variáveis de ambiente em tempo de execução via `secretKeyRef`.

**Por que Secrets e não variáveis diretas no deployment.yaml?**
- Credenciais nunca ficam expostas no repositório
- Valores encodados em base64 no cluster
- Padrão adotado por grandes empresas em produção

### Jenkins Credentials Store
As mesmas credenciais são cadastradas no cofre do Jenkins para serem usadas durante os testes Maven, sem nunca aparecerem em texto puro nos logs.

---

## 📁 Estrutura do Projeto

```
java-bd/
├── Dockerfile              # Multi-stage build
├── Jenkinsfile             # Pipeline CI/CD
├── pom.xml                 # Dependências Maven
├── .env.example            # Exemplo das variáveis necessárias
├── .gitignore
├── public/
│   └── index.html          # Frontend da aplicação
├── src/
│   ├── main/java/
│   │   └── Servidor.java   # Servidor HTTP + integração Supabase
│   └── test/java/
│       └── ServidorTest.java # Testes automatizados
└── k8s/
    ├── deployment.yaml     # Pod com variáveis do Secret injetadas
    └── service.yaml        # NodePort na porta 30081
```

---

## 🔐 Gerenciamento de Credenciais

### Variáveis necessárias

```env
SUPABASE_URL_PROD=
SUPABASE_KEY_PROD=
SUPABASE_URL_TEST=
SUPABASE_KEY_TEST=
```

### Criando o Secret no Kubernetes
```bash
kubectl create secret generic supabase-secrets \
  --from-literal=SUPABASE_URL_PROD='sua-url-prod' \
  --from-literal=SUPABASE_KEY_PROD='sua-key-prod' \
  --from-literal=SUPABASE_URL_TEST='sua-url-test' \
  --from-literal=SUPABASE_KEY_TEST='sua-key-test'
```

### Cadastrando no Jenkins
Em **Manage Jenkins → Credentials → Global → Add Credentials**:
- Kind: `Secret text`
- Cadastre as 4 variáveis com os mesmos IDs acima

---

## ⚙️ Como o Secret chega ao container

O `deployment.yaml` injeta as variáveis do Secret como variáveis de ambiente:

```yaml
env:
- name: SUPABASE_URL_PROD
  valueFrom:
    secretKeyRef:
      name: supabase-secrets
      key: SUPABASE_URL_PROD
```

O código Java lê via `System.getenv()`:

```java
String url = System.getenv("SUPABASE_URL_PROD");
String key = System.getenv("SUPABASE_KEY_PROD");
```

---

## 🧪 Testes Automatizados

Os testes rodam no Jenkins antes do build Docker, com acesso às credenciais via Jenkins Credentials Store. O Jenkins cria um `.env` temporário para os testes e o remove logo após.

```groovy
stage('Testes Java') {
  steps {
    sh '''
      cd /home/ubuntu/java-bd/java-bd
      cat > .env <<ENVFILE
SUPABASE_URL_PROD=$SUPABASE_URL_PROD
...
ENVFILE
      mvn test
      rm -f .env
    '''
  }
}
```

---

## 🚢 Deploy

A aplicação é exposta via **NodePort** na porta `30081`:

```
http://<IP-EC2>:30081
```

### Endpoints disponíveis

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/` | Retorna o frontend HTML |
| POST | `/pessoas` | Persiste nome e cidade no Supabase |

### Exemplo de requisição
```bash
curl -X POST http://<IP-EC2>:30081/pessoas \
  -H "Content-Type: application/json" \
  -d '{"nome": "Eric", "cidade": "Contagem"}'
```

---

## 🔄 Configuração do Pipeline

### Webhook GitHub
Em **Settings → Webhooks → Add webhook**:
- Payload URL: `http://<IP-EC2>:8080/github-webhook/`
- Content type: `application/json`
- Trigger: `Just the push event`

### Job no Jenkins
- Tipo: **Pipeline**
- Definition: **Pipeline script from SCM**
- Repository URL: `https://github.com/EricDiasLemos/java-bd.git`
- Branch: `*/main`
- Build Trigger: `GitHub hook trigger for GITScm polling`

---

## 📊 Monitoramento

```bash
# Ver status do pod
kubectl get pods

# Ver logs da aplicação
kubectl logs deployment/java-bd

# Reiniciar o deployment
kubectl rollout restart deployment/java-bd
```

---

## 📝 Lições Aprendidas

- A biblioteca `dotenv` procura um arquivo `.env` no disco — em produção com Kubernetes, o correto é usar `System.getenv()` para ler variáveis de ambiente injetadas pelo cluster
- O `mvn package -DskipTests` é necessário no Dockerfile porque as credenciais do Supabase não estão disponíveis durante o build da imagem
- O `deployment.yaml unchanged` no log do Jenkins não significa que o pod foi atualizado — é necessário `kubectl rollout restart` para forçar a recriação dos pods com a nova imagem
- A porta do `service.yaml` deve bater com a porta que a aplicação realmente escuta

---

*Projeto desenvolvido como estudo de DevOps com foco em CI/CD, segurança de credenciais e integração entre Jenkins, Docker e Kubernetes.*
