package kz.itzhiti.deliveryservice.repository;

import kz.itzhiti.deliveryservice.model.Courier;
import kz.itzhiti.deliveryservice.model.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    List<Courier> findByStatus(CourierStatus status);
}

