import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class MeshApi {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String CHARSET = "UTF-8";

    public static void main(String args[]) throws Exception {
        final String host = System.getenv().get("HOST");
        final String key = System.getenv().get("API_KEY");
        final String secret = System.getenv().get("API_SECRET");

        if (host == null || key == null || secret == null) {
            throw new Exception("HOST, API_KEY or API_SECRET is missing");
        }

        callApi(host, key, secret);
    }

    private static void callApi(String host, String key, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException, MalformedURLException, IOException {
        // gather params
        final String timestamp = Instant.now().truncatedTo(ChronoUnit.MINUTES).toString();
        final String nonce = UUID.randomUUID().toString();

        // generate signature
        final String payload = String.format("date:%s\nx-mesh-nonce:%s", timestamp, nonce);
        final Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(CHARSET), ALGORITHM));
        final byte[] signature = mac.doFinal(payload.getBytes(CHARSET));
        final String signatureEncoded = Base64.getEncoder().encodeToString(signature);

        // make API call
        final String auth = String.format("HMAC-SHA256 Credential=%s;SignedHeaders=Date,x-mesh-nonce;Signature=%s", key,
                signatureEncoded);
        final HttpURLConnection connection = (HttpURLConnection) new URL(String.format("%s/status", host))
                .openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", auth);
        connection.setRequestProperty("Date", timestamp);
        connection.setRequestProperty("x-mesh-nonce", nonce);
        connection.connect();

        // read response
        final int status = connection.getResponseCode();
        final String body = readResponse(status < 300 ? connection.getInputStream() : connection.getErrorStream());
        final String requestId = connection.getHeaderField("x-amzn-RequestId");
        connection.disconnect();
        System.out.printf("Request ID [%s] returned [%d]\n%s\n", requestId, status, body);
    }

    private static String readResponse(InputStream responseStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        return content.toString();
    }
}