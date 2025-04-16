import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.FileOutputStream;
import java.security.KeyStore.PasswordProtection;
import java.util.List;

@Command(name = "Pkcs11XmlSigner", mixinStandardHelpOptions = true, description = "Sign XML files using PKCS11.")
public class Pkcs11XmlSigner implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(Pkcs11XmlSigner.class);
    ;
    @Option(names = {"--pin"}, description = "The pin to access your signing asset. If using the pteid library it will still ask for the pin in a GUI dialog.", required = true)
    String pin = "XXXX";

    @Option(names = {"--algorithm"}, description = "The algorithm to sign. Defaults to SHA256.")
    String algorithm = "SHA256";

    @Option(names = {"--level"}, description = "The signature level. Defaults to XAdES_BASELINE_B.")
    String signatureLevel = "XAdES_BASELINE_B";

    @Option(names = {"--packaging"}, description = "The signature packaging. Defaults to ENVELOPED.")
    String signaturePackaging = "ENVELOPED";

    @Option(names = {"--slot"}, description = "The slot in the card where the signing token is. Defaults to 1 (with pteid cards)")
    int slot = 1;
    @Option(names = {"--key-index"}, description = "The index of the key to be used. Defaults to 0. NOTE: If using the pteid official library use slot 0 and key index 1.")
    int keyIndex = 0;

    @Option(names = {"--pkcs11-lib"}, description = "The library used to access the card reader. Defaults to /usr/local/lib/opensc-pkcs11.so")
    String pkcs11LibPath = "/usr/local/lib/opensc-pkcs11.so";

    @Parameters(paramLabel = "file", description = "The XML file to sign.")
    String filename;


    public static void main(String[] args) {
        int exitCode = new CommandLine(new Pkcs11XmlSigner()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            if (!filename.endsWith(".xml")) {
                logger.error("File must end in .xml");
                System.exit(1);
            }

            DSSDocument toSignDocument = new FileDocument(filename);

            XAdESSignatureParameters parameters = new XAdESSignatureParameters();
            parameters.setSignatureLevel(SignatureLevel.valueOf(signatureLevel));
            parameters.setSignaturePackaging(SignaturePackaging.valueOf(signaturePackaging.toUpperCase()));
            parameters.setDigestAlgorithm(DigestAlgorithm.valueOf(algorithm.toUpperCase()));

            try (Pkcs11SignatureToken token = new Pkcs11SignatureToken(
                    pkcs11LibPath, new PasswordProtection(pin.toCharArray()), slot)) {

                logger.info("Listing keys in slot {}", slot);
                for (DSSPrivateKeyEntry key : token.getKeys()) {
                    logger.info("Key: {}", key.getCertificate().getIssuerX500Principal());
                }

                List<DSSPrivateKeyEntry> keys = token.getKeys();
                DSSPrivateKeyEntry signingKey = keys.get(keyIndex);

                parameters.setSigningCertificate(signingKey.getCertificate());
                parameters.setCertificateChain(signingKey.getCertificateChain());

                logger.info("Signing key is: {}", signingKey.getCertificate().getIssuerX500Principal());
                logger.info("Listing keys in chain...");
                for (CertificateToken certificateToken : signingKey.getCertificateChain()) {
                    logger.info("Key {}", certificateToken.getIssuerX500Principal());
                }

                var verifier = new CommonCertificateVerifier();

                XAdESService service = new XAdESService(verifier);
                ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

                SignatureValue signatureValue = token.sign(
                        dataToSign, parameters.getDigestAlgorithm(), signingKey);

                DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);

                try (FileOutputStream fos = new FileOutputStream(filename.replace(".xml", "_signed.xml"))) {
                    signedDocument.writeTo(fos);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred signing file.", e);
        }

    }
}
