package ru.cryptopro.support.spring.example.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.cryptopro.support.spring.example.model.CertificateVerifyResult;
import ru.cryptopro.support.spring.example.service.CertificateVerifyService;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public class CertificateVerifyController {
    private final CertificateVerifyService certificateVerifyService;

    @PostMapping("/cert")
    public CertificateVerifyResult validateCert(
            @RequestParam(value = "cert") MultipartFile cert
    ) {
        return certificateVerifyService.validateCertificate(cert);
    }
}
