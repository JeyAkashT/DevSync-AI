package com.devsync.ai.pm;

import com.devsync.ai.api.dto.pm.OrganizationSummaryResponse;
import com.devsync.ai.repository.MembershipRepository;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationDirectoryService {

    private final MembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public List<OrganizationSummaryResponse> forUser(UUID userId) {
        return membershipRepository.findByUserIdWithOrganization(userId).stream()
                .map(m ->
                        new OrganizationSummaryResponse(
                                m.getOrganization().getId(),
                                m.getOrganization().getName(),
                                m.getOrganization().getSlug()))
                .toList();
    }

    public List<OrganizationSummaryResponse> forCurrentUser(SecurityUser user) {
        return forUser(user.delegate().getId());
    }
}
