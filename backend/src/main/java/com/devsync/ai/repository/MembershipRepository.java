package com.devsync.ai.repository;

import com.devsync.ai.model.Membership;
import com.devsync.ai.model.Organization;
import com.devsync.ai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    List<Membership> findByUser(User user);

    List<Membership> findByOrganization(Organization organization);

    @Query("SELECT m FROM Membership m JOIN FETCH m.organization WHERE m.user.id = :userId")
    List<Membership> findByUserIdWithOrganization(@Param("userId") UUID userId);

    Optional<Membership> findByOrganization_IdAndUser_Id(UUID organizationId, UUID userId);
}
