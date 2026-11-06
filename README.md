# LinkUp Backend

**LinkUp-Backend** is the server for the LinkUp chat app: a Java web application (servlets plus a WebSocket endpoint) that stores users, contacts and messages in MySQL through Hibernate. It serves the [LinkUp mobile app](https://github.com/DhanikaJ/LinkUp) and is deployed as a WAR file on GlassFish.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-Servlets%20%2B%20WebSocket-orange)]()
[![MySQL](https://img.shields.io/badge/Database-MySQL-blue)]()
[![Hibernate](https://img.shields.io/badge/ORM-Hibernate%204.3-59666C)]()

> **Status:** academic prototype for local development and demos. It has no authentication, so it is **not** safe for real users. See [Security notes](#security-notes).

## Repositories

| Repository | Role | Technology |
|---|---|---|
| [LinkUp](https://github.com/DhanikaJ/LinkUp) | Mobile client | Expo, React Native, TypeScript |
| **LinkUp-Backend** (this repo) | Chat server, uploads, database | Java servlets, WebSocket, Hibernate, MySQL, GlassFish |

## Features

- **Account creation** with first name, last name, country code, phone number and a profile photo (`POST /UserController`). The phone number must be unique.
- **Real-time chat** over a WebSocket (`/chat`): messages are saved to MySQL and pushed instantly to the recipient if they are online
- **Chat list** with the last message, time and unread count, and **message history** per conversation. Opening a chat marks the other person's delivered messages as read.
- **Contacts:** a per-user friend list with display names; add a contact by name and phone number
- **Online status:** users are marked online or offline when their WebSocket opens or closes
- **Profile photos and chat attachments:** images, videos, audio and documents are uploaded over HTTP and served as static files
- **Call signaling relay:** `call_initiate`, `call_accept`, `call_reject`, `call_end`, `webrtc_offer`, `webrtc_answer` and `webrtc_ice_candidate` messages are forwarded between users. There is no media server, and the mobile app does not use these yet. `web/testCalling.js` is a test script for them.

## Technology Stack

| Area | Technology |
|---|---|
| Language | Java (project compile level 1.7) |
| Web layer | Java servlets (`javax.servlet`) with `@MultipartConfig` file uploads |
| Real-time | Java WebSocket API (`javax.websocket`, `@ServerEndpoint("/chat")`) |
| JSON | Gson |
| Persistence | Hibernate 4.3.1 (Criteria API, annotated entities), MySQL Connector/J 8.4.0 |
| Database | MySQL |
| Server | GlassFish 5 |
| IDE / build | Apache NetBeans, Apache Ant |

## Project Structure

```text
LinkUp-Backend/
├── src/java/
│   ├── controller/        # Servlets: UserController, ProfileController, MediaController, TestServlet
│   ├── socket/            # ChatEndPoint (WebSocket), ChatService, UserService, ProfileService
│   ├── entity/            # Hibernate entities: User, Chat, FriendList, Status, BaseEntity
│   ├── dto/               # Objects sent to the app
│   ├── util/              # HibernateUtil (session factory)
│   └── hibernate.cfg.xml  # Database connection and mapped classes
├── web/                   # index.html, testCalling.js, WEB-INF/glassfish-web.xml
├── lib/                   # Library JARs used by the project
├── nbproject/             # NetBeans project configuration
├── build.xml              # Apache Ant build script
└── README.md
```

The context path is `/LinkUp` (the WAR is named `LinkUp.war`).

## Database

Create a MySQL database for the app (its name must match your connection URL). No schema script is included yet, and `hibernate.hbm2ddl.auto` is commented out in `hibernate.cfg.xml`, so Hibernate will not create the tables for you. Either create them by hand from the table below, or enable `<property name="hibernate.hbm2ddl.auto">update</property>` locally while developing.

| Table | Columns |
|---|---|
| `user` | `id`, `first_name`, `last_name`, `country_code`, `contact_no`, `status`, `created_at`, `updated_at` |
| `chat` | `id`, `from_user` (FK to `user`), `to_user` (FK to `user`), `message`, `files`, `status`, `created_at`, `updated_at` |
| `friend_list` | `id`, `user_id` (FK), `friend_id` (FK), `user_status`, `display_name`, `created_at`, `updated_at` |

`status` columns hold values of the `Status` enum (for example `ACTIVE`, `SENT`, `DELIVERED`, `READ`, `ONLINE`, `OFFLINE`). Check `src/java/entity/` for the exact column definitions before writing a schema by hand.

## Setup

### Requirements

- JDK 8 (GlassFish 5 runs on Java 8)
- GlassFish 5.x
- MySQL 8
- Apache NetBeans (recommended) or Apache Ant
- The library JARs are in `lib/`. If NetBeans reports missing libraries, re-add them from that folder to the project.

### 1. Database credentials

Open `src/java/hibernate.cfg.xml` and set your own connection URL, user name and password:

```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/YOUR_DATABASE?useSSL=false</property>
<property name="hibernate.connection.username">YOUR_USER</property>
<property name="hibernate.connection.password">YOUR_PASSWORD</property>
```

> **Never commit real credentials.** Keep your local edit out of git (for example with `git update-index --assume-unchanged src/java/hibernate.cfg.xml`), or move the settings to a git-ignored file or environment variables. If a password was ever committed, change it.

### 2. The public base URL

`socket/ChatService.java` has a constant `URL` used to build the links to profile photos and uploaded files. Set it to the address your phone can reach (for example `http://<your-computer-LAN-IP>:8080`, or your tunnel or hosting URL). `socket/ProfileService.java` also contains a `http://localhost:8080/LinkUp/...` address that should be changed the same way when you deploy elsewhere.

### 3. Build and deploy

With NetBeans: open the project, then right-click it and choose **Run**. It deploys to GlassFish and serves the app at `http://localhost:8080/LinkUp`.

With Ant:

```bash
ant clean dist        # creates dist/LinkUp.war
asadmin deploy dist/LinkUp.war
```

Uploaded files are saved under `web/profile-images/<userId>/` and `web/chat-media/<userId>/`. The code finds these folders by replacing `build/web` in the deployed path with `web`, so uploads only land in the source tree when the app is run from NetBeans.

### 4. Connect the mobile app

In the app's `.env`, point `EXPO_PUBLIC_APP_URL` at `http://<host>:8080` and `EXPO_PUBLIC_WS_FULL_URL` at `ws://<host>:8080/LinkUp`. See the [frontend README](https://github.com/DhanikaJ/LinkUp#configure-the-backend-address).

## HTTP API

Base URL: `http://<host>:8080/LinkUp`. The upload endpoints accept `multipart/form-data` and return JSON.

| Method | Endpoint | Form fields | Response |
|---|---|---|---|
| `POST` | `/UserController` | `firstName`, `lastName`, `countryCode`, `contactNo`, `profileImage` (all required) | `{ "status": true, "userId": <id>, "user": {...} }`, or `{ "status": false, "message": "..." }` (for example "This contact number already exists!") |
| `POST` | `/ProfileController` | `userId`, `profileImage` | `{ "status": true, "message": "..." }` or `status: false` with a message |
| `POST` | `/MediaController` | `userId`, `media` (max 10 MB per file, 50 MB per request) | `{ "success": true, "filePath": "<url>", "fileName", "fileType", "timestamp" }`. Allowed types: jpg, jpeg, png, gif, webp, mp4, mov, avi, mkv, mp3, wav, m4a, ogg, pdf, doc, docx, xls, xlsx, ppt, pptx |
| `GET` | `/MediaController` | none | A message telling you to use `POST` |

Uploaded files are served as plain static files under `/LinkUp/profile-images/<userId>/` and `/LinkUp/chat-media/<userId>/`.

## WebSocket protocol

Connect to `ws://<host>:8080/LinkUp/chat?userId=<id>`. Every message is a JSON object with a `type`.

**Client to server**

| `type` | Extra fields | What happens |
|---|---|---|
| `PING` | none | The server replies with `PONG` |
| `get_chat_list` | none | Replies with `friend_list` (your chats with last message, time and unread count) |
| `get_single_chat` | `friendId` | Replies with `single_chat` (history) and a refreshed `friend_list`; marks the friend's delivered messages as read |
| `send_message` | `toUserId`, `message`, optional `files` | Saves the message and delivers it to the recipient as `new_message` if they are online |
| `send_chat` | `fromId`, `toId`, `message` | Older variant that saves and delivers a text message |
| `get_friend_data` | `friendId` | Replies with `friend_data` (name, photo, online status) |
| `get_all_users` | none | Replies with `all_users` (your contacts) |
| `save_new_contact` | `user` (`firstName`, `lastName`, `countryCode`, `contactNo`) | Adds a contact and replies with `new_contact_response_text` |
| `set_user_profile` | none | Replies with `user_profile` (your own details and photo) |
| `call_initiate`, `call_accept`, `call_reject`, `call_end`, `webrtc_offer`, `webrtc_answer`, `webrtc_ice_candidate` | call and signaling data | Forwarded to the other user as `incoming_call`, `call_accepted`, `call_rejected`, `call_ended`, `webrtc_offer`, `webrtc_answer` and `webrtc_ice_candidate` |

**Server to client:** `PONG`, `friend_list`, `single_chat`, `chat`, `new_message`, `friend_data`, `all_users`, `new_contact_response_text`, `user_profile`, plus the call messages above.

Open connections are kept in a `ConcurrentHashMap` keyed by user id, so a user has one active connection at a time.

## Security notes

This project was built to learn real-time client-server development, and security is intentionally minimal. Fix these before any real use:

- **No authentication.** An account is a name and a phone number, with no password or verification code. The WebSocket trusts the `userId` in the URL, so anyone can connect as any user, read their chats and send messages as them.
- **No CORS headers** are set. Native apps are not affected, but browser clients on other origins will be blocked. Add an explicit allow-list if you build a web client.
- **Uploads are public.** Files are served as static files, so anyone with a URL can open them.
- **Credentials:** `hibernate.cfg.xml` is tracked in git and holds the database connection settings. Keep real credentials out of the repository.
- **Verbose logging:** `printStackTrace` calls and `hibernate.show_sql` are left on.

## Known Limitations

- No schema script, no seed data and no automated tests
- Hard-coded addresses in the source: the public `URL` constant in `ChatService` and a `localhost` address in `ProfileService`
- Old library versions (Hibernate 4.3.1 dates from 2014) and a Java 1.7 compile level
- Reactions, replies and message deletion are not stored on the server
- One WebSocket connection per user, and no group chat
- `TestServlet` and `web/index.html` are placeholders
- Compiled output (`build/`, `dist/`), `nbproject/private/` and uploaded profile photos were committed to the repository. They should be listed in a `.gitignore` and removed from git.

## Future Enhancements

- Real authentication (phone number plus OTP, or a password) and a server-issued token for the WebSocket
- Store uploads outside the web folder and serve them with access checks
- Reactions, replies, message deletion and group chats
- Push notifications and offline delivery
- A `schema.sql`, database migrations, and settings from environment variables
- A Dockerfile (GlassFish or Tomcat plus MySQL) for easy hosting
- Unit and integration tests

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Author

**DhanikaJ**  
Portfolio: [https://dhanikaj.dev](https://dhanikaj.dev)
