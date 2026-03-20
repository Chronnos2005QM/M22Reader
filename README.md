# M22 Reader 📖

Leitor de Manhwa/Manga para Android com suporte a **PDF, EPUB, CBZ e CBR**.

---

## ✨ Funcionalidades

| Feature | Estado |
|---|---|
| Leitura de PDF | ✅ |
| Leitura de EPUB | ✅ |
| Leitura de CBZ | ✅ |
| Leitura de CBR | ✅ |
| Biblioteca com capas | ✅ |
| Modo escuro / claro | ✅ |
| Histórico de leitura | ✅ |
| Marcadores / Favoritos | ✅ |
| Aba de adicionados recentemente | ✅ |
| Pesquisa por título e autor | ✅ |
| Vista grelha ou lista | ✅ |
| Ordenação da biblioteca | ✅ |
| Importar ficheiros do sistema | ✅ |

---

## 🏗️ Arquitetura

```
M22Reader/
├── app/src/main/java/com/m22reader/
│   ├── data/
│   │   ├── model/         → Book.kt (entidade Room)
│   │   ├── dao/           → BookDao.kt (queries)
│   │   ├── repository/    → BookRepository.kt
│   │   └── M22Database.kt
│   ├── ui/
│   │   ├── theme/         → Theme.kt (cores, tipografia)
│   │   ├── library/       → LibraryScreen + ViewModel
│   │   ├── updates/       → UpdatesScreen + ViewModel
│   │   ├── history/       → HistoryScreen + ViewModel
│   │   ├── favorites/     → FavoritesScreen + ViewModel
│   │   ├── reader/        → ReaderScreen + ViewModel
│   │   └── M22AppScaffold.kt (nav + scaffold principal)
│   ├── utils/
│   │   └── FileImporter.kt + MetadataExtractor.kt
│   ├── MainActivity.kt
│   └── M22App.kt (Hilt)
```

### Stack
- **Linguagem**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Navegação**: Navigation Compose
- **DB**: Room + Flow
- **DI**: Hilt
- **Imagens**: Coil
- **PDF**: AndroidPdfViewer (barteksc)
- **EPUB**: epublib
- **CBR**: junrar
- **CBZ**: java.util.zip (nativo)

---

## 🚀 Como compilar

### Pré-requisitos
- Android Studio Hedgehog (2023.1.1) ou superior
- JDK 17
- Android SDK 34

### Passos
```bash
# 1. Abre o projeto no Android Studio
# 2. Sincroniza o Gradle (File → Sync Project with Gradle Files)
# 3. Conecta um dispositivo ou inicia um emulador (API 26+)
# 4. Run → Run 'app'
```

### Gerar APK de release
```bash
./gradlew assembleRelease
# APK em: app/build/outputs/apk/release/app-release.apk
```

---

## 📋 Próximas melhorias sugeridas

- [ ] Leitor com scroll vertical contínuo (estilo Mihon)
- [ ] Extracção automática de capas de CBZ/CBR
- [ ] Suporte a pastas/colecções
- [ ] Backup e restauro da biblioteca
- [ ] Fonte e tamanho de texto configurável (para EPUB)
- [ ] Widget para continuar a leitura no ecrã inicial
