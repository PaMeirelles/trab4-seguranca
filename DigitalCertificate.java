import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class DigitalCertificate {
    public String version;
    public String serialNumber;
    public String validityPeriod;
    public String signatureType;
    public String issuer;
    public String subjectFriendlyName;
    public String email;

    public DigitalCertificate (String certificateFilePath) throws Exception {
        FileInputStream fis = new FileInputStream(certificateFilePath);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fis);

        // Extract certificate details
        this.version = String.valueOf(certificate.getVersion());
        this.serialNumber = certificate.getSerialNumber().toString();
        this.validityPeriod = certificate.getNotBefore() + " to " + certificate.getNotAfter();
        this.signatureType = certificate.getSigAlgName();
        this.issuer = certificate.getIssuerDN().getName();
        this.subjectFriendlyName = certificate.getSubjectDN().getName();
        //this.email = getEmailFromSubject(certificate.getSubjectDN().getName());

        fis.close();
    }

    private String getEmailFromSubject(String subject) {
        // You need to implement a method to extract the email from the subject DN string.
        // This can be done by parsing the subject string using regular expressions or other string manipulation techniques.
        // For simplicity, let's assume the email is enclosed within "<" and ">".
        int startIndex = subject.indexOf('<');
        int endIndex = subject.indexOf('>');
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return subject.substring(startIndex + 1, endIndex);
        }
        return null; // If email not found
    }
}
