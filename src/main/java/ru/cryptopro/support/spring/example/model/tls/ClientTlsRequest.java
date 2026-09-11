package ru.cryptopro.support.spring.example.model.tls;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientTlsRequest {
    private String url;
    private boolean mTLS;
}
