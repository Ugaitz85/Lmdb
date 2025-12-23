# Astro on Netlify Platform Starter

[Live Demo](https://astro-platform-starter.netlify.app/)

A modern starter based on Astro.js, Tailwind, and [Netlify Core Primitives](https://docs.netlify.com/core/overview/#develop) (Edge Functions, Image CDN, Blob Store).

## Astro Commands

All commands are run from the root of the project, from a terminal:

| Command                   | Action                                           |
| :------------------------ | :----------------------------------------------- |
| `npm install`             | Installs dependencies                            |
| `npm run dev`             | Starts local dev server at `localhost:4321`      |
| `npm run build`           | Build your production site to `./dist/`          |
| `npm run preview`         | Preview your build locally, before deploying     |
| `npm run astro ...`       | Run CLI commands like `astro add`, `astro check` |
| `npm run astro -- --help` | Get help using the Astro CLI                     |

## Deploying to Netlify

[![Deploy to Netlify](https://www.netlify.com/img/deploy/button.svg)](https://app.netlify.com/start/deploy?repository=https://github.com/netlify-templates/astro-platform-starter)

## GitHub URL

This repository is not currently linked to a public GitHub URL in this environment (no `origin` remote is configured). To push it to your own GitHub account:

1. Create a new empty repository on GitHub (e.g., `https://github.com/<tu-usuario>/<tu-repo>`).
2. Add it as a remote: `git remote add origin https://github.com/<tu-usuario>/<tu-repo>.git`.
3. Push the current branch: `git push -u origin work` (or the branch name you prefer).

After pushing, that GitHub URL will host the full project, including the `DodgeBlocks/` Android game.

## Android Studio (juego DodgeBlocks)

El juego Android completo está en `DodgeBlocks/`. Para abrirlo en Android Studio:

1. En Android Studio: **File → Open** y selecciona la carpeta `DodgeBlocks/`.
2. Espera a que finalice **Gradle Sync**.
3. Elige un emulador o dispositivo.
4. Pulsa **Run ▶ app** para instalar y ejecutar.

Requisitos rápidos: Android Studio reciente (AGP 8.2.x), Java 17, `compileSdk/targetSdk 34`, `minSdk 24` (todo ya configurado en el proyecto).

### Clonar el repo directamente desde Android Studio

Si prefieres clonar con la opción **Get from VCS** (pantalla con campo "Repository URL"):

1. En Android Studio: **File → New → Project from Version Control…** (o el botón **Get from VCS** de la pantalla inicial).
2. En **Version control** elige **Git** y pega la URL de tu repositorio (ej.: `https://github.com/<tu-usuario>/<tu-repo>.git`).
3. Selecciona la carpeta de destino y pulsa **Clone**. Android Studio descargará el repo completo.
4. Cuando termine, en el diálogo de importación elige abrir el subdirectorio `DodgeBlocks/` (es el proyecto Android). Si ya se abre la raíz, usa **File → Open…** y selecciona `DodgeBlocks/`.
5. Deja que corra **Gradle Sync** y luego usa **Run ▶ app**.

## Developing Locally

| Prerequisites                                                                |
| :--------------------------------------------------------------------------- |
| [Node.js](https://nodejs.org/) v18.14+.                                      |
| (optional) [nvm](https://github.com/nvm-sh/nvm) for Node version management. |

1. Clone this repository, then run `npm install` in its root directory.

2. For the starter to have full functionality locally (e.g. edge functions, blob store), please ensure you have an up-to-date version of Netlify CLI. Run:

```
npm install netlify-cli@latest -g
```

3. Link your local repository to the deployed Netlify site. This will ensure you're using the same runtime version for both local development and your deployed site.

```
netlify link
```

4. Then, run the Astro.js development server via Netlify CLI:

```
netlify dev
```

If your browser doesn't navigate to the site automatically, visit [localhost:8888](http://localhost:8888).
