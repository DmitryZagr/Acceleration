package com.devteam.acceleration.jabber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.util.Log;

import com.devteam.acceleration.ui.ChatActivity;

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
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by admin on 13.04.17.
 */

public class AccelerationJabberConnection implements ConnectionListener {

    private static final String TAG = AccelerationJabberConnection.class.getSimpleName();

    private XMPPTCPConnection connection;
    private Context applicationContext;
    private String username = "";
    private String password = "";
    private String serviceName = "";
    private ChatMessageListener chatMessageListener;
    private BroadcastReceiver uiThreadMessageReceiver;

    private final String BOOT_ID = "user@192.168.1.65";

    public  enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED, ERROR;
    }

    public static enum LoggedInState {
        LOGGED_IN, LOGGED_OUT;
    }

    public AccelerationJabberConnection(Context context) {
        Log.d(TAG, "RoosterConnection Constructor called.");
        applicationContext = context.getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString(AccelerationJabberParams.JABBER_ID, null);
        password = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString(AccelerationJabberParams.USER_PASSWORD, null);

        if (jid != null) {
            String[] params = jid.split("@");
            username = params[0];
            serviceName = params[1];
        }

        setupUiThreadBroadCastMessageReceiver();

    }

    public void connect() throws InterruptedException, IOException, SmackException, XMPPException {
        Log.d(TAG, "Connecting to server " + serviceName);
        XMPPTCPConnectionConfiguration.Builder builder =
                XMPPTCPConnectionConfiguration.builder();
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setUsernameAndPassword(username, password);
        builder.setHostAddress(InetAddress.getByName(serviceName));
        builder.setXmppDomain(JidCreate.from(serviceName).asDomainBareJid());
        builder.setConnectTimeout(1000);

        connection = new XMPPTCPConnection(builder.build());
        connection.addConnectionListener(this);
        connection.connect();
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
                //Bundle up the intent and send the broadcast.
                Intent intent = new Intent(AccelerationConnectionService.NEW_MESSAGE);
                intent.setPackage(applicationContext.getPackageName());
                intent.putExtra(AccelerationConnectionService.BUNDLE_FROM_JID, contactJid);
                intent.putExtra(AccelerationConnectionService.MESSAGE_BODY, message.getBody());
                applicationContext.sendBroadcast(intent);
                Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");
                ///ADDED
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
        reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();
    }

    private void setupUiThreadBroadCastMessageReceiver() {
            uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if (action.equals(AccelerationConnectionService.SEND_MESSAGE)) {
                    //Send the message.
                    sendMessage(intent.getStringExtra(AccelerationConnectionService.MESSAGE_BODY),
                            intent.getStringExtra(AccelerationConnectionService.BUNDLE_TO /* BOOT_ID*/));
                }
            }
        };

        IntentFilter filter = new IntentFilter(AccelerationConnectionService.SEND_MESSAGE);
        filter.addAction(AccelerationConnectionService.SEND_MESSAGE);
        applicationContext.registerReceiver(uiThreadMessageReceiver, filter);

    }

    public void sendMessage(String body, String toJid) {
        Log.d(TAG, "Sending message to :" + toJid);
        try {
            Chat chat = ChatManager.getInstanceFor(connection)
                    .createChat((EntityJid) JidCreate.from(toJid), chatMessageListener);
            chat.sendMessage(body);
        } catch (SmackException.NotConnectedException | XmppStringprepException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from server " + serviceName);

        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
        connection = null;
    }


    @Override
    public void connected(XMPPConnection connection) {
        AccelerationConnectionService.connectionState = ConnectionState.CONNECTED;
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d(TAG, "Authenticated Successfully");
        AccelerationConnectionService.connectionState = ConnectionState.AUTHENTICATED;
        showChatActivity();
    }

    @Override
    public void connectionClosed() {
        AccelerationConnectionService.connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        AccelerationConnectionService.connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public void reconnectionSuccessful() {
        AccelerationConnectionService.connectionState = ConnectionState.CONNECTED;
    }

    @Override
    public void reconnectingIn(int seconds) {
        AccelerationConnectionService.connectionState = ConnectionState.CONNECTING;
    }

    @Override
    public void reconnectionFailed(Exception e) {
        AccelerationConnectionService.connectionState = ConnectionState.DISCONNECTED;
    }

    private void showChatActivity() {
        Intent intent = new Intent(applicationContext, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        applicationContext.startActivity(intent);
    }
}
