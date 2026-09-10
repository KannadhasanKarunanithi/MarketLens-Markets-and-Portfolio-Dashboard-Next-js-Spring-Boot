package com.marketlens.user.bootstrap;

import java.util.Set;

import com.marketlens.user.Role;
import com.marketlens.user.UserAccount;
import com.marketlens.user.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableConfigurationProperties(BootstrapProperties.class)
public class AccountInitializer {

    private static final Logger log = LoggerFactory.getLogger(AccountInitializer.class);

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties properties;

    public AccountInitializer(UserAccountRepository users, PasswordEncoder passwordEncoder,
            BootstrapProperties properties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    @Transactional
    public void createAccounts() {
        createIfMissing(properties.getAdmin(), Set.of(Role.ADMIN, Role.USER));
        if (properties.isDemoEnabled()) {
            createIfMissing(properties.getDemo(), Set.of(Role.USER));
        }
    }

    private void createIfMissing(BootstrapProperties.Account account, Set<Role> roles) {
        if (users.existsByUsername(account.getUsername())) {
            return;
        }
        users.save(new UserAccount(
                account.getUsername(),
                account.getDisplayName(),
                passwordEncoder.encode(account.getPassword()),
                roles));
        log.info("Created bootstrap account {}", account.getUsername());
    }
}
