package com.marketlens.user.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "marketlens.bootstrap")
public class BootstrapProperties {

    private Account admin = new Account("admin", "Site Administrator", "admin12345");
    private Account demo = new Account("demo", "Demo Investor", "demo12345");
    private boolean demoEnabled = true;

    public Account getAdmin() {
        return admin;
    }

    public void setAdmin(Account admin) {
        this.admin = admin;
    }

    public Account getDemo() {
        return demo;
    }

    public void setDemo(Account demo) {
        this.demo = demo;
    }

    public boolean isDemoEnabled() {
        return demoEnabled;
    }

    public void setDemoEnabled(boolean demoEnabled) {
        this.demoEnabled = demoEnabled;
    }

    public static class Account {
        private String username;
        private String displayName;
        private String password;

        public Account() {
        }

        public Account(String username, String displayName, String password) {
            this.username = username;
            this.displayName = displayName;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
