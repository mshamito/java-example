package ru.cryptopro.support.spring.example.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cryptopro.support.spring.example.model.tls.ClientTlsRequest;
import ru.cryptopro.support.spring.example.model.tls.ClientTlsResult;
import ru.cryptopro.support.spring.example.service.ClientTlsService;

@CrossOrigin
@RestController
@RequiredArgsConstructor
//@RequestMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class ClientTlsController {
    private final ClientTlsService tlsService;

    @PostMapping("/tls")
    public ResponseEntity<ClientTlsResult> tls(
            @RequestParam(required = false, defaultValue = "false") @Schema(defaultValue = "false", type = "boolean")
            boolean mTLS,
            @RequestParam @Schema(defaultValue = "https://cryptopro.ru")
            String url
    ) {
        ClientTlsResult result = tlsService.connect(
                ClientTlsRequest.builder()
                        .url(url)
                        .mTLS(mTLS)
                        .build()
        );
        return ResponseEntity
                .status(result.getCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }
}
