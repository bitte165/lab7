package ru.bitte.lab7.requests;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AuthorizeRequest implements Request, Serializable {
    private final String username;
    private final String salt;
    private final String password;
    private final boolean signingUp;

    public AuthorizeRequest(String uname, String passwd, boolean signup) {
        username = uname;
        signingUp = signup;
        salt = getRandomString(16);
        String pepper = "&q%^SAsdF(U]";
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        password = new String(md.digest((pepper + passwd + salt).getBytes(StandardCharsets.ISO_8859_1)));
    }

    public AuthorizeRequest(String uname, String passwd, String slt) {
        username = uname;
        signingUp = false;
        salt = slt;
        String pepper = "&q%^SAsdF(U]";
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        password = new String(md.digest((pepper + passwd + salt).getBytes(StandardCharsets.ISO_8859_1)));
    }

    public String getUsername() {
        return username;
    }

    public String getSalt() {
        return salt;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSigningUp() {
        return signingUp;
    }

    public User toUser() {
        return new User(getUsername(), getPassword(), getSalt());
    }

    private String getRandomString(int len) {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[len];
        random.nextBytes(randomBytes);
        return new String(randomBytes, StandardCharsets.ISO_8859_1);
    }
}
