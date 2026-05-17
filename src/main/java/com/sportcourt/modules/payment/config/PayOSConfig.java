package com.sportcourt.modules.payment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.payos.PayOS;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public final class PayOSConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PayOSConfig.class);

    private static final PayOS PAY_OS;

    static {
        Properties p = new Properties();
        try (InputStream in = PayOSConfig.class.getClassLoader()
                .getResourceAsStream("payos/payos.properties")) {

            if (in == null) {
                throw new IllegalStateException("Khong tim thay payos/payos.properties trong resources");
            }
            p.load(in);

            String clientId = p.getProperty("payos.client-id");
            String apiKey = p.getProperty("payos.api-key");
            String checksumKey = p.getProperty("payos.checksum-key");


            PAY_OS = new PayOS(clientId, apiKey, checksumKey);


        } catch (IOException e) {
            throw new ExceptionInInitializerError("Loi doc payos.properties: " + e.getMessage());
        }
    }

    private PayOSConfig() {
    }

    public static PayOS get() {
        return PAY_OS;
    }
}
