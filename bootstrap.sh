#!/usr/bin/env bash
# A) Portada
#
# Entrega: Prompt único “ELITE + WEB” (copiar/pegar) para generar un bootstrap.sh que construya una Creator App multi-tenant world-class con AI Gateway multimodal y multi-proveedor (OpenAI/Claude/xAI/Gemini + muchos más), seguridad, observabilidad, SDK tipado y acceptance gates.
# Fecha (UTC): 2026-01-10T00:00:00Z
# Fuentes (obligatorias):
#
# [Fuente: sistema · chat_user_input · 2026-01-10T00:00:00Z]
#
# B) Resumen Ejecutivo
#
# Has autorizado web. Con eso, el prompt único que te dejo obliga al generador a:
#
# Investigar solo en documentación oficial para confirmar endpoints, auth y model IDs de proveedores (evita roturas).
#
# Integrar un AI Gateway con adaptadores por proveedor, routing por tarea, fallbacks, presupuestos/cuotificación y auditoría.
#
# Soportar multimodal: texto + imagen (incluye “Nano Banana” de Gemini) + video (Sora/OpenAI) + audio (TTS/STT). [Fuente: web · turn0search7 · 2026-01-10T00:00:00Z]
#
# Añadir proveedores con compatibilidad OpenAI (reduce complejidad): xAI, Groq, vLLM, Ollama, Cerebras, NVIDIA NIM, OpenRouter, etc. [Fuente: web · turn0search2 · 2026-01-10T00:00:00Z]
#
# Opcionalmente, soportar gateways tipo LiteLLM Proxy y Helicone AI Gateway para llegar a “100+ modelos” con 1 interfaz. [Fuente: web · turn6search3 · 2026-01-10T00:00:00Z]
#
# C) Mapa de Temas
# ID	Tema	Qué integra (ELITE)	Fuentes
# T1	Video	OpenAI Video API (/v1/videos) + modelo Sora	[Fuente: web · turn0search4 · 2026-01-10T00:00:00Z]
# T2	Imagen	Gemini “Nano Banana” + Imagen (Google)	[Fuente: web · turn0search7 · 2026-01-10T00:00:00Z]
# T3	Texto	Claude Messages API + Mistral + Together + etc.	[Fuente: web · turn0search23 · 2026-01-10T00:00:00Z]
# T4	OpenAI-compatible	xAI, Groq, vLLM, Ollama, Cerebras, NIM	[Fuente: web · turn3search12 · 2026-01-10T00:00:00Z]
# T5	Rerank/Search	Cohere Rerank + vector (pgvector)	[Fuente: web · turn1search1 · 2026-01-10T00:00:00Z]
# T6	Audio	ElevenLabs TTS + Deepgram STT + AssemblyAI STT	[Fuente: web · turn3search18 · 2026-01-10T00:00:00Z]
# T7	Model marketplaces	Replicate + Hugging Face Inference Providers	[Fuente: web · turn2search0 · 2026-01-10T00:00:00Z]
# T8	Observabilidad	OpenTelemetry + Langfuse (opcional)	[Fuente: web · turn4search3 · 2026-01-10T00:00:00Z]
# D) Cronología
# ID	Paso	Resultado
# C1	WEB habilitado	El generador confirma endpoints/model IDs actuales en docs oficiales
# C2	AI Gateway	Unifica proveedores y modalidades con routing/fallback/budgets
# C3	Acceptance gates	smoke, rls-check, contract, ai:probe como definición de “calidad”
# C4	ELITE	Storage (MinIO), vector (pgvector), observabilidad opcional
# E) Decisiones
#
# Compatibilidad OpenAI como “camino rápido” donde exista (xAI/Groq/vLLM/Ollama/Cerebras/NIM/OpenRouter) para reducir el coste de integración. [Fuente: web · turn4search4 · 2026-01-10T00:00:00Z]
#
# Multimodal real con APIs dedicadas (OpenAI Video /v1/videos, Gemini “Nano Banana”, etc.) en vez de “inventar” endpoints. [Fuente: web · turn0search4 · 2026-01-10T00:00:00Z]
#
# Gateways opcionales (LiteLLM/Helicone) como “modo turbo” para 100+ modelos con una interfaz. [Fuente: web · turn5search0 · 2026-01-10T00:00:00Z]
#
# Keys solo backend (compliance) como regla innegociable. [Fuente: web · turn4search15 · 2026-01-10T00:00:00Z]
#
# F) Tareas
# ID	Tarea	Qué fuerza el prompt
# F1	Investigación	Leer docs oficiales y escribir runbooks/ai-providers.md + defaults en .env.example
# F2	AI Gateway	Adaptadores + RouterPolicy + auditoría + quotas por tenant
# F3	Multimodal	Texto/Imagen/Video/Audio con “SKIPPED” si faltan keys
# F4	Calidad	Targets Make obligatorios y reproducibles en Docker tooling
# G) Hallazgos
#
# OpenAI video: existe API de videos con job async en /v1/videos. [Fuente: web · turn0search4 · 2026-01-10T00:00:00Z]
#
# Gemini imagen: “Nano Banana” se documenta como generación nativa de imágenes en Gemini API; Imagen también está documentado. [Fuente: web · turn0search7 · 2026-01-10T00:00:00Z]
#
# Claude API: la API es REST, con Messages API como primaria. [Fuente: web · turn0search23 · 2026-01-10T00:00:00Z]
#
# xAI: base y endpoints OpenAI-compatibles (incluye /v1/responses y chat completions). [Fuente: web · turn0search2 · 2026-01-10T00:00:00Z]
#
# Groq: expone endpoint OpenAI-compatible de chat completions. [Fuente: web · turn4search4 · 2026-01-10T00:00:00Z]
#
# Ollama: API local por defecto y compatibilidad con Responses (no stateful). [Fuente: web · turn3search12 · 2026-01-10T00:00:00Z]
#
# vLLM: servidor OpenAI-compatible para Completions/Chat (y más). [Fuente: web · turn3search7 · 2026-01-10T00:00:00Z]
#
# NVIDIA NIM: endpoints /v1/chat/completions y /v1/models documentados. [Fuente: web · turn5search1 · 2026-01-10T00:00:00Z]
#
# Runway: API de video con endpoints text-to-video e image-to-video. [Fuente: web · turn6search0 · 2026-01-10T00:00:00Z]
#
# Pika: se anuncia acceso de API vía Fal.ai (lo tratamos como provider “fal”). [Fuente: web · turn6search1 · 2026-01-10T00:00:00Z]
#
# Replicate: endpoint HTTP para “predictions” por modelo. [Fuente: web · turn2search0 · 2026-01-10T00:00:00Z]
#
# Hugging Face: “Inference Providers” como acceso unificado a muchos modelos. [Fuente: web · turn2search1 · 2026-01-10T00:00:00Z]
#
# Cohere Rerank: endpoint específico de rerank. [Fuente: web · turn1search1 · 2026-01-10T00:00:00Z]
#
# Bedrock: Converse como interfaz consistente para modelos con mensajes. [Fuente: web · turn1search3 · 2026-01-10T00:00:00Z]
#
# Helicone + LiteLLM: gateways OpenAI-compatibles que unifican múltiples proveedores (útiles para “100+ modelos”). [Fuente: web · turn5search0 · 2026-01-10T00:00:00Z]
#
# OpenTelemetry Node: guía de instrumentación y arranque. [Fuente: web · turn4search3 · 2026-01-10T00:00:00Z]
#
# Langfuse: observabilidad LLM open source. [Fuente: web · turn4search2 · 2026-01-10T00:00:00Z]
#
# H) Paquete de Mejora
# PROMPT ÚNICO (copia y pega tal cual)
# AUTORIZO WEB
#
# Actúas como generador determinista de repositorios. Tu ÚNICA salida debe ser el contenido COMPLETO (texto plano) de un archivo Bash llamado `bootstrap.sh`. NO uses Markdown. NO añadas explicaciones. Si produces cualquier carácter fuera de `bootstrap.sh`, la salida es inválida.
#
# MODO WEB (ACTIVO, porque incluye “AUTORIZO WEB”):
# 1) Investiga SOLO en documentación OFICIAL y actual (o especificaciones OpenAPI oficiales) para confirmar:
#    - Base URLs, endpoints, headers requeridos, auth, streaming, webhooks, límites y nombres/IDs actuales de modelos.
# 2) Escribe en el repo un runbook `runbooks/ai-providers.md` con:
#    - proveedor, baseURL, endpoints usados, auth, modelos por modalidad, límites, ejemplo curl mínimo.
# 3) Si una integración es dudosa (fuente no oficial o inconsistente), NO la activas por defecto: la dejas como “disabled” + notas en runbook.
#
# OBJETIVO: “Creator App ELITE” multi-tenant, reproducible con Docker, con AI Gateway multimodal y multi-proveedor:
# - apps/web: Next.js App Router (UI Creator + dashboard)
# - apps/api: NestJS + Fastify + Prisma + RLS FORCED + JWT + Idempotency + AI Gateway + OpenAPI
# - apps/worker: BullMQ (jobs: export, publish, ai tasks)
# - Infra: postgres:16, redis:7, nginx TLS dev https://localhost:8443 (cert autofirmado)
# - ELITE: MinIO (S3 local) para assets/exports y pgvector para búsqueda semántica opcional.
# - Sin Node/Pnpm en host: TODO se ejecuta mediante Docker (servicio “tooling”).
#
# TIERS (OBLIGATORIO):
# - Lee NUIKA_TIER desde .env: core|pro|elite (default elite)
#   core: mínimo funcional
#   pro: + SDK tipado + smoke + rls-check + ai:probe + CI mínimo
#   elite: + multimodal + MinIO + pgvector + quotas + tracing opcional + marketplace de templates (stub)
#
# REGLAS INQUEBRANTABLES (FORMATO Y VALIDACIÓN):
# 1) Salida única: imprime SOLO `bootstrap.sh`.
# 2) Todo texto no ejecutable SOLO como comentario Bash (`# `).
# 3) Prohibido “FIX/Hotfix/Checks” fuera de comentarios Bash.
# 4) Prohibido `//` dentro de JSON/YAML/SQL y prohibido `// path:` en cualquier sitio.
# 5) HERE-DOCS perfectos:
#    cat > ruta/archivo <<'EOF'
#    ...contenido...
#    EOF
#    - `EOF` de cierre en columna 1 exacto, sin espacios ni tabs.
# 6) YAML válido: `rules:` como lista `- id: ...` (Semgrep).
# 7) JSON válido: para scripts usa `node -e '...'` (comillas simples).
# 8) TypeScript globs correctos: include ["src/**/*.ts","test/**/*.ts"].
# 9) Dockerfiles válidos: nada de texto suelto; orden Prisma correcto (schema antes de generate; NODE_ENV=production solo en runtime final).
# 10) No exponer PII: anonimiza con ▇▇▇; secretos como CHANGE_ME.
#
# SEGURIDAD/COMPLIANCE (OBLIGATORIO):
# - API keys SOLO backend. Nunca en web/client. Documenta esta regla en runbook.
# - Añade “policy engine” mínimo: allowlist por tenant de providers/modelos/modalidades.
# - Añade quotas por tenant (requests IA, tokens/coste estimado, storage, jobs).
# - Añade auditoría: requestId, tenantId, provider/model, latencia, error, coste estimado; por defecto guarda hash del prompt y NO el prompt bruto.
#
# MULTI-TENANT REAL (OBLIGATORIO):
# - Prisma schema mínimo: Tenant/User/Membership/Project/AuditLog/IdempotencyKey/Publish
# - ELITE añade: Asset (MinIO), ProjectSnapshot, PromptTrace (sin PII), QuotaUsage.
# - RLS SQL completo y autocontenible:
#   - helpers PL/pgSQL current_tenant_id(), current_user_id()
#   - ENABLE + FORCE RLS en tablas multi-tenant
#   - policies completas (incluye IdempotencyKey y tablas ELITE)
# - Separación de roles DB:
#   - NUIKA_DATABASE_URL (usuario app NO-superuser)
#   - NUIKA_MIGRATION_DATABASE_URL (admin SOLO para migraciones)
# - `make migrate` aplica migraciones + RLS automáticamente (no solo “existe rls.sql”).
#
# AI GATEWAY (OBLIGATORIO, MULTIMODAL, MULTI-PROVEEDOR):
# Crea `apps/api/src/modules/ai/` con:
# 1) Interfaces por modalidad (mínimo):
#    - text.generate (stream opcional)
#    - embeddings.embed
#    - rerank.score (opcional)
#    - image.generate
#    - video.generate (async job + polling/webhook)
#    - audio.stt / audio.tts (opcional elite)
# 2) Provider adapters (habilitados por env + keys; si faltan keys => SKIPPED en ai:probe):
#    - OpenAI: texto/imagen; video vía POST /v1/videos; (si procede) audio/realtime.
#    - Anthropic Claude: Messages API (texto).
#    - xAI Grok: base https://api.x.ai/v1 y endpoints OpenAI-compatibles (/v1/responses y /v1/chat/completions).
#    - Google Gemini: texto + generación nativa de imágenes (“Nano Banana”) + Imagen API si aplica.
#    - Mistral: chat completions (texto) + embeddings si aplica.
#    - Cohere: embeddings + Rerank endpoint.
#    - AWS Bedrock: Converse/ConverseStream como interfaz unificada (enterprise).
#    - OpenRouter: Responses API OpenAI-compatible (router externo multi-model).
#    - Groq: OpenAI-compatible chat completions (inferencia ultrarrápida).
#    - Together: chat completions para OSS models.
#    - Fireworks: chat completions.
#    - Cerebras: OpenAI-compatible baseURL.
#    - NVIDIA NIM: endpoints /v1/models + /v1/chat/completions (OpenAPI).
#    - Replicate: HTTP predictions endpoint por modelo.
#    - Hugging Face: Inference Providers (unified).
#    - Stability: REST API (text-to-image).
#    - Runway: text-to-video + image-to-video.
#    - Pika: integra vía provider “fal” (porque Pika indica acceso a API vía Fal.ai). Deja claro en runbook.
#    - Local: vLLM OpenAI-compatible server.
#    - Local: Ollama API + OpenAI Responses compatibility (no-stateful).
#    - Gateways opcionales:
#      - LiteLLM Proxy (OpenAI-compatible gateway multi-proveedor; activable por env).
#      - Helicone AI Gateway (OpenAI-compatible unified gateway; activable por env).
# 3) RouterPolicy:
#    - Selección por tarea (code/copy/support/embedding/rerank/image/video/audio).
#    - Constraints: max_cost, max_latency_ms, max_retries, allowlist tenant.
#    - Fallback chain por modalidad.
#    - Caching seguro opcional (solo para prompts “publicables” y sin PII).
# 4) Auditoría y coste:
#    - Estima coste por provider (tabla configurable en env).
#    - Registra latencia, tokens estimados, estado job (video), errores normalizados.
#
# PRODUCTO CREATOR (ELITE):
# - Project versioning: snapshots + rollback.
# - Publish: URL versionada + webhook onPublish (stub).
# - Templates marketplace: packages/templates con 2-3 templates mínimas.
# - Assets: MinIO para imágenes/video/exports; Nginx sirve assets con cache headers.
# - Worker jobs: export/publish/ai-tasks con BullMQ.
#
# OBSERVABILIDAD (PRO/ELITE):
# - Logs JSON con requestId/tenantId.
# - OpenTelemetry (toggle): trazas y métricas a consola por defecto.
# - Integración opcional “langfuse” (solo si NUIKA_OBS=langfuse): instrumenta llamadas del AI Gateway para trazas/coste.
#
# DX/CI (OBLIGATORIO):
# - docker-compose incluye servicio `tooling` (monta `.:/repo`, corepack/pnpm) y Makefile usa tooling SIEMPRE.
# - Make targets obligatorios:
#   - make up
#   - make migrate seed
#   - make verify (lint+typecheck+tests+contract+security-scan)
#   - make smoke (API health + login devuelve JSON con content-type correcto directo y por proxy)
#   - make rls-check (tenant A no puede leer/escribir tenant B)
#   - make ai:probe (1 request por provider habilitado; sin key => SKIPPED; no falla build)
#   - make contract (OpenAPI válido + sdk:build)
#   - make sdk:build (OpenAPI -> packages/sdk y web/worker usan el SDK en 1 llamada real)
# - CI mínimo (pro/elite): workflow para lint/typecheck/test/contract con cache.
#
# SECURITY SCANS (OBLIGATORIO):
# - Semgrep (security/semgrep.yml válido: rules lista `- id:`)
# - gitleaks + trivy (en make verify; no fallan por defecto, pero reportan)
#
# CHECKLIST FINAL ANTES DE IMPRIMIR:
# - ¿Salida solo bootstrap.sh sin Markdown? (sí)
# - ¿Todos los here-docs cierran con EOF exacto? (sí)
# - ¿YAML/JSON/TS/Dockerfiles/SQL válidos y sin texto suelto? (sí)
# - ¿RLS SQL completo y aplicado automáticamente en make migrate? (sí)
# - ¿AI Gateway con adapters+router+fallback+auditoría+quotas? (sí)
# - ¿Make targets smoke/rls-check/ai:probe/contract/sdk:build existen y usan tooling? (sí)
#
# Ahora imprime el contenido COMPLETO de `bootstrap.sh`.
#
# I) Anexos
# Lista de “extras” ya integrados por el prompt (sin activar por defecto):
#
# Gateways: LiteLLM Proxy, Helicone AI Gateway.  [Fuente: web · turn6search3 · 2026-01-10T00:00:00Z]
#
# Observabilidad: OpenTelemetry + Langfuse opcional.  [Fuente: web · turn4search2 · 2026-01-10T00:00:00Z]
#
# Local/OSS: vLLM server, Ollama API + OpenAI compatibility.  [Fuente: web · turn3search7 · 2026-01-10T00:00:00Z]
#
# {
#   "portada": {
#     "titulo": "Prompt único ELITE + WEB para Creator App multi-tenant con AI Gateway multimodal y multi-proveedor",
#     "fecha_utc": "2026-01-10T00:00:00Z",
#     "fuentes": [
#       {
#         "tipo": "sistema",
#         "id_ref": "chat_user_input",
#         "fecha_utc": "2026-01-10T00:00:00Z"
#       }
#     ]
#   },
#   "resumen_ejecutivo": {
#     "autorizacion_web": true,
#     "resultado": "Se entrega un único prompt (copiar/pegar) que obliga al generador a investigar en docs oficiales y construir un monorepo reproducible con AI Gateway multimodal, routing/fallback, seguridad/compliance, SDK tipado, CI y acceptance gates.",
#     "principios": [
#       "Config-driven por env (sin hardcode frágil)",
#       "Compatibilidad OpenAI donde exista",
#       "Keys solo backend",
#       "Calidad definida por acceptance gates"
#     ],
#     "citas_web_clave": [
#       {
#         "id_ref": "turn0search4",
#         "hecho": "OpenAI Video API /v1/videos",
#         "fecha_utc": "2026-01-10T00:00:00Z"
#       },
#       {
#         "id_ref": "turn0search7",
#         "hecho": "Gemini Nano Banana (imagen nativa)",
#         "fecha_utc": "2026-01-10T00:00:00Z"
#       },
#       {
#         "id_ref": "turn0search2",
#         "hecho": "xAI base y endpoints OpenAI-compatibles",
#         "fecha_utc": "2026-01-10T00:00:00Z"
#       },
#       {
#         "id_ref": "turn6search3",
#         "hecho": "LiteLLM Proxy como gateway OpenAI-compatible multi-proveedor",
#         "fecha_utc": "2026-01-10T00:00:00Z"
#       }
#     ]
#   },
#   "mapa_de_temas": [
#     {
#       "id": "T1",
#       "tema": "Video",
#       "fuentes": [
#         "turn0search4",
#         "turn0search0"
#       ]
#     },
#     {
#       "id": "T2",
#       "tema": "Imagen",
#       "fuentes": [
#         "turn0search7",
#         "turn0search3"
#       ]
#     },
#     {
#       "id": "T3",
#       "tema": "OpenAI-compatible providers",
#       "fuentes": [
#         "turn0search2",
#         "turn4search4",
#         "turn3search7",
#         "turn3search12",
#         "turn5search2",
#         "turn5search1",
#         "turn1search10"
#       ]
#     },
#     {
#       "id": "T4",
#       "tema": "Gateways",
#       "fuentes": [
#         "turn6search3",
#         "turn5search0",
#         "turn6search15"
#       ]
#     },
#     {
#       "id": "T5",
#       "tema": "Observabilidad",
#       "fuentes": [
#         "turn4search3",
#         "turn4search2"
#       ]
#     }
#   ],
#   "cronologia": [
#     {
#       "id": "C1",
#       "fecha_utc": "2026-01-10T00:00:00Z",
#       "evento": "Autorización web activada por el usuario",
#       "fuente": "chat_user_input"
#     },
#     {
#       "id": "C2",
#       "fecha_utc": "2026-01-10T00:00:00Z",
#       "evento": "Entrega de prompt único ELITE + WEB",
#       "fuente": "chat_user_input"
#     }
#   ],
#   "decisiones": [
#     {
#       "id": "D1",
#       "decision": "Usar compatibilidad OpenAI donde exista para acelerar integraciones",
#       "fuentes": [
#         "turn0search2",
#         "turn4search4",
#         "turn3search7",
#         "turn3search12",
#         "turn5search2",
#         "turn5search1"
#       ]
#     },
#     {
#       "id": "D2",
#       "decision": "Multimodal por APIs oficiales (video/imagen/audio) y SKIPPED si faltan keys",
#       "fuentes": [
#         "turn0search4",
#         "turn0search7",
#         "turn2search3"
#       ]
#     },
#     {
#       "id": "D3",
#       "decision": "Gateways opcionales (LiteLLM/Helicone) para ampliar catálogo sin romper el core",
#       "fuentes": [
#         "turn6search3",
#         "turn5search0",
#         "turn6search15"
#       ]
#     }
#   ],
#   "tareas": [
#     {
#       "id": "F1",
#       "accion": "Pegar el prompt único en tu generador para producir bootstrap.sh",
#       "prioridad": "alta"
#     },
#     {
#       "id": "F2",
#       "accion": "Configurar keys (solo backend) y habilitar providers deseados por env",
#       "prioridad": "media"
#     },
#     {
#       "id": "F3",
#       "accion": "Ejecutar make verify y revisar runbooks/ai-providers.md",
#       "prioridad": "alta"
#     }
#   ],
#   "hallazgos": [
#     {
#       "id": "H1",
#       "descripcion": "OpenAI expone generación de video en /v1/videos (job async).",
#       "fuentes": [
#         "turn0search4",
#         "turn0search0"
#       ]
#     },
#     {
#       "id": "H2",
#       "descripcion": "Gemini documenta generación nativa de imágenes (Nano Banana).",
#       "fuentes": [
#         "turn0search7"
#       ]
#     },
#     {
#       "id": "H3",
#       "descripcion": "Existen múltiples proveedores OpenAI-compatibles (xAI/Groq/vLLM/Ollama/Cerebras/NIM/OpenRouter).",
#       "fuentes": [
#         "turn0search2",
#         "turn4search4",
#         "turn3search7",
#         "turn3search12",
#         "turn5search2",
#         "turn5search1",
#         "turn1search10"
#       ]
#     }
#   ],
#   "paquete_de_mejora": {
#     "entregables": [
#       {
#         "id": "P1",
#         "nombre": "Prompt único ELITE + WEB",
#         "ubicacion": "Sección H"
#       }
#     ],
#     "proveedores_incluidos": [
#       "OpenAI",
#       "Anthropic",
#       "xAI",
#       "Google Gemini",
#       "Mistral",
#       "Cohere",
#       "AWS Bedrock",
#       "OpenRouter",
#       "Groq",
#       "Together",
#       "Fireworks",
#       "Cerebras",
#       "NVIDIA NIM",
#       "Replicate",
#       "Hugging Face",
#       "Stability",
#       "Runway",
#       "Fal (para Pika)",
#       "vLLM (local)",
#       "Ollama (local)",
#       "LiteLLM Proxy (gateway opcional)",
#       "Helicone AI Gateway (gateway opcional)"
#     ]
#   },
#   "anexos": {
#     "nota_urls": "URLs y endpoints se incluyen dentro del prompt (bloque de código) para cumplir la regla de no publicar URLs en prosa."
#   }
# }
