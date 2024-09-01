package com.fatihgoncagul.palmhotels.service;

import com.fatihgoncagul.palmhotels.model.BookedRoom;

import java.util.List;

public interface IBookingService {
    String saveBooking(Long roomId, BookedRoom bookingRequest);

    BookedRoom findByBookingConfirmationCode(String confirmationCode);

    List<BookedRoom> getAllBookings();

    void cancelBooking(Long bookingId);
}
