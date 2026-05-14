package com.sportcourt.modules.customer_history.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Demo runner cho module Lịch sử đặt sân.
 *
 * Trước khi chạy: đảm bảo DB đang chạy và chuỗi kết nối trong DBConnection đúng.
 * Mã khách hàng giả lập mặc định là "KH-1" — đổi hằng MOCK_CUSTOMER_ID nếu cần.
 */
public class BookingHistoryDemo {

    private static final String MOCK_CUSTOMER_ID = "KH-1";
    private static final String MOCK_ACCOUNT_ID  = "ACC-1";

    public static void main(String[] args) {
        setupLookAndFeel();
        setupMockSession();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("RENTSTA – Lịch Sử Đặt Sân (Demo)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 900);
            frame.setLocationRelativeTo(null);
            frame.add(new BookingHistoryPanel.BookingHistoryPanel());
            frame.setVisible(true);
        });
    }

    // -------------------------------------------------------------------------

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Không tải được FlatLaf, dùng Look&Feel mặc định.");
        }
    }

    /**
     * Tạo UserSession không qua constructor (bypass mọi tham số bắt buộc),
     * sau đó ép field và nhét vào SessionManager bằng reflection.
     */
    private static void setupMockSession() {
        try {
            // Tạo instance mà không gọi bất kỳ constructor nào của UserSession
            sun.reflect.ReflectionFactory rf =
                    sun.reflect.ReflectionFactory.getReflectionFactory();
            Constructor<?> objCtor = Object.class.getDeclaredConstructor();
            Constructor<?> ctor    = rf.newConstructorForSerialization(UserSession.class, objCtor);
            ctor.setAccessible(true);
            UserSession mock = (UserSession) ctor.newInstance();

            setField(mock,                 "customerId", MOCK_CUSTOMER_ID);
            setField(mock,                 "accountId",  MOCK_ACCOUNT_ID);
            setField(SessionManager.class, "session",    mock);
        } catch (Exception e) {
            System.err.println("Không giả lập được session: " + e.getMessage());
            System.err.println("Hãy gọi SessionManager.login(...) thủ công trước khi chạy.");
        }
    }

    /** Gán giá trị cho instance field (kể cả private). */
    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (NoSuchFieldException ignored) {
            // Tên field không khớp — bỏ qua, không crash
        } catch (Exception e) {
            System.err.println("setField(" + fieldName + "): " + e.getMessage());
        }
    }

    /** Gán giá trị cho static field của một Class. */
    private static void setField(Class<?> clazz, String fieldName, Object value) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(null, value);
        } catch (NoSuchFieldException ignored) {
            // Tên field không khớp — bỏ qua, không crash
        } catch (Exception e) {
            System.err.println("setField(static " + fieldName + "): " + e.getMessage());
        }
    }
}