# AI Food App

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-23-orange)](https://www.oracle.com/java/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-0.8.0-blue)](https://spring.io/projects/spring-ai)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Latest-red)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker-Latest-2496ED)](https://www.docker.com/)
[![Docker Compose](https://img.shields.io/badge/Docker%20Compose-3.8-2496ED)](https://docs.docker.com/compose/)

## 📝 Sobre o Projeto

O AI Food App é uma aplicação que integra gerenciamento de alimentos com inteligência artificial para gerar receitas personalizadas e análises nutricionais. O sistema permite que os usuários cadastrem alimentos disponíveis e recebam sugestões de receitas otimizadas baseadas nos ingredientes que possuem.

## 🛠️ Tecnologias Utilizadas

- **Spring Boot**: Framework para criação da aplicação Java
- **Spring Data JPA**: Para persistência e acesso a dados
- **Spring AI**: Integração com APIs de inteligência artificial
- **Maritaca AI**: API de IA para geração de receitas e análises nutricionais em português
- **PostgreSQL**: Banco de dados relacional
- **Flyway**: Controle de versão do banco de dados
- **Lombok**: Redução de código boilerplate
- **RESTful API**: Comunicação entre frontend e backend
- **Docker**: Containerização da aplicação e banco de dados
- **Docker Compose**: Orquestração de múltiplos containers

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas:

- **Controller**: Endpoints da API REST
- **Service**: Lógica de negócios
- **Repository**: Acesso a dados
- **Model**: Entidades do domínio
- **DTO**: Objetos de transferência de dados
- **Mapper**: Conversão entre entidades e DTOs
- **Config**: Configurações da aplicação

### Infraestrutura

O projeto está configurado para execução em containers Docker:

- **Dockerfile**: Define a imagem da aplicação baseada no Eclipse Temurin (JDK 23)
- **Docker Compose**: Orquestra a aplicação e o banco de dados PostgreSQL
- **Ambiente de Desenvolvimento**: Configurações específicas para desenvolvimento local
- **Ambiente de Produção**: Preparado para implantação em ambientes containerizados

## 🗃️ Estrutura do Banco de Dados

- **tb_food_item**: Armazena informações sobre os alimentos disponíveis
- **tb_food_items_tag**: Tags associadas aos alimentos
- **tb_recipes**: Informações básicas sobre as receitas
- **tb_recipe_instructions**: Passos para preparar as receitas
- **tb_recipes_nutritional_info**: Informações nutricionais das receitas
- **tb_recipe_ingredients**: Relacionamento entre receitas e ingredientes

## 🚀 Funcionalidades

### Gerenciamento de Alimentos
- Cadastro de novos alimentos com informações nutricionais
- Listagem de todos os alimentos disponíveis
- Atualização e exclusão de alimentos

### Geração de Receitas com IA
- Criação automática de receitas baseadas nos alimentos disponíveis
- Receitas focadas em praticidade, rapidez e saúde
- Instruções detalhadas passo a passo
- Informações nutricionais estimadas

### Análise Nutricional
- Análise do perfil nutricional das receitas
- Sugestões para uma dieta equilibrada

## 🔌 API Endpoints

### Food Items
- `POST /api/foods/create`: Cria um novo alimento
- `GET /api/foods/list`: Lista todos os alimentos
- `GET /api/foods/list/{id}`: Busca um alimento pelo ID
- `PUT /api/foods/update`: Atualiza um alimento existente
- `DELETE /api/foods/delete/{id}`: Remove um alimento

### Recipes
- `GET /api/recipes/gen`: Gera uma nova receita com base nos alimentos disponíveis
- `GET /api/recipes/analyze/{id}`: Analisa o perfil nutricional de uma receita

## 💻 Como Executar o Projeto

### Execução Local

1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/ai-food-app.git
cd ai-food-app
```

2. Configure as variáveis de ambiente no arquivo `application.properties`
```properties
# Configurações do banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5432/aifoodapp
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# Configurações da API Maritaca
maritaca.api.url=https://api.maritaca.ai/api/chat/completions
maritaca.api.key=sua_chave_api
maritaca.api.model=sabia-2-medium
maritaca.system.prompt=Você é um assistente de culinária especialista em nutrição e receitas saudáveis.
```

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

2. Construa a aplicação
```bash
./mvnw clean package
```

3. Execute com Docker Compose
```bash
docker-compose up -d
```

O Docker Compose irá:
- Iniciar um container PostgreSQL com o banco de dados `foodDB`
- Construir a imagem da aplicação usando o Dockerfile
- Iniciar a aplicação conectada ao banco de dados
- Expor a aplicação na porta 8080 do host

## 🧪 Status Atual do Projeto

O projeto encontra-se em fase de desenvolvimento, com as seguintes funcionalidades implementadas:

✅ Estrutura básica da aplicação  
✅ Gerenciamento de alimentos (CRUD completo)  
✅ Integração com a API Maritaca AI  
✅ Geração de receitas baseada nos alimentos disponíveis  
✅ Análise nutricional de receitas  
✅ Containerização com Docker e Docker Compose

**Em desenvolvimento:**  
🔄 Interface de usuário  
🔄 Recomendações personalizadas baseadas em preferências alimentares  
🔄 Sistema de favoritos e histórico de receitas  
🔄 Otimização da integração com a IA para melhorar as recomendações

## 📋 Próximos Passos

- Desenvolvimento de uma interface de usuário em React
- Implementação de autenticação e autorização
- Expansão das funcionalidades de análise nutricional
- Adição de filtros para restrições alimentares (vegetariano, sem glúten, etc.)
- Otimização da geração de receitas com base no histórico de preferências
- Configuração de CI/CD para deploy automático usando Docker
- Implementação de monitoramento e logging para os containers
- Otimização das configurações Docker para ambientes de produção

## 👥 Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e enviar pull requests.

## 📄 Licença

Este projeto está licenciado sob a [MIT License](LICENSE).