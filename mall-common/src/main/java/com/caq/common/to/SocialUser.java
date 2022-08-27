package com.caq.common.to;

import lombok.Data;

@Data
public class SocialUser {
    private String access_token;
    private String remind_in;
    private String uid;
    private String isRealName;
    private long expires_in;
}
