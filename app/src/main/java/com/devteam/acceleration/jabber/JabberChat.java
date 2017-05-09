package com.devteam.acceleration.jabber;

import android.util.Log;

import com.devteam.acceleration.jabber.executors.Ui;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Created by admin on 13.04.17.
 */

public class JabberChat implements ConnectionListener {

    private static final String TAG = JabberChat.class.getSimpleName();

    private static final JabberChat JABBER_CHAT = new JabberChat();

    public static JabberChat getJabberChat() {
        return JABBER_CHAT;
    }

    private XMPPTCPConnection connection;
    private JabberModel jabberModel;
    private ChatMessageListener chatMessageListener;

    private final Executor executor = Executors.newCachedThreadPool();

    private Callback callback;

    public interface Callback {
        void onCallback(Message message, Exception error);
    }

    public enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED, ERROR;
    }

    public static ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public static enum LoggedInState {
        LOGGED_IN, LOGGED_OUT;
    }

    public void setJabberModel(JabberModel jabberModel) {
        this.jabberModel = jabberModel;

        String jid = jabberModel.getJabberId();

        if (jid != null) {
            String[] params = jid.split("@");
            this.jabberModel.setJabberId(params[0]);
            this.jabberModel.setServiceName(params[1]);
        }
    }

    public void bindCallback(Callback callback) {
        this.callback = callback;
    }

    public void unbindCallback() {
        this.callback = null;
    }


    public void createAccount() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (connection == null || !connection.isConnected()) {
                        connect();
                    }

                    AccountManager accountManager = AccountManager.getInstance(connection);
                    accountManager.sensitiveOperationOverInsecureConnection(true);
                    Map<String, String> atr = new HashMap<>();
                    atr.put(JabberParams.USER_EMAIL, jabberModel.getEmail());
                    atr.put(JabberParams.USER_NAME, jabberModel.getName());
                    accountManager.createAccount(Localpart.from(jabberModel.getJabberId()), jabberModel.getPassword(), atr);

                    loginToChat();
                    callback.onCallback(null, null);

                } catch (InterruptedException | IOException | SmackException | XMPPException e) {
                    notifyUI(null, e);
                }
            }
        });
    }

    private void connect() throws InterruptedException, IOException, SmackException, XMPPException {
        Log.d(TAG, "Connecting to server " + jabberModel.getServiceName());
        XMPPTCPConnectionConfiguration.Builder builder =
                XMPPTCPConnectionConfiguration.builder();
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setUsernameAndPassword(jabberModel.getJabberId(), jabberModel.getPassword());
        builder.setHostAddress(InetAddress.getByName(jabberModel.getServiceName()));
        builder.setXmppDomain(JidCreate.from(jabberModel.getServiceName()).asDomainBareJid());
        builder.setConnectTimeout(3000);

        connection = new XMPPTCPConnection(builder.build());
        connection.addConnectionListener(this);
        connection.connect();
    }

    public void loginToChat() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (connection == null || !connection.isConnected()) {
                        connect();
                    }
                    connection.login();
                    chatMessageListener = new ChatMessageListener() {
                        @Override
                        public void processMessage(Chat chat, Message message) {
                            ///ADDED
                            Log.d(TAG, "message.getBody() :" + message.getBody());
                            Log.d(TAG, "message.getFrom() :" + message.getFrom());

                            String from = message.getFrom().toString();
                            String contactJid = "";
                            if (from.contains("/")) {
                                contactJid = from.split("/")[0];
                                Log.d(TAG, "The real jid is :" + contactJid);
                            } else {
                                contactJid = from;
                            }
                            Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");
                        }
                    };

                    //The snippet below is necessary for the message listener to be attached to our connection.
                    ChatManager.getInstanceFor(connection).addChatListener(new ChatManagerListener() {
                        @Override
                        public void chatCreated(Chat chat, boolean createdLocally) {

                            //If the line below is missing ,processMessage won't be triggered and you won't receive messages.
                            chat.addMessageListener(chatMessageListener);
                        }
                    });

                    ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
                    ReconnectionManager.setEnabledPerDefault(true);
                    reconnectionManager.enableAutomaticReconnection();

                    notifyUI(null, null);

                } catch (InterruptedException | IOException | SmackException | XMPPException e) {
                    notifyUI(null, e);
                }
            }
        });
    }

    private void notifyUI(final Message message, final Exception e) {
        Ui.run(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.onCallback(message, e);
            }
        });
    }


    public void disconnect() {
        Log.d(TAG, "Disconnecting from server " + jabberModel.getServiceName());

        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
        connection = null;
    }


    @Override
    public void connected(XMPPConnection connection) {
        connectionState = ConnectionState.CONNECTED;
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d(TAG, "Authenticated Successfully");
        connectionState = ConnectionState.AUTHENTICATED;
    }

    @Override
    public void connectionClosed() {
        connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public void reconnectionSuccessful() {
        connectionState = ConnectionState.CONNECTED;
    }

    @Override
    public void reconnectingIn(int seconds) {
        connectionState = ConnectionState.CONNECTING;
    }

    @Override
    public void reconnectionFailed(Exception e) {
        connectionState = ConnectionState.DISCONNECTED;
    }

}
