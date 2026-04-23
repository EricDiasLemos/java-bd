# 🌐 Java Web Server + Supabase

Servidor HTTP simples em Java puro com integração ao banco de dados Supabase. Projeto desenvolvido para aprendizado de fundamentos de Java, HTTP, Docker e testes automatizados.

---

## 📋 Funcionalidades

- Servidor HTTP na porta `8000` sem frameworks externos
- Formulário HTML para cadastro de **nome** e **cidade**
- Integração com **Supabase** via API REST
- Variáveis de ambiente com `.env`
- Containerização com **Docker** (multi-stage build)
- Testes automatizados com **JUnit 5**

---

## 🗂️ Estrutura do Projeto

```
meu-site/
├── public/
│   └── index.html          # Página servida pelo servidor
├── src/
│   ├── main/java/
│   │   └── Servidor.java   # Servidor HTTP principal
│   └── test/java/
│       └── ServidorTest.java  # Testes automatizados
├── .env                    # Credenciais (não sobe para o GitHub)
├── .env.example            # Exemplo das variáveis necessárias
├── .gitignore
├── dockerfile
└── pom.xml                 # Dependências Maven
```

---

## ⚙️ Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker
- Conta no [Supabase](https://supabase.com)

---

## 🚀 Como rodar localmente

**1. Clone o repositório**
```bash
git clone <url-do-repositorio>
cd meu-site
```

**2. Configure as variáveis de ambiente**
```bash
cp .env.example .env
```
Preencha o `.env` com suas credenciais do Supabase.

**3. Compile e rode**
```bash
mvn compile
mvn exec:java "-Dexec.mainClass=Servidor"
```

**4. Acesse no navegador**
```
http://localhost:8000
```

---

## 🐳 Rodando com Docker

```bash
# Build da imagem
docker build -t meu-site-java .

# Rodar em background
docker run -d -p 8000:8000 meu-site-java
```

---

## 🧪 Testes

```bash
mvn test
```

### Testes implementados

| Teste | Tipo | Descrição |
|---|---|---|
| `deveExtrairNomeDoJSON` | Unitário | Valida o parsing do JSON recebido |
| `deveReceberCodigo200` | Integração | Verifica se o servidor responde HTTP 200 |
| `conexaoBanco` | Integração | Verifica se o Supabase está acessível |
| `formularioServidor` | Integração | Valida se o servidor recebe os dados do formulário |
| `servidorBanco` | Integração | Valida se o dado é salvo no banco |
| `dadoDuplicado` | Integração | Verifica se dados duplicados são rejeitados com HTTP 409 |

---

## 🔐 Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```
SUPABASE_URL_PROD=
SUPABASE_KEY_PROD=

SUPABASE_URL_TEST=
SUPABASE_KEY_TEST=
```

---

## 📦 Dependências

| Biblioteca | Versão | Uso |
|---|---|---|
| OkHttp | 4.12.0 | Requisições HTTP para o Supabase |
| Gson | 2.11.0 | Parsing de JSON |
| dotenv-java | 3.0.0 | Leitura do arquivo `.env` |
| JUnit Jupiter | 5.10.0 | Testes automatizados |

---

## 📡 Rotas da API

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/` | Retorna a página HTML |
| `POST` | `/pessoas` | Cadastra nome e cidade no banco |

### Exemplo de requisição POST

```json
{
  "nome": "Eric",
  "cidade": "BH"
}
```

---

## 🗄️ Banco de Dados

Tabela `name` no Supabase:

```sql
CREATE TABLE name (
    id     SERIAL PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    cidade VARCHAR(100)
);

ALTER TABLE name
ADD CONSTRAINT unique_name_cidade UNIQUE (name, cidade);
```
