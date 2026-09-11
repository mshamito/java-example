package ru.cryptopro.support.spring.example.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.CryptoPro.CAdES.EncryptionKeyAlgorithm;
import ru.CryptoPro.CAdES.exception.CAdESException;
import ru.CryptoPro.CAdES.exception.EnvelopedException;
import ru.CryptoPro.CAdES.exception.EnvelopedInvalidRecipientException;
import ru.cryptopro.support.spring.example.model.cades.CAdESSignatureParameter;
import ru.cryptopro.support.spring.example.model.cades.CAdESSignatureVerifyRequest;
import ru.cryptopro.support.spring.example.model.cades.CAdESSignatureVerifyResult;
import ru.cryptopro.support.spring.example.exception.CryptographicException;
import ru.cryptopro.support.spring.example.exception.ProvidedDataException;
import ru.cryptopro.support.spring.example.service.CmsService;
import ru.cryptopro.support.spring.example.utils.certificate.CastX509Helper;
import ru.cryptopro.support.spring.example.utils.misc.HeadersHelper;
import ru.cryptopro.support.spring.example.utils.io.StreamWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
public class CAdESSignatureController {
    private final CmsService cmsService;
    private final X509Certificate x509Certificate;

    @PostMapping(value = "/encr")
    public ResponseEntity<StreamingResponseBody> encrypt(
            @RequestParam(value = "data")
            MultipartFile data,
            @RequestParam(required = false, value = "cert")
            List<MultipartFile> certs,
            @RequestParam(required = false, value = "alg")
            @Schema(defaultValue = "ekaKuznechik", type = "EncryptionKeyAlgorithm", description = "ekaDefault, ekaMagma, ekaMagmaMac, ekaKuznechik, ekaKuznechikMac")
            EncryptionKeyAlgorithm algorithm,
            @RequestParam(required = false, defaultValue = "false")
            @Schema(defaultValue = "false", type = "boolean")
            boolean encodeToB64
    ) {
        if (data.isEmpty())
            throw new ProvidedDataException("Provided data is empty");
        List<X509Certificate> x509Certificates = certs == null ?
                Collections.singletonList(x509Certificate) :
                CastX509Helper.castCertificates(certs);

        HttpHeaders headers = HeadersHelper.prepareHeaders(data.getOriginalFilename(), ".enc");
        MediaType mediaType = encodeToB64 ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;

        try (
                InputStream inputStream = data.getInputStream()
        ) {
            StreamWrapper enveloped = cmsService.encrypt(inputStream, x509Certificates, algorithm, encodeToB64);
            StreamingResponseBody response = enveloped::writeTo;
            return ResponseEntity.ok().headers(headers).contentType(mediaType).body(response);
        } catch (Exception e) {
            throw new CryptographicException("Encrypt failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/decr")
    public ResponseEntity<StreamingResponseBody> decrypt(
            @RequestParam(value = "cms") MultipartFile encryptedCms
    ) throws IOException {
        if (encryptedCms.isEmpty())
            throw new ProvidedDataException("Provided data is empty");

        HttpHeaders headers = HeadersHelper.prepareHeaders(encryptedCms.getOriginalFilename(), ".decrypted");

        try (
                InputStream inputStream = encryptedCms.getInputStream()
        ) {
            StreamWrapper decrypted = cmsService.decrypt(inputStream);
            StreamingResponseBody response = decrypted::writeTo;
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(response);
        } catch (EnvelopedException | EnvelopedInvalidRecipientException e) {
            throw new CryptographicException("Decrypt failed: " + e.getMessage());
        }
    }

    @PostMapping("/sign")
    public ResponseEntity<StreamingResponseBody> sign(
            @RequestParam MultipartFile data,
            @RequestParam(required = false, defaultValue = "true") @Schema(defaultValue = "true", type = "boolean") boolean detached,
            @RequestParam(required = false) @Schema(defaultValue = "http://testca2012.cryptopro.ru/tsp/tsp.srf") String tsp,
            @RequestParam(required = false) @Schema(defaultValue = "bes", description = "bes, t, xlt1, a") String type,
            @RequestParam(required = false, defaultValue = "false") @Schema(defaultValue = "false", type = "boolean") boolean encodeToB64,
            @RequestParam(required = false, defaultValue = "true") @Schema(defaultValue = "true", type = "boolean") boolean addChain
    ) {
        if (data.isEmpty())
            throw new ProvidedDataException("Provided data is empty");

        CAdESSignatureParameter.CAdESSignatureParameterBuilder builder = CAdESSignatureParameter.builder();
        builder.detached(detached);
        builder.encodeToB64(encodeToB64);
        builder.addChain(addChain);
        if (Strings.isNotBlank(tsp))
            builder.tsp(tsp);
        if (Strings.isNotBlank(type))
            builder.type(type);
        CAdESSignatureParameter params = builder.build();

        HttpHeaders headers = HeadersHelper.prepareHeaders(data.getOriginalFilename(), ".sig");
        MediaType mediaType = encodeToB64 ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM;

        try (
                InputStream inputStream = data.getInputStream()
        ) {
            StreamWrapper signature = cmsService.sign(inputStream, params);
            StreamingResponseBody response = signature::writeTo;
            return ResponseEntity.ok().headers(headers).contentType(mediaType).body(response);
        } catch (CAdESException | IOException | CertificateEncodingException e) {
            throw new CryptographicException("Sign failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CAdESSignatureVerifyResult> verify(
            @RequestParam MultipartFile sign,
            @RequestParam(required = false) MultipartFile data
    ) {
        try {
            return cmsService.verify(new CAdESSignatureVerifyRequest(sign, data));
        } catch (CAdESException | IOException e) {
            throw new CryptographicException(e.getMessage());
        }
    }
}
