# EtherLot (chat-app)

Простое приложение для чата, поддерживающее групповые чаты и видеозвонки.

## ✨ Функции

### 💬 Функционал чата

- Создание, присоединение, выход, удаление и изменение групповых чатов

- Отправка и получение сообщений в режиме реального времени

- История сообщений

- Управление группами (создание, редактирование, удаление, выход)

### 📹 Видеозвонки

- Групповые видеозвонки

- Уведомления о входящих звонках и функция ответа/отклонения

- Передача аудио и видео в режиме реального времени

### 👥 Управление пользователями

- Регистрация и вход пользователей

### 🎨 Пользовательский интерфейс

- Дизайн Material Design 3

- Адаптивный макет


## 🚀 Быстрый старт

### Проект клонирования

```bash
    git clone https://github.com/Mobile-App-ITMO/chat-app.git
    cd chat-app
```

### Настройка среды

1. Открыть Android Studio

2. Выбрать «Открыть существующий проект»

3. Выбрать каталог проекта

4. Дождать завершения синхронизации Gradle

### Настройка сервера

```bash
    cd chat-app
    gradle :server:rest:run
```

### Запуск приложение

- Подключите ваше устройство Android или запустите эмулятор.
- Нажмите кнопку «Запустить» в Android Studio. (MainActivity)

## 📖 Руководство пользователя

### Создание учетной записи

1. Запустите приложение
2. Нажмите кнопку «Регистрация»
3. Введите имя пользователя и пароль
4. Завершите регистрацию

### Создание чата

1. Войдите в систему и откройте главный интерфейс
2. Нажмите кнопку «+» в правом нижнем углу
3. Выберите «Создать комнату»
4. Введите название комнаты
5. Нажмите «Создать»

### Инициирование видеозвонка

1. Войдите в чат
2. Нажмите значок видео в правом верхнем углу
3. Дождитесь ответа собеседника

### Управление группами

1. Нажмите значок настроек в интерфейсе чата
2. Выберите соответствующую операцию:
   - Редактировать комнату: Изменить название комнаты
   - Покинуть комнату: Покинуть текущую группу
   - Удалить комнату: Расформировать группу

# Основная структура приложения

```
chat-app
├─ app
│  ├─ android
│  │  ├─ proguard-rules.pro
│  │  └─ src
│  │     └─ androidMain
│  │        ├─ AndroidManifest.xml
│  │        ├─ kotlin
│  │        │  └─ MainActivity.kt
│  │        └─ res
│  ├─ common
│  │  └─ src
│  │     ├─ androidMain
│  │     │  └─ kotlin
│  │     │     ├─ Android.kt
│  │     │     ├─ calls
│  │     │     │  ├─ AudioRenderer.kt
│  │     │     │  └─ VideoRenderer.kt
│  │     │     └─ vm
│  │     │        └─ ChatViewModelProvider.kt
│  │     ├─ commonMain
│  │     │  └─ kotlin
│  │     │     ├─ calls
│  │     │     │  ├─ AudioRenderer.kt
│  │     │     │  ├─ CallAction.kt
│  │     │     │  ├─ CallMediaState.kt
│  │     │     │  ├─ FloatingVideoRenderer.kt
│  │     │     │  ├─ VideoCallControls.kt
│  │     │     │  ├─ VideoCallScreen.kt
│  │     │     │  └─ VideoRenderer.kt
│  │     │     ├─ ChatApplication.kt
│  │     │     ├─ client
│  │     │     │  └─ RemoteList.kt
│  │     │     ├─ messages
│  │     │     │  ├─ MessageInput.kt
│  │     │     │  ├─ MessageList.kt
│  │     │     │  └─ MessageListItem.kt
│  │     │     ├─ rooms
│  │     │     │  ├─ CreateRoomDialog.kt
│  │     │     │  ├─ EditRoomDialog.kt
│  │     │     │  ├─ JoinRoomDialog.kt
│  │     │     │  ├─ RoomHeader.kt
│  │     │     │  └─ RoomsSideMenu.kt
│  │     │     ├─ settings
│  │     │     │  └─ UserMenu.kt
│  │     │     ├─ style
│  │     │     │  └─ MaterialColors.kt
│  │     │     ├─ ui
│  │     │     │  ├─ components
│  │     │     │  │  ├─ AddButton.kt
│  │     │     │  │  ├─ BackIcon.kt
│  │     │     │  │  ├─ BlackButton.kt
│  │     │     │  │  ├─ BottomNavBar.kt
│  │     │     │  │  ├─ Errors.kt
│  │     │     │  │  ├─ FriendList.kt
│  │     │     │  │  ├─ GMList.kt
│  │     │     │  │  ├─ Icons.kt
│  │     │     │  │  ├─ InputField.kt
│  │     │     │  │  ├─ Loader.kt
│  │     │     │  │  ├─ SettingList.kt
│  │     │     │  │  ├─ ToggleButton.kt
│  │     │     │  │  └─ WhiteButton.kt
│  │     │     │  ├─ screens
│  │     │     │  │  ├─ chat
│  │     │     │  │  │  ├─ ChatScreen.kt
│  │     │     │  │  │  └─ GroupChatScreen.kt
│  │     │     │  │  ├─ home
│  │     │     │  │  │  └─ HomeScreen.kt
│  │     │     │  │  └─ login
│  │     │     │  │     ├─ ConfirmationScreen.kt
│  │     │     │  │     ├─ LoginScreen.kt
│  │     │     │  │     ├─ RegisterScreen.kt
│  │     │     │  │     └─ WelcomeArtifacts.kt
│  │     │     │  └─ theme
│  │     │     │     ├─ Color.kt
│  │     │     │     ├─ Design.kt
│  │     │     │     ├─ Theme.kt
│  │     │     │     └─ Type.kt
│  │     │     ├─ utils
│  │     │     │  ├─ LocalStorage.kt
│  │     │     │  ├─ Requests.kt
│  │     │     │  ├─ Text.kt
│  │     │     │  └─ Time.kt
│  │     │     └─ vm
│  │     │        ├─ ChatViewModel.kt
│  │     │        ├─ ChatViewModelProvider.kt
│  │     │        ├─ joinIceServers.kt
│  │     │        └─ VideoCallModel.kt
│  │     ├─ jvmMain
│  │     │  └─ kotlin
│  │     │     ├─ calls
│  │     │     │  ├─ AudioRenderer.kt
│  │     │     │  └─ VideoRenderer.kt
│  │     │     ├─ ChatPreviews.kt
│  │     │     ├─ data
│  │     │     │  └─ LocalStorage.jvm.kt
│  │     │     ├─ login
│  │     │     │  └─ LoginPreviews.kt
│  │     │     └─ vm
│  │     │        └─ ChatViewModelProvider.kt
│  │     ├─ wasmJsMain
│  │     │  └─ kotlin
│  │     │     ├─ calls
│  │     │     │  ├─ AudioRenderer.wasmJs.kt
│  │     │     │  └─ VideoRenderer.wasmJs.kt
│  │     │     └─ vm
│  │     │        └─ ChatViewModelProvider.wasmJs.kt
│  │     └─ webMain
│  │        └─ kotlin
│  │           └─ data
│  │              └─ LocalStorage.web.kt
│  ├─ desktop
│  │  ├─ proguard-rules.pro
│  │  └─ src
│  │     └─ main
│  │        └─ kotlin
│  │           └─ main.kt
│  └─ wasmJs
│     └─ src
│        └─ wasmJsMain
│           ├─ kotlin
│           │  └─ io.ktor.chat
│           │     └─ main.kt
│           └─ resources
│              ├─ index.html
│              └─ styles.css
├─ app.properties
├─ client
│  ├─ README.md
│  └─ src
│     ├─ androidMain
│     │  └─ kotlin
│     │     └─ HttpChatClient.android.kt
│     ├─ commonMain
│     │  └─ kotlin
│     │     ├─ CallSessionManager.kt
│     │     ├─ ChatClient.kt
│     │     ├─ HttpChatClient.kt
│     │     ├─ HttpSignalingClient.kt
│     │     ├─ MockChatClient.kt
│     │     ├─ PeerConnectionManager.kt
│     │     └─ Repositories.kt
│     ├─ jvmMain
│     │  └─ kotlin
│     │     └─ HttpChatClient.jvm.kt
│     ├─ jvmTest
│     │  ├─ kotlin
│     │  │  └─ HttpChatClientTest.kt
│     │  └─ resources
│     │     └─ test-server.yaml
│     └─ wasmJsMain
│        └─ kotlin
│           └─ HttpChatClient.wasmJs.kt
├─ core
│  └─ src
│     ├─ commonMain
│     │  └─ kotlin
│     │     ├─ Authentication.kt
│     │     ├─ Call.kt
│     │     ├─ Entities.kt
│     │     ├─ Events.kt
│     │     ├─ Exceptions.kt
│     │     └─ Repository.kt
│     └─ commonTest
│        └─ kotlin
│           └─ ListRepositoryTest.kt
├─ db
│  └─ src
│     ├─ main
│     │  └─ kotlin
│     │     ├─ ColumnConversion.kt
│     │     ├─ ExposedRepository.kt
│     │     ├─ KeyDB.kt
│     │     ├─ MemberRepository.kt
│     │     ├─ MessageRepository.kt
│     │     ├─ RoomRepository.kt
│     │     ├─ Schema.kt
│     │     └─ UserRepository.kt
│     └─ test
│        └─ kotlin
│           └─ UserRepositoryTest.kt
├─ LICENSE
├─ postgres-data
├─ README.md
└─ server
   ├─ admin
   │  ├─ README.md
   │  └─ src
   │     ├─ Admin.kt
   │     └─ main
   │        ├─ kotlin
   │        │  └─ Admin.kt
   │        └─ resources
   │           ├─ application.yaml
   │           └─ logback.xml
   ├─ common
   │  ├─ README.md
   │  └─ src
   │     └─ main
   │        └─ kotlin
   │           ├─ Databases.kt
   │           ├─ HealthCheck.kt
   │           ├─ Logging.kt
   │           ├─ Mail.kt
   │           ├─ Repositories.kt
   │           └─ Security.kt
   └─ rest
      ├─ README.md
      └─ src
         ├─ main
         │  ├─ kotlin
         │  │  ├─ Authentication.kt
         │  │  ├─ Memberships.kt
         │  │  ├─ Messages.kt
         │  │  ├─ Rest.kt
         │  │  ├─ Rooms.kt
         │  │  ├─ Signaling.kt
         │  │  ├─ SseRoutes.kt
         │  │  └─ Users.kt
         │  └─ resources
         │     ├─ application.yaml
         │     └─ logback.xml
         └─ test
            ├─ kotlin
            │  ├─ AuthenticationTest.kt
            │  ├─ MessagesTest.kt
            │  ├─ Mocks.kt
            │  └─ TestUtils.kt
            └─ resources
               └─ test.yaml

```