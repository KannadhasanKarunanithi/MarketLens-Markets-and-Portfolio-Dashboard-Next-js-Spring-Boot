package com.marketlens.user;

import java.util.List;

public record UserSummary(String username, String displayName, List<String> roles) {

    public static UserSummary from(UserAccount account) {
        return new UserSummary(
                account.getUsername(),
                account.getDisplayName(),
                account.getRoles().stream().map(Enum::name).sorted().toList());
    }
}
