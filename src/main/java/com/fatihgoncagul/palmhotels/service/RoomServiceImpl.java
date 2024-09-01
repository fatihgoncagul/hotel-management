package com.fatihgoncagul.palmhotels.service;

import com.fatihgoncagul.palmhotels.exception.InternalServerException;
import com.fatihgoncagul.palmhotels.exception.ResourcesNotFoundException;
import com.fatihgoncagul.palmhotels.model.Room;
import com.fatihgoncagul.palmhotels.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements IRoomService {

    private final RoomRepository roomRepository;
    @Override
    public Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws IOException, SQLException {
        Room roomResponse = new Room();
        roomResponse.setRoomType(roomType);
        roomResponse.setRoomPrice(roomPrice);
        if(!file.isEmpty()){
            byte[] photoBytes = file.getBytes();
            Blob photoBlob = new SerialBlob(photoBytes);
            roomResponse.setPhoto(photoBlob);
        }
        return roomRepository.save(roomResponse);
    }

    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomType();
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException {

        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isEmpty()){
            throw new ResourcesNotFoundException("Sorry room not found");
        }

        Blob photoBlob = theRoom.get().getPhoto();
        if (photoBlob!= null){
            return photoBlob.getBytes(1,(int) photoBlob.length());
        }


        return new byte[0];
    }

    @Override
    public void deleteRoom(Long roomId) {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isPresent()){
            roomRepository.deleteById(roomId);
        }
    }

    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes) {
        Room roomResponse = roomRepository.findById(roomId).orElseThrow(() -> new ResourcesNotFoundException("Room not found!"));

        if(roomType != null) roomResponse.setRoomType(roomType);
        if (roomResponse != null) roomResponse.setRoomPrice(roomPrice);
        if (photoBytes != null && photoBytes.length>0){
            try{
                roomResponse.setPhoto(new SerialBlob(photoBytes));
            }catch (SQLException e){
                throw new InternalServerException("Error updating room");

            }
        }
        return roomRepository.save(roomResponse);

    }

    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return Optional.of(roomRepository.findById(roomId).get());
    }

}
