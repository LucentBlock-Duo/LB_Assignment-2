package com.lucentblock.assignment2.repository;

import com.lucentblock.assignment2.entity.Car;
import com.lucentblock.assignment2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByLicensePlateNoAndDeletedAtIsNull(String licensePlateNo);
    List<Car> findCarsByUserAndDeletedAtIsNull(User user);
}
