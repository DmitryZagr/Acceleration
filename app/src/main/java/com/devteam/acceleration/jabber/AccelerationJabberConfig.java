package com.devteam.acceleration.jabber;

/**
 * Created by admin on 13.04.17.
 */

public class AccelerationJabberConfig {

    private static final int DEFAULT_PORT = 5222;

    private String userName;
    private String userPassword;
    private String userResource;
    private String userService;
    private String serviceServer;
    private int servicePort = DEFAULT_PORT;

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getUserResource() {
        return userResource;
    }

    public String getUserService() {
        return userService;
    }

    public String getServiceServer() {
        return serviceServer;
    }

    public int getServicePort() {
        return servicePort;
    }

    public static class AccelerationJabberConfigBuilder {

        private String userName;
        private String userPassword;
        private String userResource;
        private String userService;
        private String serviceServer;
        private int servicePort = DEFAULT_PORT;

        public AccelerationJabberConfigBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public AccelerationJabberConfigBuilder userPassword(String userPassword) {
            this.userPassword = userPassword;
            return this;
        }

        public AccelerationJabberConfigBuilder userResource(String userResource) {
            this.userResource = userResource;
            return this;
        }

        public AccelerationJabberConfigBuilder userService(String userService) {
            this.userService = userService;
            return this;
        }

        public AccelerationJabberConfigBuilder servicePort(int servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public AccelerationJabberConfig build() {
            AccelerationJabberConfig config = new AccelerationJabberConfig();
            config.userName = this.userName;
            config.userPassword = this.userPassword;
            config.userResource = this.userResource;
            config.userService = this.userService;
            config.serviceServer = this.serviceServer;
            return config;
        }


    }

}
