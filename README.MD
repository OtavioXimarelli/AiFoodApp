# 🤖 AiFoodApp

## 📝 Descrição

O AiFoodApp é uma aplicação Java Spring Boot que demonstra a integração de um back-end com múltiplas APIs de geração de texto, incluindo a API da Maritalk e OpenAI, para gerar receitas e outras informações. Além disso, a aplicação gerencia itens alimentares utilizando um banco de dados H2. O projeto segue o padrão de arquitetura MVC para melhor organização e escalabilidade.

## ✨ Funcionalidades

### 🌟 Gerenciamento de Itens Alimentares

- Criação, listagem, atualização e exclusão de itens alimentares via API REST.
- Persistência de dados em banco de dados H2.

### 🌟 Geração de Receitas e Conteúdo

- Integração com APIs externas (Maritalk e OpenAI) para gerar receitas e outros tipos de conteúdo baseados em texto.
- Utilização de WebClient para comunicação com as APIs externas.
- Utilização do mesmo padrão de requisição para ambas as APIs.

### 🌟 Configuração de Ambiente Flexível

- Gerenciamento de variáveis de ambiente com Dotenv, facilitando a troca de URLs, chaves de API e modelos.
- Configuração centralizada do WebClient para chamadas às APIs externas.
- Facilidade de troca de modelo e API devido ao uso do mesmo padrão de requisição.

## 🛠️ Tecnologias Utilizadas

- **Java**: Linguagem de programação principal.
- **Spring Boot**: Framework para desenvolvimento rápido de aplicações Java.
- **Spring WebFlux**: Framework reativo para construção de aplicações web escaláveis.
- **H2 Database**: Banco de dados em memória para desenvolvimento e testes.
- **Flyway**: Gerenciamento de migrações de banco de dados.
- **Lombok**: Geração automática de código boilerplate.
- **Dotenv**: Gerenciamento de variáveis de ambiente.
- **Maven**: Gerenciamento de dependências e build.

## 📂 Persistência de Dados no H2

O projeto utiliza o banco de dados H2 para armazenar os dados e foi configurado para armazenamento persistente em arquivo. No entanto, por padrão, o H2 não persiste os dados entre reinicializações. Veja abaixo como configurar corretamente.

### Como acessar o console do H2

1. **Inicie a aplicação**
   ```
   ./mvnw spring-boot:run
   ```

2. **Acesse o console web do H2**
  - Abra um navegador e acesse: http://localhost:8080/h2-console

3. **Configurar a conexão**
  - Use as seguintes credenciais:
    - **JDBC URL**: `jdbc:h2:mem:testdb`
    - **User Name**: `sa`
    - **Password**: (deixe em branco)

4. **Executar consultas**
   ```sql
   SELECT * FROM FOOD_ITEM;
   ```

### Tornando os dados persistentes

Para manter os dados após reinicialização, altere o arquivo `application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/aitestapp
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

## 🗄️ Migrações de Banco de Dados com Flyway

O projeto utiliza Flyway para gerenciamento de migrações de banco de dados, garantindo que o esquema seja consistente em todos os ambientes.

### Estrutura das Migrações

As migrações estão localizadas em:
```
src/
└── main/
    └── resources/
        └── db/
            └── migration/
                ├── V1__create_food_item_table.sql
                ├── V2__add_nutrition_columns.sql
                └── ...
```

### Executando Migrações

As migrações são executadas automaticamente na inicialização da aplicação. Para executá-las manualmente:

```bash
./mvnw flyway:migrate
```

### Criando Novas Migrações

Para adicionar uma nova migração:

1. Crie um novo arquivo SQL na pasta `src/main/resources/db/migration/`
2. Nomeie o arquivo seguindo o padrão: `V{número}__{descrição}.sql`
3. Escreva as instruções SQL necessárias
4. Ao reiniciar a aplicação, o Flyway aplicará automaticamente as novas migrações

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── com/example/aitestapp/
│   │       ├── AiTestAppApplication.java
│   │       ├── controller/
│   │       │   ├── FoodItemController.java
│   │       │   └── RecipeController.java
│   │       ├── service/
│   │           ├── FoodItemService.java
│   │           └── TextGenService.java
│   └── resources/
│       ├── application.properties
│       └── db/
│           └── migration/
│               └── V1__create_food_item_table.sql
└── test/
    └── java/
        └── com/example/aitestapp/
            └── AiTestAppApplicationTests.java
```

## 💻 Instalação

### Pré-requisitos:

- Java JDK 11 ou superior.
- Maven.
- Git.

### Configuração e Instalação:

1. **Clone o repositório**:
   ```bash
   git clone https://github.com/OtavioXimarelli/AiAPP.git
   cd AiTestApp
   ```

2. **Configure as variáveis de ambiente no arquivo `.env`**:
   ```
   MARITALK_URL=url_da_maritalk
   MARITAL_KEY=sua_api_key_aqui
   MARI_MODEL=seu_modelo_aqui
   GPT_URL=url_da_openai
   GPT_MODEL=seu_modelo_aqui
   OPEN_AI_KEY=sua_open_ai_key_aqui
   ```

3. **Execute a aplicação**:
   ```bash
   ./mvnw spring-boot:run
   ```

## 📚 Uso

Acesse a aplicação em http://localhost:8080/.

Utilize os endpoints abaixo para gerenciar itens alimentares e gerar receitas.

### Endpoints da API

#### Geração de Receitas

- **Gerar Receita**
  - GET `/gen` - Retorna uma receita gerada por API externa.

  - **Atualmente o serviço de geração de receita com base nos itens salvos no banco de dado está em desenvolvimento :)**

#### Gerenciamento de Itens Alimentares

- **Criar item** (POST `/food/create`)
- **Listar todos os itens** (GET `/food/list`)
- **Listar item por ID** (GET `/food/list/{id}`)
- **Atualizar item** (PUT `/food/update`)

## 👥 Contribuições

Contribuições são bem-vindas! Abra um pull request ou relate problemas.

## ✉️ Licença

Este projeto está licenciado sob a MIT License. 