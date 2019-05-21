package com.flightstats.hub.webhook;

import com.flightstats.hub.cluster.ClusterStateDao;
import com.flightstats.hub.config.WebhookProperties;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.flightstats.hub.constant.ZookeeperNodes.WEBHOOK_LAST_COMPLETED;

@Singleton
@Slf4j
class WebhookStateReaper {
    private final ClusterStateDao clusterStateDao;
    private final WebhookContentPathSet webhookInProcess;
    private final WebhookErrorService webhookErrorService;
    private final WebhookLeaderLocks webhookLeaderLocks;
    private final WebhookProperties webhookProperties;

    @Inject
    WebhookStateReaper(ClusterStateDao clusterStateDao,
                       WebhookContentPathSet webhookInProcess,
                       WebhookErrorService webhookErrorService,
                       WebhookLeaderLocks webhookLeaderLocks,
                       WebhookProperties webhookProperties) {
        this.clusterStateDao = clusterStateDao;
        this.webhookInProcess = webhookInProcess;
        this.webhookErrorService = webhookErrorService;
        this.webhookLeaderLocks = webhookLeaderLocks;
        this.webhookProperties = webhookProperties;
    }

    void delete(String webhook) {
        if (!webhookProperties.isWebhookLeadershipEnabled()) {
            return;
        }
        log.info("deleting " + webhook);
        webhookInProcess.delete(webhook);
        clusterStateDao.delete(webhook, WEBHOOK_LAST_COMPLETED);
        webhookErrorService.delete(webhook);
        webhookLeaderLocks.deleteWebhookLeader(webhook);
        log.info("deleted " + webhook);
    }
}
