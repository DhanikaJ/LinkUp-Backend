/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import entity.Chat;
import entity.Status;
import entity.User;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import util.HibernateUtil;

@ServerEndpoint(value = "/chat")
public class ChatEndPoint {

    private static final Gson GSON = new Gson();
    private int userId;

    @OnOpen
    public void onOpen(Session session) {
        String query = session.getQueryString();
        if (query != null && query.startsWith("userId=")) {
            userId = Integer.parseInt(query.substring("userId=".length()));
            ChatService.register(userId, session);
            UserService.updateLogInStatus(userId);
            UserService.updateFriendChatStatus(userId);
//            ChatService.sendToUser(userId,
//                    ChatService.friendListEnvelope(ChatService.getFriendChatsForUser(userId)));
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (userId > 0) { // userId != null
            ChatService.unregister(userId);
            UserService.updateLogOutStatus(userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        if (userId > 0) {
            UserService.updateLogOutStatus(userId);
        }
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Map<String, Object> map = ChatEndPoint.GSON.fromJson(message, Map.class);
            String type = (String) map.get("type");
            switch (type) {
                case "PING": {
                    JsonObject responseObject = new JsonObject();
                    responseObject.addProperty("type", "PONG");
                    ChatService.sendToUser(userId, responseObject);
                    break;
                }
                case "send_chat": {
                    int fromId = (int) map.get("fromId");
                    int toId = (int) map.get("toId");
                    String chatText = (String) map.get("message");
                    org.hibernate.Session s = HibernateUtil.getSessionFactory().openSession();
                    User fromUser = (User) s.get(User.class, fromId);
                    User toUser = (User) s.get(User.class, toId);

                    if (fromUser != null && toUser != null) {
                        Chat chat = new Chat(fromUser, chatText, toUser, "", Status.SENT);
                        chat.setCreatedAt(new Date());
                        chat.setUpdatedAt(new Date());
                        ChatService.deliverChat(chat);
                    }
                    break;
                }
                case "get_chat_list": {
                    ChatService.sendToUser(userId,
                            ChatService.friendListEnvelope(ChatService.getFriendChatsForUser(userId)));
                    break;
                }
                case "get_single_chat": {
                    int friendId = (int) ((double) map.get("friendId"));
                    List<Chat> chats = ChatService.getChatHistory(userId, friendId);
                    Map<String, Object> envelop = ChatService.singleChatEnvelope(chats);
                    ChatService.sendToUser(userId, envelop);
                    ChatService.sendToUser(userId,
                            ChatService.friendListEnvelope(ChatService.getFriendChatsForUser(userId)));
                    break;
                }
                case "send_message": {
                    int friendId = (int) ((double) map.get("toUserId"));
                    String chat = String.valueOf(map.get("message"));
                    String files = map.containsKey("files") ? String.valueOf(map.get("files")) : "FILE:";
                    ChatService.saveNewChat(userId, friendId, chat, files);
                    break;
                }
                case "get_friend_data": {
                    int friendId = (int) ((double) map.get("friendId"));
                    Map<String, Object> envelope = UserService.getFriendData(friendId);
                    ChatService.sendToUser(userId, envelope);
                    break;
                }
                case "get_all_users": {
                    Map<String, Object> envelope = UserService.getAllUsers(userId);
                    ChatService.sendToUser(userId, envelope);
                    break;
                }
                case "save_new_contact": {
                    LinkedTreeMap userObject = (LinkedTreeMap) map.get("user"); //com.google.gson.internal
                    User user = new User(
                            String.valueOf(userObject.get("firstName")),
                            String.valueOf(userObject.get("lastName")),
                            String.valueOf(userObject.get("countryCode")),
                            String.valueOf(userObject.get("contactNo")));
                    Map<String, Object> envelope = UserService.saveNewContact(userId, user);
                    ChatService.sendToUser(userId, envelope);
                    Map<String, Object> e = UserService.getAllUsers(userId);
                    ChatService.sendToUser(userId, e);
                    break;
                }
                case "set_user_profile": {
                    Map<String, Object> envelope = UserService.getMyProfileData(userId);
                    ChatService.sendToUser(userId, envelope);
                    break;
                }
                case "call_initiate": {
                    // User initiates a call
                    int callerId = userId;
                    int calleeId = (int) ((double) map.get("calleeId"));
                    String callType = (String) map.get("callType"); // "audio" or "video"

                    Map<String, Object> envelope = new HashMap<>();
                    envelope.put("type", "incoming_call");
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("callerId", callerId);
                    payload.put("callType", callType);
                    payload.put("callerName", UserService.getUserName(callerId));
                    payload.put("callerProfileImage", ProfileService.getProfileUrl(callerId));
                    envelope.put("payload", payload);

                    ChatService.sendToUser(calleeId, envelope);
                    break;
                }

                case "call_accept": {
                    // Callee accepts the call
                    int callerId = (int) ((double) map.get("callerId"));

                    Map<String, Object> envelope = new HashMap<>();
                    envelope.put("type", "call_accepted");
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("calleeId", userId);
                    envelope.put("payload", payload);

                    ChatService.sendToUser(callerId, envelope);
                    break;
                }

                case "call_reject": {
                    // Callee rejects the call
                    int callerId = (int) ((double) map.get("callerId"));

                    Map<String, Object> envelope = new HashMap<>();
                    envelope.put("type", "call_rejected");
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("calleeId", userId);
                    envelope.put("payload", payload);

                    ChatService.sendToUser(callerId, envelope);
                    break;
                }

                case "call_end": {
                    // Either party ends the call
                    int otherUserId = (int) ((double) map.get("otherUserId"));

                    Map<String, Object> envelope = new HashMap<>();
                    envelope.put("type", "call_ended");
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("endedBy", userId);
                    envelope.put("payload", payload);

                    ChatService.sendToUser(otherUserId, envelope);
                    break;
                }

                // WebRTC Signaling Messages
                case "webrtc_offer": {
                    int targetUserId = (int) ((double) map.get("targetUserId"));
                    String offer = (String) map.get("offer");

                    Map<String, Object> envelope = new HashMap<>();
                    envelope.put("type", "webrtc_offer");
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("offer", offer);
                    payload.put("fromUserId", userId);
                    envelope.put("payload", payload);

                    ChatService.sendToUser(targetUserId, envelope);
                    break;
                }

                case "webrtc_answer": {
                    int targetUserId = (int) ((double) map.get("targetUserId"));
                    String answer = (String) map.get("answer");

                    Map<String, Object> envelope = new HashMap<>();
                    envelope.put("type", "webrtc_answer");
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("answer", answer);
                    payload.put("fromUserId", userId);
                    envelope.put("payload", payload);

                    ChatService.sendToUser(targetUserId, envelope);
                    break;
                }

                case "webrtc_ice_candidate": {
                    int targetUserId = (int) ((double) map.get("targetUserId"));
                    String candidate = (String) map.get("candidate");

                    Map<String, Object> envelope = new HashMap<>();
                    envelope.put("type", "webrtc_ice_candidate");
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("candidate", candidate);
                    payload.put("fromUserId", userId);
                    envelope.put("payload", payload);

                    ChatService.sendToUser(targetUserId, envelope);
                    break;
                }
                default: {
                    System.out.println("Ignored unknown client type: " + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
