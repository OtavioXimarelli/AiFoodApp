# AI Food App

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-23-orange)](http## 🔐 Fluxo de Autenticação

1. **Iniciar Login**:
   - Usuário clica no botão de login com Google
   - O frontend redireciona para `/oauth2/authorization/google`
   - Spring Security processa a requisição e redireciona para tela de login do Google

2. **Autorização Google**:
   - Usuário se autentica no Google
   - Google redireciona de volta para `/login/oauth2/code/google` com o código de autorização

3. **Processamento pela Aplicação**:
   - Spring Security processa o código e cria uma sessão
   - Cookies JSESSIONID são configurados
   - Usuário é redirecionado para a página inicial

4. **Verificação de Autenticação**:
   - O frontend verifica o status com `/api/auth/status`
   - Se autenticado, busca os dados do usuário com `/api/auth`

## 🔧 Solução de Problemas

### Problemas Comuns de Autenticação
- **404 em /oauth2/authorization/google**: Verifique a configuração do Nginx para este path
- **Cookies Não Persistentes**: Verifique as configurações de SameSite e Domínio nos cookies
- **401 Unauthorized**: Verifique os logs para problemas de sessão ou token
- **Redirecionamentos Infinitos**: Problemas na configuração do proxy Nginx

### Comandos Úteis
- Verificar logs do backend: `docker logs aifoodapp`
- Testar configuração Nginx: `sudo nginx -t`
- Aplicar nova configuração Nginx: `./update_nginx.sh`
- Reiniciar serviços: `docker-compose restart`cle.com/java/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-0.8.0-blue)](https://spring.io/projects/spring-ai)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Latest-red)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker-Latest-2496ED)](https://www.docker.com/)
[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-3.8-2496ED)](https://docs.docker.com/compose/)
[![OAuth2](https://img.shields.io/badge/OAuth2-Google-4285F4)](https://developers.google.com/identity/protocols/oauth2)


## 📝 Sobre o Projeto

O AI Food App integra gerenciamento de alimentos com inteligência artificial para gerar receitas personalizadas e análises nutricionais. O sistema conta com autenticação de usuários via Google OAuth2, persistência de sessões, e segurança reforçada via Spring Security, tudo implantado em um ambiente AWS EC2 com HTTPS.

## 🛠️ Tecnologias Utilizadas

- **Spring Boot 3.5.4**: Framework para aplicações Java
- **Spring Data JPA**: Persistência de dados
- **Spring AI**: Integração com APIs de IA
- **Maritaca AI**: API de IA para receitas e nutrição
- **Spring Security & OAuth2**: Autenticação e autorização (Google)
- **Spring Session JDBC**: Persistência de sessões
- **PostgreSQL 15**: Banco de dados relacional
- **Flyway**: Controle de versão do banco
- **Lombok**: Redução de boilerplate
- **RESTful API**: Comunicação backend
- **Docker**: Containerização
- **Docker Compose**: Orquestração de containers
- **Nginx**: Proxy reverso e SSL
- **AWS EC2**: Hospedagem em nuvem

## 🏗️ Arquitetura

- **Controller**: Endpoints REST e gerenciamento de requisições
- **Service**: Lógica de negócio e integração com serviços externos
- **Repository**: Acesso a dados
- **Model**: Entidades do domínio
- **DTO**: Transferência de dados
- **Mapper**: Conversão entre entidades e DTOs
- **Config**: Configurações da aplicação
- **Security**: Autenticação OAuth2, gerenciamento de sessões
- **Filter**: Processamento de requisições (correção de caminhos, limitação de taxa, diagnósticos)

## 🔒 Segurança e Autenticação

- **OAuth2 com Google**: Login social sem necessidade de senhas
- **Spring Session JDBC**: Persistência de sessões no banco de dados
- **Cookie Seguro**: Configuração avançada de cookies com secure, SameSite e domínio
- **Rate Limiting**: Proteção contra ataques de força bruta
- **Diagnósticos de Autenticação**: Monitoramento e depuração do fluxo de autenticação

### Principais Configurações

```yaml
# Configuração do Google OAuth2
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email

# Configuração de Sessões e Cookies
  session:
    store-type: jdbc
    jdbc:
      initialize-schema: never
    timeout: 2592000  # 30 dias
    cookie:
      secure: ${COOKIE_SECURE:true}
      same-site: ${COOKIE_SAME_SITE:lax}
      domain: ${COOKIE_DOMAIN:.aifoodapp.site}
```

## 🗃️ Estrutura do Banco de Dados

- **tb_food_item**: Alimentos e informações nutricionais
- **tb_recipes**: Receitas geradas
- **tb_recipe_instructions**: Passos das receitas
- **tb_recipes_nutritional_info**: Informações nutricionais das receitas
- **tb_recipe_ingredients**: Ingredientes das receitas
- **tb_users**: Usuários (OAuth2 e informações de perfil)
- **spring_session**: Tabelas de persistência de sessão

## 🌐 Arquitetura de Implantação

- **Frontend**: Site estático servido pelo Nginx
- **Backend**: Aplicação Spring Boot em container Docker
- **Banco de Dados**: PostgreSQL em container Docker
- **Nginx**: Proxy reverso, SSL e roteamento de API
- **Docker Compose**: Orquestração de todos os serviços
- **AWS EC2**: Infraestrutura de hospedagem

## 🚀 Funcionalidades

### Autenticação Social
- Login com Google OAuth2
- Persistência de sessões por 30 dias
- Fluxo de autenticação seguro com redirecionamentos

### Gerenciamento de Alimentos
- CRUD completo de alimentos
- Categorização e informações nutricionais

### Geração de Receitas com IA
- Criação automática de receitas baseadas nos alimentos disponíveis
- Instruções detalhadas passo a passo
- Informações nutricionais estimadas

### Análise Nutricional
- Análise do perfil nutricional das receitas
- Recomendações baseadas em necessidades dietéticas

## 🔌 API Endpoints

### Autenticação e Usuário
- `GET /oauth2/authorization/google`: Inicia o processo de login com Google OAuth2
- `GET /login/oauth2/code/google`: Callback após autenticação com Google
- `GET /api/auth/status`: Verifica status de autenticação
- `GET /api/auth`: Retorna informações do usuário autenticado
- `POST /api/auth/logout`: Encerra a sessão do usuário

### Alimentos (Food Items)
- `POST /api/foods/create`: Cria alimento
- `GET /api/foods/list`: Lista alimentos
- `GET /api/foods/list/{id}`: Busca alimento por ID
- `PUT /api/foods/update`: Atualiza alimento
- `DELETE /api/foods/delete/{id}`: Remove alimento

### Receitas (Recipes)
- `GET /api/recipes/gen`: Gera receita com IA
- `GET /api/recipes/analyze/{id}`: Analisa nutrição da receita
- `GET /api/recipes/user`: Lista receitas do usuário

## 💻 Como Executar o Projeto

### Execução Local

1. Clone o repositório
   ```bash
   git clone https://github.com/OtavioXimarelli/AiFoodApp.git
   cd AiFoodApp
   ```

2. Crie um arquivo `.env` na raiz do projeto com as variáveis necessárias

3. Execute com Docker Compose
   ```bash
   docker-compose up -d
   ```

### Implantação em Produção (AWS EC2)

1. Configure suas credenciais AWS e acesso SSH

2. Execute o script de implantação
   ```bash
   ./deploy.sh --auto
   ```

3. Para atualizar apenas a configuração do Nginx
   ```bash
   ./update_nginx.sh
   ```

## 🧪 Status Atual do Projeto

- ✅ Estrutura completa da aplicação
- ✅ Autenticação OAuth2 com Google
- ✅ Persistência de sessões
- ✅ Gerenciamento de alimentos (CRUD)
- ✅ Integração com Maritaca AI
- ✅ Geração de receitas com IA
- ✅ Análise nutricional
- ✅ Implantação automatizada em AWS EC2
- ✅ Configuração HTTPS com Nginx

## 📋 Próximos Passos

- 🔲 Melhorias na interface do usuário
- 🔲 Recomendações personalizadas por perfil
- 🔲 Sistema de favoritos/histórico
- 🔲 Integração com múltiplos provedores de IA
- 🔲 CI/CD e monitoramento aprimorado

## � Solução de Problemas

### Problemas Comuns de Autenticação
- **Cookies Não Persistentes**: Verifique as configurações de SameSite e Domínio nos cookies
- **401 Unauthorized**: Verifique os logs para problemas de sessão ou token
- **Redirecionamentos Infinitos**: Problemas na configuração do proxy Nginx

### Comandos Úteis
- Verificar logs do backend: `docker logs aifoodapp`
- Testar configuração Nginx: `sudo nginx -t`
- Reiniciar serviços: `docker-compose restart`

## 👥 Contribuições

Contribuições são bem-vindas! Abra issues e pull requests para melhorar o projeto.

## 📄 Licença

Este projeto está licenciado sob a [MIT License](LICENSE).
