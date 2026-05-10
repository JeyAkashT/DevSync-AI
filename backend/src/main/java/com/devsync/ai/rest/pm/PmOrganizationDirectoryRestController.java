package com.devsync.ai.rest.pm;

import com.devsync.ai.api.dto.pm.OrganizationSummaryResponse;
import com.devsync.ai.pm.OrganizationDirectoryService;
import com.devsync.ai.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class PmOrganizationDirectoryRestController {

    private final OrganizationDirectoryService organizationDirectoryService;

    @GetMapping("/organizations")
    public List<OrganizationSummaryResponse> myOrganizations(@AuthenticationPrincipal SecurityUser user) {
        return organizationDirectoryService.forCurrentUser(user);
    }
}
