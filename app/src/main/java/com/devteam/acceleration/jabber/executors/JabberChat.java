package com.devteam.acceleration.jabber.executors;

import android.util.Log;

import com.devteam.acceleration.jabber.JabberModel;
import com.devteam.acceleration.jabber.JabberParams;

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
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.si.provider.StreamInitiationProvider;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private ChatManagerListener chatManagerListener;

    private FileTransferManager fileTransferManager;
    private FileTransferListener fileTransferListener;

    private final Executor executor = Executors.newCachedThreadPool();

    private CallbackMessage callback;

    public interface CallbackMessage {
        void onCallback(Message message, Exception error);
    }

    public enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED, ERROR;
    }

    public static ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public static enum LoggedInState {
        LOGGED_IN, LOGGED_OUT;
    }

    private JabberChat() {
        initializeChatListeners();
        initializeFileTransferListeners();
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

    public void bindCallback(CallbackMessage callback) {
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
                    if (connection == null) {
                        connect();
                    } else if (!connection.isConnected()) {
                        connection.connect();
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

        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);

        connection = new XMPPTCPConnection(builder.build());
        connection.addConnectionListener(this);

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
        ReconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();

        ChatManager.getInstanceFor(connection).addChatListener(chatManagerListener);

        fileTransferManager = FileTransferManager.getInstanceFor(connection);
        fileTransferManager.addFileTransferListener(fileTransferListener);
        FileTransferNegotiator.getInstanceFor(connection);

        connection.connect();
    }

    private void initializeChatListeners() {
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

                notifyUI(message, null);

                Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");
            }
        };

        chatManagerListener = new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                //If the line below is missing ,processMessage won't be triggered and you won't receive messages.
                chat.addMessageListener(chatMessageListener);
            }
        };
    }

    private void initializeFileTransferListeners() {
        fileTransferListener = new FileTransferListener() {
            @Override
            public void fileTransferRequest(FileTransferRequest request) {
                // Check to see if the request should be accepted
//                if(shouldAccept(request)) {
                // Accept it
                IncomingFileTransfer transfer = request.accept();
                try {
                    InputStream input = transfer.recieveFile();
//                    transfer.recieveFile(new File("shakespeare_complete_works.txt"));
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                } else {
//                    // Reject it
//                    request.reject();
//                }
            }
        };

//        ProviderManager.addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
//        ProviderManager.addIQProvider("query","http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
//        ProviderManager.addIQProvider("query","http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

//        ProviderManager.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
//
//        ProviderManager.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
////        ProviderManager.addIQProvider("open", "http://jabber.org/protocol/ibb", new IBBProviders.Open());
////        ProviderManager.addIQProvider("close", "http://jabber.org/protocol/ibb", new IBBProviders.Close());
////        ProviderManager.addExtensionProvider("data", "http://jabber.org/protocol/ibb", new IBBProviders.Data());
//        ProviderManager.addIQProvider("open", "http://jabber.org/protocol/ibb", new OpenIQProvider());
////        ProviderManager.addIQProvider("data", "http://jabber.org/protocol/ibb", new DataPacketProvider());
//        ProviderManager.addIQProvider("close", "http://jabber.org/protocol/ibb", new CloseIQProvider());
////        ProviderManager.addExtensionProvider("data", "http://jabber.org/protocol/ibb", new DataPacketProvider());
    }

    public void loginToChat() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (connection == null) {
                        connect();
                    } else if (!connection.isConnected()) {
                        connection.connect();
                    }

                    if (!connection.isAuthenticated()) {
                        connection.login();
                    }

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

    public void sendMessage(final String body, final String toJid) {
        Log.d(TAG, "Sending message to :" + toJid);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    validateConnection();

                    Chat chat = ChatManager.getInstanceFor(connection)
                            .createChat((EntityJid) JidCreate.from(toJid), chatMessageListener);
                    chat.sendMessage(body);
                    notifyUI(null, null);
                } catch (SmackException | InterruptedException | XMPPException | IOException e) {
                    notifyUI(null, e);
                }
            }
        });
    }

    private void validateConnection() throws InterruptedException, XMPPException, SmackException, IOException {
        if (connection == null) {
            connect();
        } else if (!connection.isConnected()) {
            connection.connect();
        }

        if (!connection.isAuthenticated()) {
            connection.login();
        }
    }


    public void disconnect() {
        Log.d(TAG, "Disconnecting from server " + jabberModel.getServiceName());

        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
        chatMessageListener = null;
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
