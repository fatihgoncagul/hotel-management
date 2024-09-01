package com.fatihgoncagul.palmhotels.service;

import com.fatihgoncagul.palmhotels.exception.InvalidBookingRequestException;
import com.fatihgoncagul.palmhotels.model.BookedRoom;
import com.fatihgoncagul.palmhotels.model.Room;
import com.fatihgoncagul.palmhotels.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService{

    private final IRoomService roomService;

    private final BookingRepository bookingRepository;


    @Override
    public List<BookedRoom> getAllBookings() {
        return  bookingRepository.findAll();
    }
    @Override
    public void cancelBooking(Long bookingId) {

        bookingRepository.deleteById(bookingId);

    }


    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {

        return bookingRepository.findByRoomId(roomId);

    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {

        if(bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())){
            throw new InvalidBookingRequestException("Check-in date must come before check-out date");
        }
        Room room = roomService.getRoomById(roomId).get();
        List<BookedRoom> existingBookings = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookingRequest,existingBookings);
        if (roomIsAvailable){
            room.addBooking(bookingRequest);
            bookingRepository.save(bookingRequest);
        }else {
            throw new InvalidBookingRequestException("This room is not available for the selected days!");
        }


        return bookingRequest.getBookingConfirmationCode();
    }

    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {

        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate())

                || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
                ||(bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate()))
                && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate())
                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate()))
                &&bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate())
                ||(bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate()))
                && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate())
                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate()))
                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate())
                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate()))
                && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()));


    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode);
    }




}
