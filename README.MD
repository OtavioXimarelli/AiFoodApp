# AI Food App

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-23-orange)](https://oracle.com/java/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-0.8.0-blue)](https://spring.io/projects/spring-ai)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Latest-red)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker-Latest-2496ED)](https://www.docker.com/)
[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-3.8-2496ED)](https://docs.docker.com/compose/)
[![OAuth2](https://img.shields.io/badge/OAuth2-Google-4285F4)](https://developers.google.com/identity/protocols/oauth2)

---

## 📝 Sobre o Projeto

O **AI Food App** integra o gerenciamento inteligente de alimentos com IA para gerar receitas personalizadas e análises nutricionais. A autenticação é feita via Google OAuth2, garantindo segurança e praticidade.

---

## 📋 Sumário

- [Fluxo de Autenticação](#fluxo-de-autenticação)
- [Solução de Problemas](#solução-de-problemas)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitetura](#arquitetura)
- [Segurança e Autenticação](#segurança-e-autenticação)
- [Banco de Dados](#estrutura-do-banco-de-dados)
- [Implantação](#arquitetura-de-implantação)
- [Funcionalidades](#funcionalidades)
- [API Endpoints](#api-endpoints)
- [Como Executar](#como-executar-o-projeto)
- [Status Atual](#status-atual-do-projeto)
- [Próximos Passos](#próximos-passos)
- [Contribuições](#contribuições)
- [Licença](#licença)

---

## 🔐 Fluxo de Autenticação

1. **Iniciar Login**
   - Usuário clica no botão de login com Google.
   - Frontend redireciona para `/oauth2/authorization/google`.
   - Spring Security processa e redireciona para login do Google.

2. **Autorização Google**
   - Usuário autentica no Google.
   - Google retorna para `/login/oauth2/code/google` com o código.

3. **Processamento**
   - Spring Security processa o código, cria sessão e define o cookie `JSESSIONID`.
   - Usuário é redirecionado para a página inicial.

4. **Verificação**
   - Frontend consulta `/api/auth/status`.
   - Se autenticado, busca dados do usuário em `/api/auth`.

---

## 🛠️ Tecnologias Utilizadas

- **Spring Boot 3.5.4**: Backend Java
- **Spring Data JPA**: Persistência
- **Spring AI**: Integração IA
- **Maritaca AI**: API de receitas e nutrição
- **Spring Security & OAuth2**: Autenticação Google
- **Spring Session JDBC**: Sessões persistentes
- **PostgreSQL 15**: Banco relacional
- **Flyway**: Versionamento do banco
- **Lombok**: Menos boilerplate
- **RESTful API**: Integração backend
- **Docker & Docker Compose**: Containers e orquestração
- **Nginx**: Proxy reverso e SSL
- **AWS EC2**: Hospedagem

---

## 🏗️ Arquitetura

- **Controller**: Endpoints REST
- **Service**: Lógica de negócio
- **Repository**: Acesso a dados
- **Model**: Entidades do domínio
- **DTO & Mapper**: Transferência e conversão de dados
- **Config & Security**: Configurações gerais e autenticação
- **Filter**: Processamento de requisições (paths, rate limiting, diagnósticos)

---

## 🔒 Segurança e Autenticação

- **OAuth2 Google**: Login seguro, sem senha
- **Spring Session JDBC**: Sessões no banco
- **Cookie Seguro**: Configuração avançada (`secure`, `SameSite`, `domain`)
- **Rate Limiting**: Proteção contra brute force
- **Diagnóstico**: Monitoramento do fluxo de autenticação

### Exemplo de configuração (`application.yaml`)

```yaml
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

---

## 🗃️ Estrutura do Banco de Dados

- `tb_food_item`: Alimentos e informações nutricionais
- `tb_recipes`: Receitas geradas
- `tb_recipe_instructions`: Passos das receitas
- `tb_recipes_nutritional_info`: Nutrição das receitas
- `tb_recipe_ingredients`: Ingredientes de receitas
- `tb_users`: Usuários (OAuth2 e perfil)
- `spring_session`: Sessões persistidas

---

## 🌐 Arquitetura de Implantação

- **Frontend**: Site estático servido pelo Nginx
- **Backend**: Spring Boot em container Docker
- **Banco de Dados**: PostgreSQL em container Docker
- **Nginx**: Proxy reverso, SSL e API gateway
- **Docker Compose**: Orquestração de todos os serviços
- **AWS EC2**: Infraestrutura de hospedagem

---

## 🚀 Funcionalidades

### Autenticação Social
- Login com Google OAuth2
- Sessões persistentes (30 dias)
- Redirecionamentos seguros

### Gerenciamento de Alimentos
- CRUD completo
- Categorização e nutrição

### Geração de Receitas IA
- Receitas automáticas baseadas nos alimentos do usuário
- Instruções detalhadas
- Nutrição estimada

### Análise Nutricional
- Perfil nutricional das receitas
- Recomendações baseadas em necessidades dietéticas

---

## 🔌 API Endpoints

### Autenticação e Usuário
- `GET /oauth2/authorization/google` — Inicia login Google OAuth2
- `GET /login/oauth2/code/google` — Callback de autenticação
- `GET /api/auth/status` — Status de autenticação
- `GET /api/auth` — Dados do usuário autenticado
- `POST /api/auth/logout` — Logout

### Alimentos
- `POST /api/foods/create` — Criar alimento
- `GET /api/foods/list` — Listar alimentos
- `GET /api/foods/list/{id}` — Buscar alimento por ID
- `PUT /api/foods/update` — Atualizar alimento
- `DELETE /api/foods/delete/{id}` — Remover alimento

### Receitas
- `GET /api/recipes/gen` — Gerar receita com IA
- `GET /api/recipes/analyze/{id}` — Analisar nutrição da receita
- `GET /api/recipes/user` — Listar receitas do usuário

---

## 💻 Como Executar o Projeto

### Execução Local

1. Clone o repositório:
   ```bash
   git clone https://github.com/OtavioXimarelli/AiFoodApp.git
   cd AiFoodApp
   ```

2. Crie um arquivo `.env` na raiz com as variáveis necessárias.

3. Execute com Docker Compose:
   ```bash
   docker-compose up -d
   ```

### Implantação em Produção (AWS EC2)

1. Configure as credenciais AWS e acesso SSH à sua instância EC2.

2. Faça o upload dos arquivos do projeto e das variáveis de ambiente para a instância.

3. Execute os containers Docker na EC2 normalmente:
   ```bash
   docker-compose up -d
   ```

---

## 🧪 Status Atual do Projeto

- ✅ Estrutura completa
- ✅ Autenticação Google OAuth2
- ✅ Sessões persistentes
- ✅ Gerenciamento de alimentos (CRUD)
- ✅ Integração Maritaca AI
- ✅ Geração de receitas IA
- ✅ Análise nutricional
- ✅ Deploy automatizado AWS EC2
- ✅ HTTPS via Nginx

---

## 📋 Próximos Passos

- [ ] Melhorias na interface do usuário
- [ ] Recomendações personalizadas por perfil
- [ ] Sistema de favoritos/histórico
- [ ] Integração com múltiplos provedores de IA
- [ ] CI/CD e monitoramento aprimorados

---

## 🛠️ Solução de Problemas

### Autenticação

- **404 em `/oauth2/authorization/google`**: Verifique configuração do Nginx.
- **Cookies não persistentes**: Veja `SameSite` e domínio dos cookies.
- **401 Unauthorized**: Verifique logs para sessão/token.
- **Redirecionamentos Infinitos**: Revise configuração do proxy Nginx.

### Comandos Úteis

- Ver logs do backend: `docker logs aifoodapp`
- Testar configuração do Nginx: `sudo nginx -t`
- Aplicar nova config Nginx: `./update_nginx.sh`
- Reiniciar serviços: `docker-compose restart`

---

## 👥 Contribuições

Contribuições são bem-vindas! Abra issues e pull requests para melhorias.

---

## 📄 Licença

Este projeto está licenciado sob a [MIT License](LICENSE).
