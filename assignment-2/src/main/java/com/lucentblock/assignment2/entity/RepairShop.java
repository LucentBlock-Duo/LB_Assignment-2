package com.lucentblock.assignment2.entity;


import com.lucentblock.assignment2.model.ResponseRepairShopDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="repair_shop")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RepairShop implements SoftDeletable{

    @Id
    private Long id;

    @Column
    private String name; // 블루핸즈봉명점

    @ManyToOne
    @JoinColumn(referencedColumnName = "id",name="location_id")
    private CountryLocation location; // location 수정

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public ResponseRepairShopDTO toDto(){
        return ResponseRepairShopDTO.builder()
                .id(id)
                .name(name)
                .location(location.toDto()).build();
    }
}
