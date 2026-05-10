package com.devsync.ai.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(PmTransactionalSeed.class)
public class PmSeedStarter {

    private final PmTransactionalSeed pmTransactionalSeed;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            pmTransactionalSeed.seedDemoData();
        } catch (Exception ex) {
            log.error("PM demo seed aborted: {}", ex.getMessage(), ex);
        }
    }
}
