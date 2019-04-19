package com.flightstats.hub.system.functional;

import com.flightstats.hub.model.Webhook;
import com.flightstats.hub.system.config.DependencyInjector;
import com.flightstats.hub.system.resilient.HubLifecycle;
import com.flightstats.hub.system.service.CallbackService;
import com.flightstats.hub.system.service.ChannelService;
import com.flightstats.hub.system.service.WebhookService;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.flightstats.hub.model.ChannelContentStorageType.SINGLE;
import static com.flightstats.hub.util.StringUtils.randomAlphaNumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WebhookLifecycleTest extends DependencyInjector {
    @Inject
    private CallbackService callbackResource;
    @Inject
    private ChannelService channelResource;
    @Inject
    private WebhookService webhookResource;
    private String channelName;
    private String webhookName;
    @javax.inject.Inject
    private HubLifecycle hubLifecycle;

    @BeforeAll
    public void hubSetup() {
        hubLifecycle.setup();
    }

    @BeforeEach
    public void before() {
        this.channelName = randomAlphaNumeric(10);
        this.webhookName = randomAlphaNumeric(10);
    }

    private Webhook buildWebhook() {
        return Webhook.builder()
                .name(webhookName)
                .channelUrl(channelResource.getHubBaseUrl() + "channel/" + channelName)
                .callbackUrl(callbackResource.getCallbackBaseUrl() + "callback/")
                .batch(SINGLE.toString())
                .build();
    }

    @Test
    @SneakyThrows
    public void testWebhookWithNoStartItem() {
        final String data = "{\"fn\": \"first\", \"ln\":\"last\"}";

        channelResource.create(channelName);

        final Webhook webhook = buildWebhook().withParallelCalls(2);
        webhookResource.insertAndVerify(webhook);

        final List<String> channelItems = channelResource.addItems(channelName, data, 10);
        final List<String> channelItemsPosted = callbackResource.awaitItemCountSentToWebhook(webhookName, Optional.empty(), channelItems.size());

        Collections.sort(channelItems);
        Collections.sort(channelItemsPosted);
        assertEquals(channelItems, channelItemsPosted);
    }

    @Test
    @SneakyThrows
    public void testWebhookWithStartItem() {
        final String data = "{\"key1\": \"value1\", \"key2\":\"value2\"}";

        channelResource.create(channelName);
        final List<String> channelItems = channelResource.addItems(channelName, data, 10);

        final Webhook webhook = buildWebhook().
                withStartItem(channelItems.get(4)).
                withParallelCalls(2);
        webhookResource.insertAndVerify(webhook);
        final List<String> channelItemsExpected = channelItems.subList(5, channelItems.size());
        final List<String> channelItemsPosted = callbackResource.awaitItemCountSentToWebhook(webhookName, Optional.empty(), channelItemsExpected.size());

        Collections.sort(channelItemsExpected);
        Collections.sort(channelItemsPosted);
        assertEquals(channelItemsExpected, channelItemsPosted);
    }

    @Test
    @SneakyThrows
    public void testWebhookWithStartItem_expectItemsInOrder() {
        final String data = "{\"city\": \"portland\", \"state\":\"or\"}";

        channelResource.create(channelName);
        final List<String> channelItems = channelResource.addItems(channelName, data, 10);

        final Webhook webhook = buildWebhook().
                withStartItem(channelItems.get(4)).
                withParallelCalls(1);
        webhookResource.insertAndVerify(webhook);
        final List<String> channelItemsExpected = channelItems.subList(5, channelItems.size());
        final List<String> channelItemsPosted = callbackResource.awaitItemCountSentToWebhook(webhookName, Optional.empty(), channelItemsExpected.size());

        assertEquals(channelItemsExpected, channelItemsPosted);
    }

    @AfterEach
    @SneakyThrows
    public void after() {
        this.channelResource.delete(channelName);
        this.webhookResource.delete(webhookName);
    }

    @AfterAll
    public void hubCleanup() {
        hubLifecycle.cleanup();
    }
}
