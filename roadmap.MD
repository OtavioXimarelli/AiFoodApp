# Roadmap para MVP do AiFoodApp

## Fase 1: Fundação e Correção (Semanas 1-2)

### Corrigir problemas estruturais
- Resolver problemas no repositório FoodItemRepository
- Corrigir estrutura do serviço TextGenService
- Padronizar tratamento de erros da API

### Aprimorar o modelo de dados
- Adicionar campos nutricionais básicos ao modelo FoodItem
- Incluir propriedades para categorização de alimentos
- Definir relacionamentos entre entidades

## Fase 2: Funcionalidades Core de IA (Semanas 3-5)

### Implementar geração de receitas com base em alimentos disponíveis
- Aprimorar o TextGenService existente para gerar melhores receitas
- Criar interface para seleção de alimentos disponíveis
- Implementar filtros para preferências alimentares (vegetariano, sem glúten, etc.)

### Desenvolver análise nutricional básica
- Criar serviço de análise nutricional utilizando IA
- Desenvolver endpoints para solicitar análises personalizadas
- Implementar visualização clara de insights nutricionais

### Criar controller para funcionalidades de IA
- Desenvolver endpoints para solicitação de insights nutricionais
- Implementar rotas para geração de receitas personalizadas
- Estabelecer padrão de resposta e tratamento de erros

## Fase 3: Funcionalidades Essenciais Tradicionais (Semanas 6-8)

### Implementar sistema de categorização de alimentos
- Adicionar enums ou constantes para grupos alimentares
- Criar endpoints para listar alimentos por grupo
- Desenvolver interface para visualizar alimentos por categoria

### Desenvolver sistema básico de listas de compras
- Criar modelos para lista de compras e itens
- Implementar endpoints para gerenciamento de listas
- Desenvolver funcionalidade para adicionar ingredientes de receitas à lista

### Implementar tracking de macronutrientes
- Criar modelo de dados para armazenar consumo diário
- Desenvolver endpoints para registrar refeições
- Implementar visualização básica de macronutrientes consumidos

## Fase 4: Integração e Refinamento (Semanas 9-10)

### Integrar funcionalidades e melhorar UX
- Conectar geração de receitas com listas de compras
- Permitir compartilhamento de receitas geradas
- Refinar feedback de usuário e interfaces

### Implementar testes e otimizações
- Criar testes unitários para serviços principais
- Otimizar chamadas às APIs externas (caching, etc.)
- Identificar e resolver possíveis gargalos de performance

### Preparar para lançamento do MVP
- Documentação básica da API
- Configuração de ambiente de produção
- Configuração de monitoramento básico

## Fase 5: Lançamento MVP e Feedback Inicial (Semanas 11-12)

### Lançamento MVP
- Deploy da aplicação
- Configuração de CI/CD básico
- Preparação para coleta de feedback

### Coleta de feedback inicial
- Implementar mecanismo básico de coleta de feedback
- Preparar para o próximo ciclo de desenvolvimento

## Próximos Passos Pós-MVP (Futuro)

1. **Funcionalidades avançadas de IA**
   - Recomendação de substituições saudáveis
   - Assistente virtual de nutrição
   - Previsão de validade inteligente

2. **Expansões principais**
   - Sistema completo de planos alimentares
   - Integração com wearables
   - Scanner de código de barras
   - Compartilhamento social

3. **Refinamentos técnicos**
   - Arquitetura escalável para mais usuários
   - Sistema avançado de analytics
   - Personalização avançada baseada em ML