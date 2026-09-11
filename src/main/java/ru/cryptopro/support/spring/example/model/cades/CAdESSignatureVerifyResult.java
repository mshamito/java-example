package ru.cryptopro.support.spring.example.model.cades;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.cryptopro.support.spring.example.model.ParsedCertificateInfo;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CAdESSignatureVerifyResult {
    private int id;
    @JsonProperty("type")
    private String CAdESType;
    private ParsedCertificateInfo cert;
}
