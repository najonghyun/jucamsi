package com.inet.juchamsi.domain.parking.application.impl;

import com.inet.juchamsi.domain.parking.application.ParkingService;
import com.inet.juchamsi.domain.parking.dao.ParkingHistoryRepository;
import com.inet.juchamsi.domain.parking.dao.ParkingLotRepository;
import com.inet.juchamsi.domain.parking.dto.request.EntranceRequest;
import com.inet.juchamsi.domain.parking.entity.ParkingHistory;
import com.inet.juchamsi.domain.parking.entity.ParkingLot;
import com.inet.juchamsi.domain.user.dao.UserRepository;
import com.inet.juchamsi.domain.user.entity.User;
import com.inet.juchamsi.global.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

import static com.inet.juchamsi.global.common.Active.ACTIVE;

@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingHistoryRepository parkingHistoryRepository;
    private final UserRepository userRepository;

    // 입차 위치 정보 저장
    @Override
    public void createEntrance(EntranceRequest request) {
        // 주차 위치로 주차장 정보 가져오기
        String groundAddress = request.getGroundAddress();
        Optional<ParkingLot>  parkingLot = parkingLotRepository.findBySeatMacAddress(groundAddress, ACTIVE);
        if (parkingLot.isEmpty()) {
            throw new NotFoundException(ParkingLot.class, groundAddress);
        }
        int seatNumber = parkingLot.get().getSeatNumber();
        // mac 주소로 사용자 내역 가져오기
        String macAddress = request.getMacAddress();
        Optional<User> user = userRepository.findUserByMacAddress(macAddress, ACTIVE);
        if (user.isEmpty()) {
            throw new NotFoundException(User.class, macAddress);
        }
        // 현재 시간 출력
        Timestamp inTime = new Timestamp(System.currentTimeMillis());
        // 주차 히스토리 저장
        parkingHistoryRepository.save(ParkingHistory.builder()
                        .user(user.get())
                        .parkingLot(parkingLot.get())
                        .active(ACTIVE)
                        .inTime(inTime)
                        .outTime(null)
                        .build());
        // 주차 위치 정보로부터 뒤차가 있을 경우 해당 차의 출차시간에 맞춰 알람을 보낸다.
        int backNumber = parkingLot.get().getBackNumber();
        if (backNumber != 0) {
            // 뒤차 정보 -> 뒤차의 자리 번호로 해당 주차장 식별키를 알아내 주차장 히스토리에 현재 주차되어 있는 차를 찾음
            Optional<Long> parkingOp = parkingHistoryRepository.existByParkingHistoryAndActive(backNumber, ACTIVE);
            if (parkingOp.isPresent()) {
                // TODO: 현재 주차가 되어있음 -> 해당 차의 출차시간에 맞춰 알람 보냄
                // TODO: 뒤차주에게 현 차주의 출차시간을 알람으로 보내줌
            }
        }
        // 주차가 됐다는 알람을 사용자(차주)에게 전송한다.
    }
}
