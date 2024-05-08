import java.io.FileInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;

public class DigitalCertificate {
    public String version;
    public String serialNumber;
    public String validityPeriod;
    public String signatureType;
    public String issuer;
    public String subjectFriendlyName;
    public String email;
    public PublicKey publicKey;

    public DigitalCertificate(String certificateFilePath) throws Exception {
        FileInputStream fis = new FileInputStream(certificateFilePath);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fis);

        // Extract certificate details
        this.version = String.valueOf(certificate.getVersion());
        this.serialNumber = certificate.getSerialNumber().toString();
        this.validityPeriod = certificate.getNotBefore() + " to " + certificate.getNotAfter();
        this.signatureType = certificate.getSigAlgName();

        // Extract issuer CN
        this.issuer = extractCommonName(certificate.getIssuerX500Principal().getName());

        // Extract subject CN and E
        this.subjectFriendlyName = extractCommonName(certificate.getSubjectX500Principal().getName());
        this.email = extractEmail(certificate.getSubjectX500Principal().getName());

        // Extract public key
        this.publicKey = certificate.getPublicKey();

        fis.close();
    }

    private String extractCommonName(String name) {
        X500Name x500Name = new X500Name(name);
        RDN[] rdns = x500Name.getRDNs(BCStyle.CN);
        if (rdns.length > 0) {
            return rdns[0].getFirst().getValue().toString();
        }
        return null;
    }

    private String extractEmail(String name) {
        X500Name x500Name = new X500Name(name);
        RDN[] rdns = x500Name.getRDNs(BCStyle.E);
        if (rdns.length > 0) {
            return rdns[0].getFirst().getValue().toString();
        }
        return null;
    }
}
