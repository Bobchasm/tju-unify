# TJU-Unify Agent Guide

Multi-package campus-life app: Vue 3 frontend (`front/`), Python FastAPI agent (`tian-agent/`), Spring Cloud backend (`unify-conv/`).

## Repository Layout

| Directory | Role | Stack |
|-----------|------|-------|
| `front/` | Mobile-first web frontend | Vue 3, Vite, Vue Router, Axios |
| `tian-agent/` | AI campus agent service | Python 3.10, FastAPI, LangChain, Chroma |
| `unify-conv/` | Gateway + microservices | Spring Boot 3.1.5, Java 17, Maven, Nacos, MyBatis-Plus, MySQL |
| `docs/` | Documentation (Chinese) | — |

## Frontend (`front/`)

- **Install**: `cd front && npm install`
- **Dev**: `npm run dev` → `http://localhost:5173` (binds `0.0.0.0`)
- **Build**: `npm run build`
- **Preview**: `npm run preview`
- **Proxy rules** (`vite.config.js`):
  - `/api` → `http://localhost:7070`
  - `/unify-api` → `http://localhost:7070`
- **Agent API is NOT proxied**: `src/api/agent.js` hardcodes `http://localhost:8000` for agent chat calls. If you change the agent port, update this file too.
- **Mobile-only UI**: max-width 480px; test with mobile dev-tools.

## Agent Service (`tian-agent/`)

- **Start**: `cd tian-agent && python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload`
- **API docs**: `http://localhost:8000/docs`
- **Install deps**: `pip install -r requirements.txt`
- **Config files** (`config/`): `agent.yml`, `retrieval.yml`, `rag.yml`, `chroma.yml`, `prompts.yml`
- **Persistent runtime data** (do not commit):
  - `data/chat_history/` — full message archives per session
  - `data/conversation_memory/` — summarized memory per session
  - `chroma_db/` — vector store
- **Auth flow**: frontend JWT is passed in `bearer_token` field on chat requests; agent injects it into `utils/unify_api_context.py` before calling `/unify-api/**` on the gateway.
- **Model source**: DashScope (Alibaba) via `model/factory.py`; requires compatible env/API key.
- **Pyright config**: `pyrightconfig.json` targets Python 3.10; excludes `chroma_db`, `__pycache__`, `.venv`.

## Spring Backend (`unify-conv/`)

- **Build / install**: `cd unify-conv && mvn install` (tests are skipped by default: `skipTests=true`)
- **Module boot order quirk**: if startup fails with class-not-found or dependency errors, install `conv-common` then `conv-api` manually before starting other services:
  ```bash
  cd unify-conv/conv-common && mvn install
  cd ../conv-api && mvn install
  ```
- **Modules**:
  - `conv-gateway` — API gateway (port implied 7070 from agent config)
  - `conv-news`, `conv-transaction`, `conv-memo`, `conv-errand` — business services
  - `conv-common` — shared deps/utilities (required by all services)
  - `conv-api` — Feign clients (inner + outer)
- **Conventions for new modules**:
  - Module name prefix: `conv-`
  - Service name prefix: `unify-`
  - Base package: `com.tju.unify.conv.<module>`
  - Must depend on `conv-common`; use `conv-api` if calling other services.
  - Must include `spring-cloud-starter-alibaba-nacos-discovery` and `@EnableDiscoveryClient`.
  - Register routes in `conv-gateway/src/main/resources/application.yml` under `spring.cloud.gateway.routes`.
  - Add public (no-auth) paths to `unify.auth.excludePaths` in gateway config.
- **Database**: MySQL on external server; new DB schemas should have their creation SQL saved under `unify-conv/sql/`.
- **ORM**: MyBatis-Plus (not plain MyBatis). Use its built-in pagination instead of custom pagination interceptors.
- **Auth / Users**: unified with an external e-commerce platform (shared DB + shared JWT). Gateway handles auth; user service is external.
- **Java version**: 17
- **Spring Cloud versions**: Boot 3.1.5, Cloud 2022.0.4, Alibaba 2022.0.0.0

## Port Map (Development)

| Service | Port |
|---------|------|
| Frontend dev server | 5173 |
| Gateway / unify-api | 7070 |
| Agent API | 8000 |

## Cross-Origin Notes

- Agent (`main.py`) sets `CORSMiddleware` with `allow_origins=["*"]`.
- Frontend dev proxy handles CORS for `/api` and `/unify-api` only; direct agent calls rely on the agent’s CORS middleware.

## Testing / CI

- No CI workflows, unit tests, or lint configs are present in this repo.
- Maven tests are skipped by default (`skipTests=true`).

## Git / Env

- `.gitignore` excludes: `target/`, `node_modules/`, `front/dist/`, `**/logs/`, `.env` files, IDE dirs.
- Do not commit `tian-agent/data/`, `chroma_db/`, or any `.env` files.
