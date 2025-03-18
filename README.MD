# AiTestApp

## Descrição

AiTestApp é uma aplicação Java baseada em Spring Boot que permite a interação com uma API de geração de receitas. A aplicação utiliza o padrão de arquitetura MVC e integra-se com um banco de dados H2 para armazenar itens alimentares.

## Funcionalidades

- **Gerenciamento de Itens Alimentares**: Criação, listagem e atualização de itens alimentares.
- **Geração de Receitas**: Geração de receitas com base em ingredientes fornecidos pelo usuário, utilizando uma API externa.
- **Configuração de WebClient**: Configuração de um cliente web para interagir com a API de geração de receitas.

## Tecnologias Utilizadas

- Java
- Spring Boot
- Spring WebFlux
- H2 Database
- Lombok
- Dotenv para gerenciamento de variáveis de ambiente

## Estrutura do Projeto
src
├── main
│ ├── java
│ │ └── com
│ │ └── example
│ │ └── aitestapp
│ │ ├── AiTestAppApplication.java
│ │ ├── config
│ │ │ └── WebClientConfig.java
│ │ ├── controller
│ │ │ ├── FoodItemController.java
│ │ │ └── RecipeController.java
│ │ ├── model
│ │ │ └── FoodItem.java
│ │ ├── repository
│ │ │ └── FoodItemRepository.java
│ │ └── service
│ │ ├── FoodItemService.java
│ │ └── TextGenService.java
│ └── resources
│ └── application.properties
└── test
└── java
└── com
└── example
└── aitestapp
└── AiTestAppApplicationTests.java


## Instalação

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu_usuario/AiTestApp.git
   cd AiTestApp
   ```

2. Certifique-se de ter o Java JDK 11 ou superior instalado.

3. Configure as variáveis de ambiente necessárias em um arquivo `.env` na raiz do projeto:
   ```
   MARITALK_URL=seu_url_aqui
   MARITAL_KEY=seu_api_key_aqui
   MARI_MODEL=seu_modelo_aqui
   OPEN_AI_KEY=seu_open_ai_key_aqui
   ```

4. Execute a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

## Uso

- Acesse a aplicação em `http://localhost:8080`.
- Utilize os endpoints disponíveis para gerenciar itens alimentares e gerar receitas.

## Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir um *pull request* ou relatar problemas.

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).