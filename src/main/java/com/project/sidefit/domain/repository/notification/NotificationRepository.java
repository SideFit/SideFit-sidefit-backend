package com.project.sidefit.domain.repository.notification;

import com.project.sidefit.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

    @Query("select n from Notification n join fetch n.sender s join fetch n.receiver r where s.id = :senderId and r.id = :receiverId order by n.createdDate desc")
    List<Notification> findWithSenderIdAndReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
