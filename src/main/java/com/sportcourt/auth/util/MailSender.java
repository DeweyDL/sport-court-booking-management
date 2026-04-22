package com.sportcourt.auth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MailSender {
    private static final String PRIMARY_CONFIG = "mail.properties";
    private static final String FALLBACK_CONFIG = "mail.properties.example";
    private static final String CRLF = "\r\n";

    private final Properties config = new Properties();

    public MailSender() {
        loadConfig();
    }

    public void sendOtp(String toEmail, String otpCode, String purpose) throws IOException {
        String host = require("mail.host");
        int port = Integer.parseInt(require("mail.port"));
        String username = require("mail.username");
        String password = require("mail.appPassword").replace(" ", "");
        String fromName = config.getProperty("mail.fromName", "RentSta").trim();

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(15_000);

            SmtpConnection smtp = SmtpConnection.plain(socket);
            smtp.expectOk("Connect");
            smtp.sendCommand("EHLO localhost", 250);
            smtp.startTls(host);
            smtp.sendCommand("EHLO localhost", 250);
            smtp.sendCommand("AUTH LOGIN", 334);
            smtp.sendCommand(base64(username), 334);
            smtp.sendCommand(base64(password), 235);
            smtp.sendCommand("MAIL FROM:<" + username + ">", 250);
            smtp.sendCommand("RCPT TO:<" + toEmail.trim() + ">", 250);
            smtp.sendCommand("DATA", 354);
            smtp.sendRaw(buildMessage(username, toEmail.trim(), fromName, otpCode, purpose));
            smtp.sendCommand(".", 250);
            smtp.sendCommand("QUIT", 221);
        }
    }

    private String buildMessage(String fromEmail, String toEmail, String fromName, String otpCode, String purpose) {
        String subject = "[" + fromName + "] Ma OTP " + purpose;
        String body = "Ma OTP cua ban la: " + otpCode + "\nMa co hieu luc trong 5 phut.";

        return ""
                + "From: " + encodeHeader(fromName) + " <" + fromEmail + ">" + CRLF
                + "To: <" + toEmail + ">" + CRLF
                + "Subject: " + encodeHeader(subject) + CRLF
                + "MIME-Version: 1.0" + CRLF
                + "Content-Type: text/plain; charset=UTF-8" + CRLF
                + "Content-Transfer-Encoding: 8bit" + CRLF
                + CRLF
                + dotStuff(body).replace("\n", CRLF)
                + CRLF;
    }

    private String encodeHeader(String value) {
        return "=?UTF-8?B?" + base64(value) + "?=";
    }

    private String base64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String dotStuff(String value) {
        String normalized = value.replace("\r\n", "\n");
        String[] lines = normalized.split("\n", -1);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                builder.append('\n');
            }

            String line = lines[i];
            if (line.startsWith(".")) {
                builder.append('.');
            }
            builder.append(line);
        }

        return builder.toString();
    }

    private void loadConfig() {
        ClassLoader classLoader = MailSender.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(PRIMARY_CONFIG) != null
                ? classLoader.getResourceAsStream(PRIMARY_CONFIG)
                : classLoader.getResourceAsStream(FALLBACK_CONFIG)) {
            if (input == null) {
                throw new IllegalStateException("Cannot find mail.properties or mail.properties.example");
            }
            config.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load mail configuration", e);
        }
    }

    private String require(String key) {
        String value = config.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing mail property: " + key);
        }
        return value.trim();
    }

    private static final class SmtpConnection {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;

        private SmtpConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        }

        private static SmtpConnection plain(Socket socket) throws IOException {
            return new SmtpConnection(socket);
        }

        private void sendCommand(String command, int expectedCode) throws IOException {
            writer.print(command + CRLF);
            writer.flush();
            expect(expectedCode, command);
        }

        private void sendRaw(String text) {
            writer.print(text);
            writer.flush();
        }

        private void expectOk(String phase) throws IOException {
            expect(220, phase);
        }

        private void expect(int expectedCode, String phase) throws IOException {
            SmtpResponse response = readResponse();
            if (response.code != expectedCode) {
                throw new IOException("SMTP " + phase + " failed: " + response.message);
            }
        }

        private void startTls(String host) throws IOException {
            sendCommand("STARTTLS", 220);

            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) factory.createSocket(
                    socket,
                    host,
                    socket.getPort(),
                    true
            );
            sslSocket.setUseClientMode(true);
            sslSocket.startHandshake();
            socket = sslSocket;
            reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.UTF_8), true);
        }

        private SmtpResponse readResponse() throws IOException {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                throw new IOException("SMTP server closed the connection");
            }

            StringBuilder message = new StringBuilder(firstLine);
            int code = parseCode(firstLine);

            while (firstLine.length() > 3 && firstLine.charAt(3) == '-') {
                String nextLine = reader.readLine();
                if (nextLine == null) {
                    break;
                }
                message.append(" | ").append(nextLine);
                firstLine = nextLine;
            }

            return new SmtpResponse(code, message.toString());
        }

        private int parseCode(String line) throws IOException {
            if (line.length() < 3) {
                throw new IOException("Invalid SMTP response: " + line);
            }
            return Integer.parseInt(line.substring(0, 3));
        }
    }

    private static final class SmtpResponse {
        private final int code;
        private final String message;

        private SmtpResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
