# ReNerd

**ReNerd** é um projeto de um aplicativo Android (Kotlin) desenvolvido para fins de aprimorar habiilidades técnicas. Ele demonstra a construção de um cliente móvel capaz de consumir, exibir e filtrar um catálogo de mídias a partir de uma API com um contrato de dados específico.

![renerd](https://github.com/user-attachments/assets/031c45ae-73c8-40ed-b201-1ed7f7fbb208)


**Aviso:** Este repositório contém apenas o código-fonte da aplicação. Ele não é um produto funcional pronto para uso, pois não inclui a API ou a base de dados necessárias para popular o conteúdo. O projeto deve ser analisado estritamente como uma demonstração de arquitetura e implementação técnica.

## Visão Geral do Projeto

O conceito central do ReNerd era criar uma interface de cliente robusta e agnóstica à fonte de conteúdo. A aplicação foi projetada para interagir com qualquer API que fornecesse um catálogo de mídias (como podcasts) seguindo uma estrutura JSON pré-definida. A implementação serve como um estudo de caso prático de engenharia de software para Android.

### Contrato de Dados da API (Estrutura Esperada)

A lógica do aplicativo é construída em torno de um modelo de dados principal, `EpisodeModel`. Toda a funcionalidade depende de uma fonte de dados que retorne objetos JSON compatíveis com esta estrutura Kotlin:

```kotlin
// Modelo principal que dita a estrutura dos dados esperados pela aplicação.
data class EpisodeModel(
    val id: Int,
    val title: String,
    val description: String,![renerd](https://github.com/user-attachments/assets/daf5b2b9-12d5-4162-9d7a-8e75334e0515)

    val subject: String,
    @SerializedName("published_at") val publishedAt: String,
    val guests: String, // Campo de texto com nomes para serem parseados
    val image: String,
    val thumbnails: Thumbnails,
    @SerializedName("audio_high") val audioHigh: String,
    // ... e outros campos conforme o modelo completo no código-fonte.
)

data class Thumbnails(
    @SerializedName("img-16x9-1210x544") val img16x91210x544: String,
    // ... outros formatos de imagem.
)

data class Category(
    val name: String,
    val slug: String,
    // ...
)
```
*Nota: Os modelos acima são um extrato simplificado para ilustrar o contrato.*

## Funcionalidades Demonstradas

As funcionalidades implementadas são um reflexo direto da manipulação dos campos do contrato de dados:

*   **Renderização de Lista:** A tela principal exibe uma lista de itens, utilizando os campos `image`/`thumbnails` e `title`.
*   **Sistema de Filtragem:** A aplicação demonstra lógicas de filtragem complexas baseadas em múltiplos critérios:
    *   **Data:** Utiliza o campo `publishedAt` para filtrar por ano.
    *   **Categoria:** Utiliza o campo `subject`.
    *   **Participantes:** Implementa um parser para o campo `guests` (string de nomes separados por vírgula).
*   **Busca Textual:** Uma implementação de busca em tempo real que consulta os campos `title` e `description`.
*   **Player de Mídia em Segundo Plano:** Demonstra o uso do **ExoPlayer** dentro de um `Foreground Service`, garantindo a reprodução de áudio contínua (URLs dos campos `audio_high`, `audio_medium`, etc.) mesmo quando o app não está em primeiro plano.

## Arquitetura e Decisões Técnicas

O projeto foi estruturado com foco em boas práticas de engenharia de software para criar uma base de código limpa, testável e de alto desempenho.

*   **Tecnologia Nativa (Kotlin):** A escolha foi por 100% Kotlin nativo para garantir a melhor performance e integração com o sistema operacional Android.
*   **Padrão Arquitetural MVP (Model-View-Presenter):**
    *   **Model (Repository):** Camada de abstração que define a interface para obtenção dos dados, desacoplando o resto do app da origem específica (rede ou cache).
    *   **View:** Interface passiva (Activity/Fragment) responsável unicamente pela exibição dos dados.
    *   **Presenter:** Contém a lógica de apresentação, tratando as interações do usuário e formatando os dados para a View.
*   **Persistência e Cache com SQLDelight:** Para o cache local de metadados, foi utilizado o SQLDelight. Esta biblioteca gera interfaces Kotlin type-safe a partir de queries SQL, oferecendo a performance do SQL bruto com a segurança de um ORM leve.
*   **Concorrência com Kotlin Coroutines:** Todas as operações de I/O (rede e banco de dados) são gerenciadas com Coroutines para evitar o bloqueio da thread principal e manter a UI responsiva.

## Objetivo do Portfólio

O propósito deste repositório é servir como:

1.  **Demonstração de Competência Técnica:** Exibir a habilidade de projetar e implementar um aplicativo Android complexo do zero.
2.  **Exemplo de Arquitetura Limpa:** Ilustrar a aplicação prática de padrões de design como MVP e o princípio de separação de responsabilidades.
3.  **Estudo de Caso de Tecnologias Chave:** Apresentar a implementação de bibliotecas e componentes essenciais do ecossistema Android, como ExoPlayer, Coroutines e SQLDelight, na solução de problemas do mundo real.

Este projeto é um retrato de uma etapa do meu desenvolvimento profissional e não se destina a ser distribuído ou utilizado por terceiros.
