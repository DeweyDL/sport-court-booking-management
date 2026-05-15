package com.sportcourt.modules.customer_booking.view;

import java.util.List;

final class CustomerBookingSampleData {
    private CustomerBookingSampleData() {
    }

    static List<CourtOption> courts() {
        return List.of(
                new CourtOption("Sân Bóng Pro Arena", "Quận 7, TP. Hồ Chí Minh", "Bóng đá 5 người", "150.000VNĐ / giờ"),
                new CourtOption("Badminton Central", "Quận 1, TP. Hồ Chí Minh", "Cầu lông", "180.000VNĐ / giờ"),
                new CourtOption("The Pickleball Hub", "Thủ Đức, TP. Hồ Chí Minh", "Pickleball", "200.000VNĐ / giờ"),
                new CourtOption("Sân Bóng Pro Arena", "Quận 7, TP. Hồ Chí Minh", "Bóng đá 5 người", "150.000VNĐ / giờ"),
                new CourtOption("Badminton Central", "Quận 1, TP. Hồ Chí Minh", "Cầu lông", "180.000VNĐ / giờ"),
                new CourtOption("The Pickleball Hub", "Thủ Đức, TP. Hồ Chí Minh", "Pickleball", "200.000VNĐ / giờ")
        );
    }

    static CourtOption defaultCourt() {
        return courts().get(0);
    }

    static final class CourtOption {
        private final String name;
        private final String address;
        private final String sportType;
        private final String price;

        CourtOption(String name, String address, String sportType, String price) {
            this.name = name;
            this.address = address;
            this.sportType = sportType;
            this.price = price;
        }

        String name() {
            return name;
        }

        String address() {
            return address;
        }

        String sportType() {
            return sportType;
        }

        String price() {
            return price;
        }
    }
}
