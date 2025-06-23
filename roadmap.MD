# Roadmap de Sprints para o AiFoodApp

## Sprint 1: Qualidade, Estabilidade e Refinamento da Base

**Objetivo da Sprint:** Fortalecer a fundação do projeto, garantindo que as funcionalidades existentes sejam confiáveis e robustas antes de adicionar novos recursos. Esta sprint foca em testes e melhorias técnicas críticas.

### Implementar Cobertura de Testes Unitários e de Integração

*   **Descrição:** Criar um conjunto sólido de testes automatizados para validar a lógica de negócio e os endpoints da API. Atualmente, o projeto carece de testes significativos.
*   **Tarefas:**
   *   **Testes de Serviço:** Escrever testes unitários para `FoodItemService`, `RecipeService` e `AuthorizationService`, utilizando mocks para isolar a lógica de negócio.
   *   **Testes de Controller:** Implementar testes de integração usando `MockMvc` para `FoodItemController`, `RecipeController` e `AuthenticationController` para validar as requisições, respostas HTTP e segurança dos endpoints.

### Refatorar o Parsing de Receitas da IA

*   **Descrição:** Substituir o método atual de parsing de texto no `ChatService`, que é frágil, por um método mais confiável baseado em JSON.
*   **Tarefas:**
   *   Atualizar o prompt enviado à Maritaca AI para exigir que a resposta da receita seja sempre em formato JSON estruturado.
   *   Modificar o método `parseRecipeFromText` em `ChatService` para usar uma biblioteca (como Jackson) para desserializar a string JSON diretamente para o objeto `Recipe` ou um DTO correspondente.

### Implementar Cache para Análise Nutricional

*   **Descrição:** Reduzir o número de chamadas repetitivas à API de IA para receitas que já foram analisadas, melhorando a performance e reduzindo custos.
*   **Tarefas:**
   *   Habilitar o suporte a cache do Spring (`@EnableCaching`).
   *   Anotar o método `analyzeNutritionalProfile` no `ChatService` com `@Cacheable` para que os resultados sejam armazenados em cache com base no ID da receita.

### Associar Receitas Geradas ao Usuário

*   **Descrição:** Vincular cada receita gerada ao usuário que a solicitou. Isso é fundamental para funcionalidades futuras como o histórico de receitas.
*   **Tarefas:**
   *   Adicionar um relacionamento Many-to-One da entidade `Recipe` para a entidade `User`.
   *   Atualizar a lógica em `RecipeController` e `RecipeService` para identificar o usuário autenticado e associá-lo à nova receita ao salvá-la.

---

## Sprint 2: Aprimoramento da IA e Categorização

**Objetivo da Sprint:** Tornar a interação com a IA mais poderosa e útil para o usuário, e implementar funcionalidades de organização de alimentos.

### Adicionar Filtros Avançados na Geração de Receitas

*   **Descrição:** Permitir que o usuário personalize a geração de receitas com base em preferências alimentares, ingredientes específicos e outras restrições.
*   **Tarefas:**
   *   Modificar o endpoint `GET /api/recipes/gen` para aceitar `RequestParams`, como `dietaryPreference` (ex: "vegetariano", "vegano") e `excludeIngredients` (lista de nomes de ingredientes a serem evitados).
   *   Aprimorar o método `generateRecipe` no `ChatService` para incorporar dinamicamente esses filtros no prompt enviado para a IA.

### Criar Endpoint para Listar Alimentos por Categoria

*   **Descrição:** Expor um endpoint que permita ao cliente (frontend) buscar e exibir alimentos agrupados por sua categoria (`FoodGroup`).
*   **Tarefas:**
   *   Adicionar o método `List<FoodItem> findByFoodGroup(FoodGroup foodGroup);` na interface `FoodItemRepository`.
   *   Implementar um novo método no `FoodItemService` para chamar o repositório.
   *   Criar um novo endpoint no `FoodItemController`, como `GET /api/foods/by-category`, que aceita a categoria como parâmetro.

---

## Sprint 3: Sistema de Listas de Compras

**Objetivo da Sprint:** Desenvolver a funcionalidade completa de listas de compras, um recurso essencial do aplicativo.

### Modelar e Implementar a Estrutura da Lista de Compras

*   **Descrição:** Criar a base de dados e as classes de domínio para suportar as listas de compras.
*   **Tarefas:**
   *   Criar as entidades JPA `ShoppingList` (com relacionamento para `User`) e `ShoppingListItem` (com relacionamento para `ShoppingList` e `FoodItem`).
   *   Gerar as migrações do banco de dados correspondentes usando Flyway.
   *   Implementar os `JpaRepository` para as novas entidades.

### Desenvolver o Serviço e os Endpoints para Gerenciamento da Lista

*   **Descrição:** Construir a lógica de negócio e a API para que o usuário possa interagir com suas listas.
*   **Tarefas:**
   *   Criar o `ShoppingListService` com métodos para: criar/buscar a lista de um usuário, adicionar/remover/atualizar itens.
   *   Desenvolver o `ShoppingListController` com endpoints RESTful para todas as operações do serviço.

---

## Sprint 4: Integração e Experiência do Usuário (UX)

**Objetivo da Sprint:** Conectar as funcionalidades existentes para criar uma experiência de usuário coesa e fluida.

### Integrar Receitas com a Lista de Compras

*   **Descrição:** Criar uma funcionalidade "one-click" para que o usuário possa adicionar todos os ingredientes de uma receita diretamente em sua lista de compras.
*   **Tarefas:**
   *   Criar um novo endpoint, como `POST /api/recipes/{recipeId}/add-to-shopping-list`.
   *   Implementar a lógica no `RecipeService` ou `ShoppingListService` que busca os ingredientes da receita (`RecipeIngredient`) e os adiciona na lista de compras do usuário autenticado.

### Desenvolver Histórico de Receitas do Usuário

*   **Descrição:** Criar uma tela ou endpoint onde o usuário possa ver todas as receitas que ele gerou anteriormente.
*   **Tarefas:**
   *   Criar um novo método no `RecipeRepository` para buscar receitas por ID de usuário (`findByUserId`).
   *   Expor um endpoint `GET /api/recipes/my-history` no `RecipeController` que retorna a lista de receitas para o usuário logado.

---

## Próximos Passos (Pós-MVP)

*   Desenvolver Sistema de Tracking de Macronutrientes
*   Funcionalidades Avançadas de IA (ex: substituições saudáveis, planejamento de refeições)
*   Construção da Interface de Usuário (Frontend)