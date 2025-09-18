# Multiplayer (Firebase Realtime) - Integração para Manarolling

Este README mostra os passos necessários para configurar e usar a estrutura multiplayer que adicionei ao projeto.
Eu adicionei arquivos Kotlin em: `main/java/com/fiap/manarolling/multiplayer` e uma tela de lobby em `ui/LobbyScreen.kt`.
Também atualizei `Routes.kt` adicionando `LOBBY` e `MULTIPLAYER`.

## Arquivos adicionados
- multiplayer/GameSession.kt
- multiplayer/MultiplayerRepository.kt
- multiplayer/MultiplayerViewModel.kt
- ui/LobbyScreen.kt

## Passos para configurar o Firebase
1. Crie um projeto no console do Firebase: https://console.firebase.google.com/
2. Adicione um app Android com o pacote `com.fiap.manarolling` (verifique o packageName no AndroidManifest).
3. Baixe o `google-services.json` e coloque em `app/` (no módulo do app) do seu projeto Android (substitua se houver).
4. No `build.gradle` do projeto (nível app) adicione:
   ```gradle
   implementation 'com.google.firebase:firebase-database-ktx:20.2.2'
   implementation 'com.google.firebase:firebase-auth-ktx:22.0.0' // opcional
   ```
5. No `build.gradle` (nível projeto) adicione o plugin do Google Services e no app-level aplique:
   ```gradle
   classpath 'com.google.gms:google-services:4.3.15'
   // no app module
   apply plugin: 'com.google.gms.google-services'
   ```
6. Habilite o Realtime Database no console do Firebase e coloque regras de leitura/escrita enquanto estiver em desenvolvimento (apenas para protótipo):
   ```json
   {
     "rules": {
       ".read": true,
       ".write": true
     }
   }
   ```
   **ATENÇÃO**: Isto é inseguro para produção. Ajuste regras antes de publicar.

## Como funciona (fluxo básico)
- O mestre cria uma sala; o `MultiplayerRepository.createSession` gera um código (6 caracteres alfanuméricos) e escreve `/sessions/{sessionId}`.
- Jogadores se conectam usando o código; são adicionados em `/sessions/{sessionId}/players/{playerId}`.
- O mestre atualiza o estado do jogo em `/sessions/{sessionId}/state` (um mapa). Todos que escutam recebem atualizações em tempo real.
- `MultiplayerViewModel` expõe `sessionState` como `StateFlow<GameSession?>` para que as UIs possam reagir.

## Como integrar à navegação
- Adicione uma rota que mostre `LobbyScreen` antes de entrar na sala.
- Ao criar/entrar: chame `vm.startListening(sessionId)` e navegue para a tela de jogo (ex: `Routes.MULTIPLAYER`).
- Ao sair: chame `vm.leaveSession(...)` e `vm.stopListening(sessionId)`.

## Observações e próximos passos
- Para identificação única de jogadores, idealmente use Auth do Firebase (anonymous auth) em vez de usar apenas nomes.
- Você pode adicionar permissões: apenas o mestre pode alterar certos campos do `state` (por exemplo, `events`).
- Para performance, troque o uso direto de `Map<String, Any>` por modelos mais fortemente tipados conforme seu estado do jogo crescer.
- Se preferir usar `Firestore`, a lógica é parecida e o Firestore tem consultas mais ricas.
