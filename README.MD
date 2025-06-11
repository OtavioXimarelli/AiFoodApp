# AI Food App

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-23-orange)](https://www.oracle.com/java/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-0.8.0-blue)](https://spring.io/projects/spring-ai)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Latest-red)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker-Latest-2496ED)](https://www.docker.com/)
[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-3.8-2496ED)](https://docs.docker.com/compose/)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/OtavioXimarelli/AiFoodApp)


## 📝 Sobre o Projeto

O AI Food App integra gerenciamento de alimentos com inteligência artificial para gerar receitas personalizadas e análises nutricionais. Agora, o sistema conta com autenticação de usuários e segurança reforçada via Spring Security e JWT.

## 🛠️ Tecnologias Utilizadas

- **Spring Boot**: Framework para aplicações Java
- **Spring Data JPA**: Persistência de dados
- **Spring AI**: Integração com APIs de IA
- **Maritaca AI**: API de IA para receitas e nutrição
- **Spring Security**: Autenticação e autorização (JWT)
- **PostgreSQL**: Banco de dados relacional
- **Flyway**: Controle de versão do banco
- **Lombok**: Redução de boilerplate
- **RESTful API**: Comunicação backend
- **Docker**: Containerização
- **Docker Compose**: Orquestração de containers
- **dotenv-java**: Carregamento de variáveis do `.env`

## 🏗️ Arquitetura

- **Controller**: Endpoints REST
- **Service**: Lógica de negócio
- **Repository**: Acesso a dados
- **Model**: Entidades do domínio
- **DTO**: Transferência de dados
- **Mapper**: Conversão entre entidades e DTOs
- **Config**: Configurações da aplicação
- **Security**: Lógica de autenticação e autorização

## 🔒 Segurança

- **Spring Security** protege todos os endpoints, exceto `/api/auth/login` e `/api/auth/register`
- **JWT**: Autenticação baseada em token para acesso à API
- **Variáveis Sensíveis**: Configuradas via arquivo `.env` (NUNCA commitado no repositório)

### Exemplo de `.env` (NÃO use valores reais em produção)

```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/seu_db 
DB_USERNAME=seu_usuario 
DB_PASSWORD=sua_senha

# Maritaca AI
MARITACA_API_URL=https://api.maritaca.ai/api/chat/completions 
MARITACA_API_KEY=sua_chave_api 
MARITACA_API_MODEL=modelo do maritaca
MARITACA_SYSTEM_PROMPT="Você é um assistente culinário"

# JWT
JWT_SECRET=seu_segredo_super_secreto_para_jwt
```

> **O arquivo `.env` está no `.gitignore` por padrão. Nunca compartilhe seus dados reais.**

## 🗃️ Estrutura do Banco de Dados

- **tb_food_item**: Alimentos
- **tb_food_items_tag**: Tags dos alimentos
- **tb_recipes**: Receitas
- **tb_recipe_instructions**: Passos das receitas
- **tb_recipes_nutritional_info**: Informações nutricionais
- **tb_recipe_ingredients**: Ingredientes das receitas
- **tb_user**: Usuários (autenticação)

## 🚀 Funcionalidades

### Gerenciamento de Alimentos
- CRUD de alimentos

### Geração de Receitas com IA
- Criação automática de receitas baseadas nos alimentos disponíveis
- Instruções detalhadas
- Informações nutricionais estimadas

### Análise Nutricional
- Análise do perfil nutricional das receitas

### Autenticação & Segurança
- Cadastro e login de usuários
- Endpoints protegidos por JWT

## 🔌 API Endpoints

### Autenticação
- `POST /api/auth/login`: Login (retorna JWT)
- `POST /api/auth/register`: Cadastro de usuário

### Alimentos (Food Items)
- `POST /api/foods/create`: Cria alimento
- `GET /api/foods/list`: Lista alimentos
- `GET /api/foods/list/{id}`: Busca alimento por ID
- `PUT /api/foods/update`: Atualiza alimento
- `DELETE /api/foods/delete/{id}`: Remove alimento

### Receitas (Recipes)
- `GET /api/recipes/gen`: Gera receita com IA
- `GET /api/recipes/analyze/{id}`: Analisa nutrição da receita

## 💻 Como Executar o Projeto

### Execução Local

1. Clone o repositório
   ```bash
   git clone https://github.com/seu-usuario/ai-food-app.git
   cd ai-food-app
   ```

2. Crie um arquivo `.env` na raiz do projeto (veja exemplo acima)

3. Execute o projeto
   ```bash
   ./mvnw spring-boot:run
   ```

### Execução com Docker

1. Clone o repositório
   ```bash
   git clone https://github.com/seu-usuario/ai-food-app.git
   cd ai-food-app
   ```

2. Crie o arquivo `.env` na raiz

3. Construa e execute com Docker Compose
   ```bash
   docker-compose up -d
   ```

## 🧪 Status Atual do Projeto

- ✅ Estrutura básica da aplicação
- ✅ Gerenciamento de alimentos (CRUD)
- ✅ Integração com Maritaca AI
- ✅ Geração de receitas com IA
- ✅ Análise nutricional
- ✅ Autenticação e segurança com JWT
- ✅ Configuração sensível via .env

## 📋 Próximos Passos

- 🔲 Interface de usuário (React)
- 🔲 Recomendações personalizadas
- 🔲 Favoritos/histórico
- 🔲 Integração AI aprimorada
- 🔲 CI/CD e monitoramento

## 👥 Contribuições

Contribuições são bem-vindas! Abra issues e pull requests.

## 📄 Licença


Este projeto está licenciado sob a [MIT License](LICENSE).

