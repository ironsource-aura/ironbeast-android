package io.ironsourceatom.sdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class Utils {

    /**
     * helper function that extract a buffer from the given inputStream
     * @param in
     * @return
     * @throws IOException
     */
    public static byte[] getBytes(final InputStream in) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int bytesRead;
        byte[] data = new byte[8192];
        while ((bytesRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    /**
     * IronSourceAtom auth function
     * Exception could be: NoSuchAlgorithmException and InvalidKeyException
     * @param data
     * @param key
     * @return auth string
     */
    public static String auth(String data, String key) {
        try {
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            sha256_HMAC.init(secret_key);
            StringBuilder sb = new StringBuilder();
            for (byte b : sha256_HMAC.doFinal(data.getBytes("UTF-8"))) {
                sb.append(String.format("%1$02x", b));
            }
            return sb.toString();
        } catch(NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            return "";
        }
    }
}
