package io.smartin.id1212.net.communication;

import com.google.gson.JsonSyntaxException;
import io.smartin.id1212.exceptions.GameException;
import io.smartin.id1212.exceptions.KeyException;
import io.smartin.id1212.exceptions.NicknameException;
import io.smartin.id1212.model.components.ChicagoGame;
import io.smartin.id1212.net.dto.Action;
import io.smartin.id1212.net.dto.Message;
import io.smartin.id1212.controller.PlayerController;
import io.smartin.id1212.net.services.Converter;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import static io.smartin.id1212.net.dto.Message.MessageType.*;

@ServerEndpoint("/game")
public class GameEndpoint {
    private PlayerController playerController;
    private final SessionHandler sessionHandler = SessionHandler.getInstance();

    @OnOpen
    public void onOpen(Session session) {
        playerController = new PlayerController(session.getId());
        sessionHandler.register(session);
    }

    @OnClose
    public void onClose(Session session) {
        ChicagoGame game = playerController.getPlayer().getGame();
        playerController.leaveGame();
        playerController = null;
        sessionHandler.unregister(session);
        sessionHandler.broadcastSnapshots(game);

    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            Action action = Converter.toAction(message);
            playerController.handleAction(action);
            ChicagoGame game = playerController.getPlayer().getGame();
            sessionHandler.broadcastSnapshots(game);
        } catch (JsonSyntaxException e) {
            SessionHandler.sendMsg(session, new Message(JSON_ERROR, e.getMessage()));
        } catch (GameException e) {
            SessionHandler.sendMsg(session, new Message(GAME_ERROR, e.getMessage()));
        } catch (NicknameException e) {
            SessionHandler.sendMsg(session, new Message(NICKNAME_ERROR, e.getMessage()));
        } catch (KeyException e) {
            SessionHandler.sendMsg(session, new Message(KEY_ERROR, e.getMessage()));
        }
    }
}
