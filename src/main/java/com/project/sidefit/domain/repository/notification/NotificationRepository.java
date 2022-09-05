package com.project.sidefit.domain.repository.notification;

import com.project.sidefit.domain.entity.Notification;
import com.project.sidefit.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    @Query("select n from Notification n join n.receiver r where r.id = :id and n.isChecked = false")
    List<Notification> findWithReceiverIdAndUnChecked(@Param("id") Long receiverId);
}
