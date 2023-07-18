package com.lucentblock.assignment2.service;


import com.lucentblock.assignment2.entity.*;
import com.lucentblock.assignment2.exception.PreviousRepairNotFoundException;
import com.lucentblock.assignment2.exception.ReserveErrorCode;
import com.lucentblock.assignment2.exception.ReserveNotFoundException;
import com.lucentblock.assignment2.model.*;
import com.lucentblock.assignment2.repository.PreviousRepairRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import static com.lucentblock.assignment2.model.PreviousRepairSearchRequestDTO.*;

@Service
@RequiredArgsConstructor
public class PreviousRepairService {

    private final PreviousRepairRepository previousRepairRepository;
    private final EntityManager em;

    public PreviousRepair createPreviousRepair(Long reserveId) {
        Reserve reserve = em.find(Reserve.class, reserveId);
        PreviousRepair previousRepair = reserve.toPreviousRepairEntity();

        return previousRepairRepository.save(previousRepair);
    }

    public List<ResponsePreviousRepairDTO> commonSearch(Common dto){
        User user=em.find(User.class,dto.getUser_id());
        RepairMan repairMan=em.find(RepairMan.class,dto.getRepair_man_id());

        return previousRepairRepository.findPreviousRepair(user,repairMan).stream()
                .map(PreviousRepair::toDto).toList();
    } // 사용자, 정비공 이름으로 search, 사용자 별로 이 결과를 holding 할 수 없을까?

    public List<ResponsePreviousRepairDTO> detailSearch(Detail dto) {
        ForeignKeySetForPreviousRepair set=getForeignKeySet(dto);

        return previousRepairRepository.findPreviousRepairDetail(set,dto.getStartTime(),dto.getEndTime()).stream()
                .map(PreviousRepair::toDto).toList();
    } // 사용자(정비공)/자동차/특정 정비내역/일정(기간) 등 상세검색기능

    private ForeignKeySetForPreviousRepair getForeignKeySet(RequestPreviousRepairDTO dto) {
        User user = em.find(User.class, dto.getUser_id());
        Car car = em.find(Car.class, dto.getCar_id());
        RepairMan repairMan = em.find(RepairMan.class, dto.getRepair_man_id());
        RepairShop repairShop = em.find(RepairShop.class, dto.getRepair_shop_id());
        MaintenanceItem maintenanceItem = em.find(MaintenanceItem.class, dto.getMaintenance_item_id());

        return ForeignKeySetForPreviousRepair.builder()
                .user(user)
                .car(car)
                .repairMan(repairMan)
                .repairShop(repairShop)
                .maintenanceItem(maintenanceItem).build();
    }


    @Transactional
    public PreviousRepair deleteRepair(Long repairId){
        PreviousRepair repair = previousRepairRepository.findById(repairId)
                .orElseThrow(()->new PreviousRepairNotFoundException("기록을 찾을 수 없습니다."));

        repair.delete();
        previousRepairRepository.save(repair);

        return repair;
    }
}