package com.devteam.acceleration.jabber;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;

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

    public static enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED;
    }

    public AccelerationJabberConnection(Context context) {
        Log.d(TAG, "RoosterConnection Constructor called.");
        applicationContext = context.getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString(AccelerationJabberParams.JABBER_ID, null);
        password = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString(AccelerationJabberParams.USER_PASSWORD, null);

        if (jid != null) {
            username = jid.split("@")[0];
            serviceName = jid.split("@")[1];
        }
    }

    public void connect() throws InterruptedException, IOException, SmackException, XMPPException {
        Log.d(TAG, "Connecting to server " + serviceName);
        XMPPTCPConnectionConfiguration.Builder builder =
                XMPPTCPConnectionConfiguration.builder();
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setUsernameAndPassword(username, password);
        builder.setHostAddress(InetAddress.getByName(serviceName));
        builder.setXmppDomain(JidCreate.from(serviceName).asDomainBareJid());

        connection = new XMPPTCPConnection(builder.build());
        connection.addConnectionListener(this);
        connection.connect();
        connection.login();
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from serser " + serviceName);

            if (connection != null && connection.isConnected()) {
                connection.disconnect();
            }
        connection = null;
    }

    @Override
    public void connected(XMPPConnection connection) {

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {

    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectionClosedOnError(Exception e) {

    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionFailed(Exception e) {

    }
}
