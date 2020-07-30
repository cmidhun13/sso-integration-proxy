package com.szells.sso.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CustomerHeaders {
    private String firstName;
    private String lastName;
    private String userName;
    private String secureKey;
    private String email;
    private String groups;

}
