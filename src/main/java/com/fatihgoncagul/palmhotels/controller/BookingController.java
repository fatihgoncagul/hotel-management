package com.fatihgoncagul.palmhotels.controller;


import com.fatihgoncagul.palmhotels.exception.InvalidBookingRequestException;
import com.fatihgoncagul.palmhotels.exception.ResourcesNotFoundException;
import com.fatihgoncagul.palmhotels.model.BookedRoom;
import com.fatihgoncagul.palmhotels.model.Room;
import com.fatihgoncagul.palmhotels.response.BookingResponse;
import com.fatihgoncagul.palmhotels.service.IBookingService;
import com.fatihgoncagul.palmhotels.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final IBookingService bookingService;
    private final IRoomService roomService;

    @GetMapping("/all-bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings(){

        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for(BookedRoom booking : bookings){
            BookingResponse bookingResponse = getBookingResponse(booking);
            bookingResponses.add(bookingResponse);
        }
        return ResponseEntity.ok(bookingResponses);

    }

    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode){

        try{
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = getBookingResponse(booking);
            return ResponseEntity.ok(bookingResponse);
        }catch (ResourcesNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }



    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBooking(@PathVariable Long roomId,@RequestBody BookedRoom bookingRequest){

        try{

            String confirmationCode = bookingService.saveBooking(roomId,bookingRequest);
            return ResponseEntity.ok("Room booked succesfully. Your confirmation code is: "+confirmationCode);

        }catch (InvalidBookingRequestException e){

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/room/{bookingId}/delete")
    public void cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
    }

    private BookingResponse getBookingResponse(BookedRoom booking) {
        Room theRoomResponse = roomService.getRoomById(booking.getRoomResponse().getId()).get();
        com.fatihgoncagul.palmhotels.response.RoomResponse roomResponse = new com.fatihgoncagul.palmhotels.response.RoomResponse(
                theRoomResponse.getId(),
                theRoomResponse.getRoomType(),
                theRoomResponse.getRoomPrice());

        return new BookingResponse(
                booking.getBookingId(),booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getGuestFullName(), booking.getGuestEmail(),booking.getNumOfAdults(), booking.getNumOfChildren(), booking.getTotalNumOfGuest(), booking.getBookingConfirmationCode(), roomResponse);

    }
}
