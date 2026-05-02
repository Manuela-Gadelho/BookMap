# BookMap - Guia de Configuracao

## 1. Configurar Firebase (Banco de Dados Online)

O app usa Firebase Firestore para sincronizacao em nuvem e Firebase Auth. Siga os passos abaixo:

### Passo a passo:

1. Acesse o [Firebase Console](https://console.firebase.google.com/)
2. Clique em **"Adicionar projeto"** (ou "Add project")
3. Nome do projeto: `BookMap`
4. Desative o Google Analytics (opcional, pode ativar se quiser)
5. Clique em **"Criar projeto"**

### Adicionar app Android:

1. No painel do projeto, clique no icone **Android**
2. Nome do pacote: `com.bookmap.app`
3. Apelido: `BookMap`
4. Clique em **"Registrar app"**
5. Faca download do arquivo `google-services.json`
6. Copie o arquivo para `app/google-services.json` (substituindo o placeholder existente)

### Ativar Firestore:

1. No menu lateral, clique em **"Firestore Database"**
2. Clique em **"Criar banco de dados"**
3. Selecione **"Iniciar no modo de teste"** (para desenvolvimento)
4. Escolha a regiao mais proxima (ex: `southamerica-east1` para Brasil)
5. Clique em **"Ativar"**

### Ativar Authentication:

1. No menu lateral, clique em **"Authentication"**
2. Clique em **"Comecar"**
3. Ative o provedor **"E-mail/Senha"**
4. Salve

## 2. Configurar Google Maps API Key

### Passo a passo:

1. Acesse o [Google Cloud Console](https://console.cloud.google.com/)
2. Selecione o projeto BookMap (mesmo do Firebase, ja sera criado automaticamente)
3. No menu lateral: **APIs e servicos > Biblioteca**
4. Busque por **"Maps SDK for Android"**
5. Clique em **"Ativar"**
6. Va para **APIs e servicos > Credenciais**
7. Clique em **"Criar credenciais" > "Chave de API"**
8. Copie a chave gerada
9. Abra o arquivo `app/src/main/AndroidManifest.xml`
10. Substitua `YOUR_GOOGLE_MAPS_API_KEY` pela sua chave:
    ```xml
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="SUA_CHAVE_AQUI" />
    ```

### (Recomendado) Restringir a chave:

1. Na pagina de credenciais, clique na chave criada
2. Em **"Restricoes de aplicativo"**, selecione **"Apps Android"**
3. Adicione o pacote `com.bookmap.app` e o SHA-1 do seu certificado
4. Em **"Restricoes de API"**, selecione **"Maps SDK for Android"**
5. Salve

## 3. Executar o App

```bash
# Compilar
./gradlew assembleDebug

# Rodar testes unitarios (incluindo H2)
./gradlew testDebugUnitTest

# Rodar testes instrumentalizados (requer emulador/dispositivo)
./gradlew connectedAndroidTest

# Instalar no dispositivo
./gradlew installDebug
```

## 4. Estrutura de Testes

- **Testes Unitarios** (`src/test/`): Robolectric + JUnit + Mockito
- **Testes H2** (`src/test/.../h2/`): Validacao SQL com banco H2 em memoria
- **Testes Espresso** (`src/androidTest/.../espresso/`): Testes instrumentalizados de UI
  - `LoginFlowTest` - Fluxo de login (8 testes)
  - `RegisterFlowTest` - Fluxo de cadastro (7 testes)
  - `ForgotPasswordFlowTest` - Recuperacao de senha (7 testes)
  - `BookFlowTest` - Gerenciamento de livros (13 testes)
  - `ClubFlowTest` - Clubes de leitura (9 testes)
  - `MapFlowTest` - Mapa literario (5 testes)
  - `ProfileFlowTest` - Perfil do usuario (6 testes)
  - `FullE2EFlowTest` - Fluxo completo end-to-end (4 testes)
