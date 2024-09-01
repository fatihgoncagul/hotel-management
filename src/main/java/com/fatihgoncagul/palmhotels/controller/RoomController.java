package com.fatihgoncagul.palmhotels.controller;

import com.fatihgoncagul.palmhotels.exception.PhotoRetrievalException;
import com.fatihgoncagul.palmhotels.exception.ResourcesNotFoundException;
import com.fatihgoncagul.palmhotels.model.BookedRoom;
import com.fatihgoncagul.palmhotels.model.Room;
import com.fatihgoncagul.palmhotels.response.BookingResponse;
import com.fatihgoncagul.palmhotels.service.BookingService;
import com.fatihgoncagul.palmhotels.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {
    private final IRoomService roomService;
    private final BookingService bookingService;

    @PostMapping("add/new-room")
    public ResponseEntity<com.fatihgoncagul.palmhotels.response.RoomResponse> addNewRoom(@RequestParam("photo") MultipartFile photo,
                                                                                         @RequestParam("roomType") String roomType,
                                                                                         @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {
        Room savedRoomResponse = roomService.addNewRoom(photo,roomType,roomPrice);
        com.fatihgoncagul.palmhotels.response.RoomResponse roomResponse = new com.fatihgoncagul.palmhotels.response.RoomResponse(savedRoomResponse.getId(), savedRoomResponse.getRoomType(), savedRoomResponse.getRoomPrice());
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/room/types")
    public List<String> getRoomTypes(){
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<com.fatihgoncagul.palmhotels.response.RoomResponse>> getAllRooms() throws SQLException, PhotoRetrievalException {

        List<Room> rooms = roomService.getAllRooms();
        List<com.fatihgoncagul.palmhotels.response.RoomResponse> roomResponseRespons = new ArrayList<>();
        for(Room room : rooms){
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            com.fatihgoncagul.palmhotels.response.RoomResponse roomResponse = getRoomResponse(room);
            if(photoBytes!= null && photoBytes.length>0){
                String base64Photo = Base64.encodeBase64String(photoBytes);

                roomResponse.setPhoto(base64Photo);

            }
            roomResponseRespons.add(roomResponse);
        }
        return ResponseEntity.ok(roomResponseRespons);

    }

    private com.fatihgoncagul.palmhotels.response.RoomResponse getRoomResponse(Room roomResponse) throws PhotoRetrievalException {

        List<BookedRoom> bookings = getAlLBookingsByRoomId(roomResponse.getId());
        List<BookingResponse> bookingsInfo = bookings
                .stream()
                .map(booking -> new BookingResponse(booking.getBookingId(),booking.getCheckInDate(),
                        booking.getCheckOutDate(),booking.getBookingConfirmationCode()) ).toList();
        byte[] photoBytes = null;
        Blob photoBlob = roomResponse.getPhoto();
        if(photoBlob!=null){
            try{
                photoBytes = photoBlob.getBytes(1,(int) photoBlob.length());

            }catch (SQLException e){
                throw new PhotoRetrievalException("Error retrieving photo!");
            }
        }
        return new com.fatihgoncagul.palmhotels.response.RoomResponse(roomResponse.getId(), roomResponse.getRoomType(), roomResponse.getRoomPrice(), roomResponse.isBooked(),photoBytes,bookingsInfo);

    }

    private List<BookedRoom> getAlLBookingsByRoomId(Long roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);
    }

    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") Long roomId){
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{roomId}")
    public ResponseEntity<com.fatihgoncagul.palmhotels.response.RoomResponse> updateRoom(@PathVariable Long roomId, @RequestParam(required = false) String roomType, @RequestParam(required = false) BigDecimal roomPrice, @RequestParam(required = false)  MultipartFile photo) throws IOException, SQLException {

        byte[] photoBytes = photo !=null && !photo.isEmpty() ?
                photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);

        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ? new SerialBlob(photoBytes) : null;
        Room theRoomResponse = roomService.updateRoom(roomId,roomType,roomPrice,photoBytes);
        com.fatihgoncagul.palmhotels.response.RoomResponse roomResponse = getRoomResponse(theRoomResponse);
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<com.fatihgoncagul.palmhotels.response.RoomResponse>> getRoomById(@PathVariable Long roomId){

        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            com.fatihgoncagul.palmhotels.response.RoomResponse roomResponse = getRoomResponse(room);
                return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(() -> new ResourcesNotFoundException("Room not found!"));
    }


}
