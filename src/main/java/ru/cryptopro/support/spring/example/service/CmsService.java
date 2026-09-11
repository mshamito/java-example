package ru.cryptopro.support.spring.example.service;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.CollectionStore;
import org.springframework.stereotype.Service;
import ru.CryptoPro.AdES.Options;
import ru.CryptoPro.CAdES.*;
import ru.CryptoPro.CAdES.exception.CAdESException;
import ru.CryptoPro.CAdES.exception.EnvelopedException;
import ru.CryptoPro.CAdES.exception.EnvelopedInvalidRecipientException;
import ru.CryptoPro.JCP.tools.AlgorithmUtility;
import ru.cryptopro.support.spring.example.config.pki.StoreConfig;
import ru.cryptopro.support.spring.example.config.StreamWrapperConfig;
import ru.cryptopro.support.spring.example.model.ParsedCertificateInfo;
import ru.cryptopro.support.spring.example.model.cades.CAdESSignatureParameter;
import ru.cryptopro.support.spring.example.model.cades.CAdESSignatureVerifyRequest;
import ru.cryptopro.support.spring.example.model.cades.CAdESSignatureVerifyResult;
import ru.cryptopro.support.spring.example.exception.CryptographicException;
import ru.cryptopro.support.spring.example.utils.cades.CAdESTypeHelper;
import ru.cryptopro.support.spring.example.utils.io.EncodingHelper;
import ru.cryptopro.support.spring.example.utils.io.StreamUpdateHelper;
import ru.cryptopro.support.spring.example.utils.io.StreamWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CmsService {
    private final StoreConfig storeConfig;
    private final X509Certificate certificate;
    private final PrivateKey privateKey;
    private final StreamWrapperConfig streamWrapperConfig;
    private final Set<X509CRL> localCRLs;

    public StreamWrapper encrypt(InputStream data, List<X509Certificate> certs, EncryptionKeyAlgorithm algorithm, boolean encodeToB64) throws Exception {
        EncryptionKeyAlgorithm encryptionKeyAlgorithm = algorithm == null ? EncryptionKeyAlgorithm.ekaKuznechik : algorithm;
        EnvelopedSignature envelopedSignature = new EnvelopedSignature(encryptionKeyAlgorithm);
        if (certs.isEmpty()) {
            // no certs provided. cert from alias will be used as recipient
            envelopedSignature.addKeyTransRecipient(certificate);
        } else {
            for (X509Certificate walk : certs)
                envelopedSignature.addKeyTransRecipient(walk);
        }

        StreamWrapper enveloped = streamWrapperConfig.getStreamWrapperInstance();
        try (
                InputStream inputStream = data
        ) {
            // DER output
            if (!encodeToB64) {
                envelopedSignature.open(enveloped.getOutputStream());
                StreamUpdateHelper.streamUpdateEnvelopedSignature(inputStream, envelopedSignature);
                envelopedSignature.close();
                return enveloped;
            }

            // base64 output
            try (
                    OutputStream wrapped = EncodingHelper.encodeStream(enveloped.getOutputStream())
            ) {
                envelopedSignature.open(wrapped);
                StreamUpdateHelper.streamUpdateEnvelopedSignature(inputStream, envelopedSignature);
                envelopedSignature.close();
                return enveloped;
            }
        }
    }

    public StreamWrapper decrypt(InputStream encryptedCms) throws EnvelopedException, EnvelopedInvalidRecipientException, IOException {
        StreamWrapper streamWrapper = streamWrapperConfig.getStreamWrapperInstance();
        try (
                InputStream tryToGuess = EncodingHelper.decodeDerOrB64Stream(encryptedCms);
                OutputStream outputStream = streamWrapper.getOutputStream()
        ) {
            EnvelopedSignature envelopedSignature = new EnvelopedSignature(tryToGuess);
            envelopedSignature.decrypt(certificate, privateKey, outputStream);
            return streamWrapper;
        }
    }

    public StreamWrapper sign(InputStream data, CAdESSignatureParameter params) throws CAdESException, IOException, CertificateEncodingException {
        StreamWrapper signature = streamWrapperConfig.getStreamWrapperInstance();
        String digestOid = AlgorithmUtility.keyAlgToDigestOid(privateKey.getAlgorithm());
        String keyOid = AlgorithmUtility.keyAlgToKeyAlgorithmOid(privateKey.getAlgorithm());
        int signatureType = CAdESTypeHelper.mapValue(params.getType());
        String tsp = params.getTsp();
        if (Strings.isBlank(tsp) && (signatureType == CAdESType.CAdES_T || signatureType == CAdESType.CAdES_X_Long_Type_1))
            throw new CryptographicException("Tsp address is empty");

        CAdESSignature cAdESSignature = new CAdESSignature(params.isDetached());
        if (signatureType == CAdESType.CAdES_BES || signatureType == CAdESType.CAdES_T) {
            cAdESSignature.setOptions(new Options().disableCertificateValidation());
        }
        boolean addCertChainToSign = params.isAddChain();
        cAdESSignature.addSigner(
                storeConfig.getProviderName(),
                digestOid,
                keyOid,
                privateKey,
                Collections.singletonList(certificate),
                signatureType, // signature type
                tsp, // tsp address
                false, // countersign
                null, // signed attributes
                null, // unsigned attributes
                localCRLs, // set of crl
                addCertChainToSign // add chain
        );

        if (!addCertChainToSign) { // add only one certificate
            Collection<X509CertificateHolder> certificateHolders = new ArrayList<>();
            certificateHolders.add(new X509CertificateHolder(certificate.getEncoded()));
            CollectionStore<X509CertificateHolder> store = new CollectionStore<>(certificateHolders);
            cAdESSignature.setCertificateStore(store);
        }

        boolean encodeToB64 = params.isEncodeToB64();
        try (
                InputStream inputStream = data
        ) {
            // DER output
            if (!encodeToB64) {
                cAdESSignature.open(signature.getOutputStream());
                StreamUpdateHelper.streamUpdateCAdESSignature(inputStream, cAdESSignature);
                cAdESSignature.close();
                return signature;
            }

            // base64 output
            try (
                    OutputStream wrapped = EncodingHelper.encodeStream(signature.getOutputStream())
            ) {
                cAdESSignature.open(wrapped);
                StreamUpdateHelper.streamUpdateCAdESSignature(inputStream, cAdESSignature);
                cAdESSignature.close();
                return signature;
            }
        }
    }

    public List<CAdESSignatureVerifyResult> verify(CAdESSignatureVerifyRequest request) throws CAdESException, IOException {
        List<CAdESSignatureVerifyResult> results = new ArrayList<>();
        try (
                InputStream tryToGuess = EncodingHelper.decodeDerOrB64Stream(request.getSign());
                InputStream dataStream = request.getData()
        ) {
            CAdESSignature signature = new CAdESSignature(tryToGuess, dataStream, null);
            signature.verify(null, localCRLs); // no exception ? everything is ok.
            CAdESSigner[] signers = signature.getCAdESSignerInfos();
            for (int i = 0; i < signers.length; i++) {
                CAdESSigner signer = signers[i];
                CAdESSignatureVerifyResult result = new CAdESSignatureVerifyResult();
                result.setId(i);
                result.setCAdESType(CAdESTypeHelper.mapValue(
                        signer.getSignatureType()
                ));
                X509Certificate cert = signer.getSignerCertificate();
                result.setCert(new ParsedCertificateInfo(cert));
                results.add(result);
            }
        }
        return results;
    }
}