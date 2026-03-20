<p align="center">
  <img src="https://raw.githubusercontent.com/mateusfilpo/pokemart/main/images/cover.png" alt="PokéMart Banner" width="100%" />
</p>

<h1 align="center">☕ PokéMart API — Backend E-Commerce Pokémon</h1>

<p align="center">
  <em>High-Performance Hexagonal Backend</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4" />
  <img src="https://img.shields.io/badge/Neo4j-008CC1?style=for-the-badge&logo=neo4j&logoColor=white" alt="Neo4j" />
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=fff" alt="Docker" />
  <img src="https://img.shields.io/badge/GCP-4285F4?style=for-the-badge&logo=googlecloud&logoColor=fff" alt="GCP" />
</p>

<p align="center">
  <br>
  <strong>🔗 Acesse o projeto online:</strong><br>
  <a href="https://pokemart.filpo.com.br">https://pokemart.filpo.com.br</a>
  <br><br>
  <strong>🎨 Repositório do Frontend:</strong><br>
  <a href="https://github.com/mateusfilpo/pokemart">https://github.com/mateusfilpo/pokemart</a>
</p>

---

## 📖 Sobre o Projeto

O **PokéMart API** é o núcleo de processamento do ecossistema PokéMart. Desenvolvido com as tecnologias mais modernas do ecossistema Java, o projeto foca em **escalabilidade, resiliência e manutenibilidade**, servindo como um showcase de padrões arquiteturais avançados e engenharia de software de alta qualidade.

- 🏗️ **Arquitetura Hexagonal (Clean)** — Isolamento total das regras de negócio contra tecnologias externas.
- 🔗 **Persistência em Grafo** — Modelagem complexa de relacionamentos entre Treinadores e Itens via Neo4j.
- ⚡ **Caching Distribuído** — Performance extrema com Redis para catálogo e estatísticas.
- 🔐 **Segurança Avançada** — Autenticação Stateless via JWT com Cookies HttpOnly e RBAC.
- 📊 **Observabilidade Industrial** — Stack completa com Prometheus e Grafana para monitoramento em tempo real.

---

## ✨ Funcionalidades Principais

### 🏹 Core Engine (Hexagonal)
| Recurso | Descrição |
|---|---|
| 🏛️ Ports & Adapters | Domínio puro, sem dependências de frameworks, garantindo testabilidade. |
| 🗺️ Object Mapping | Mapeadores performáticos e sem estado (Lombok @UtilityClass). |
| 🛡️ Service Layer | Orquestração de casos de uso com validação rigorosa de dados. |

### 🔐 Segurança & Controle
| Recurso | Descrição |
|---|---|
| 🔑 Auth JWT | Fluxo seguro com expiração de tokens e persistência segura em cookies. |
| 👤 RBAC | Controle de acesso baseado em roles (`ADMIN` vs `USER`). |
| 🚦 Rate Limiting | Proteção contra abusos via Redis (Bucket4j) por IP e por Endpoint. |

### 💾 Dados & Performance
| Recurso | Descrição |
|---|---|
| 📉 Neo4j Graph DB | Consultas poderosas em grafos para modelagem de histórico e relacionamentos. |
| 🗄️ Migrations | Versionamento de esquema Neo4j integrado ao ciclo de vida da aplicação. |
| 🚀 Redis Cache | Estratégia de cache Side-Aside para redução brutal de latência no catálogo. |

### 📊 DevDocs & Observabilidade
| Recurso | Descrição |
|---|---|
| 📑 OpenAPI 3 / Swagger | Documentação interativa e contratos de API sempre atualizados. |
| 📈 Prometheus Metrics | Exposição de métricas via Micrometer/Actuator. |
| 🖼️ Grafana Dashboards | Visualização de estatísticas da JVM, throughput e saúde do sistema. |

---

## 🛠️ Tecnologias Utilizadas

<table>
  <tr>
    <th>Categoria</th>
    <th>Tecnologia</th>
    <th>Papel no Projeto</th>
  </tr>
  <tr>
    <td><strong>Linguagem</strong></td>
    <td><img src="https://img.shields.io/badge/Java%2025-ED8B00?style=flat-square&logo=openjdk&logoColor=white" /></td>
    <td>Últimas features de estabilidade e performance da JVM</td>
  </tr>
  <tr>
    <td><strong>Framework</strong></td>
    <td><img src="https://img.shields.io/badge/Spring%20Boot%204-6DB33F?style=flat-square&logo=springboot&logoColor=white" /></td>
    <td>Base da aplicação e injeção de dependências</td>
  </tr>
  <tr>
    <td><strong>Database</strong></td>
    <td><img src="https://img.shields.io/badge/Neo4j-008CC1?style=flat-square&logo=neo4j&logoColor=white" /></td>
    <td>Persistência principal e relacionamentos em grafo</td>
  </tr>
  <tr>
    <td><strong>Cache</strong></td>
    <td><img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white" /></td>
    <td>Cache de alta performance e Rate Limiting distribuído</td>
  </tr>
  <tr>
    <td><strong>Testes</strong></td>
    <td><img src="https://img.shields.io/badge/JUnit5--25A162?style=flat-square&logo=junit5&logoColor=white" /></td>
    <td>Testes unitários e de integração (100% Code Coverage)</td>
  </tr>
  <tr>
    <td><strong>Observabilidade</strong></td>
    <td><img src="https://img.shields.io/badge/Grafana-F46800?style=flat-square&logo=grafana&logoColor=white" /></td>
    <td>Dashboards e monitoramento de métricas</td>
  </tr>
</table>

---

## 🚀 Como Rodar localmente

### Pré-requisitos
- **Java 25** + **Maven 3.9+**
- **Docker** & **Docker Compose**

### Instalação (Via Docker - Recomendado)

```bash
# 1. Clone o repositório
git clone https://github.com/mateusfilpo/pokemart-api.git
cd pokemart-api

# 2. Suba a infraestrutura completa (DB, Cache, Admin, Prometheus, Grafana)
docker-compose up -d

# 3. Acesse a documentação Swagger
http://localhost:8080/swagger-ui.html
```

### Rodando Testes
```bash
./mvnw clean test
```
*Os testes utilizam **Testcontainers**, garantindo que as integrações com Neo4j e Redis funcionem exatamente como em produção.*

---

## 🗺️ Roadmap de Evolução

- [x] **Arquitetura Base:** Java 25 + Spring Boot 4 + Hexagonal.
- [x] **Persistência:** Integração Neo4j com Migrations automáticas.
- [x] **Resiliência:** Rate Limiting via Bucket4j/Redis.
- [x] **DevOps:** CI/CD via GitHub Actions e Deploy em Cloud (GCP).
- [x] **Observabilidade:** Monitoramento real com Prometheus e Grafana Dashboard.
- [x] **Performance:** Virtual Threads habilitadas para alta concorrência.

> [!NOTE]
> A implementação completa de Observabilidade (Spring Actuator, Prometheus e Grafana Dashboards) foi desenvolvida e testada com sucesso na branch `feature/observability`. Ela não está incluída no deploy de produção da branch `main` devido a restrições de memória (Free Tier) da instância e2-micro no GCP.

---

## 👨‍💻 Autor

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/mateusfilpo.png" width="120" alt="Foto de perfil" style="border-radius: 50%;" />
      <br />
      <strong>Mateus Filpo</strong>
      <br />
      <em>Desenvolvedor Full Stack</em>
      <br /><br />
      <a href="https://linkedin.com/in/mateusfilpo">
        <img src="https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=fff" />
      </a>
      <a href="https://github.com/mateusfilpo">
        <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=fff" />
      </a>
    </td>
  </tr>
</table>

---

<p align="center">
  <img src="https://img.shields.io/badge/%E2%9A%A1%20Desenvolvido%20com-Spring%20Boot%204-6DB33F?style=for-the-badge" />
  <br /><br />
  <em>⭐ Se este projeto te ajudou ou te inspirou, deixe uma estrela!</em>
</p>
