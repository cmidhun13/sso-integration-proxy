package com.szells.sso.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CutomerGraphQLQuery {
    @JsonProperty("variables")
    private Object variables;

}
