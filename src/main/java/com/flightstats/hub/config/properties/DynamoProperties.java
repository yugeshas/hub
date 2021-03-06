package com.flightstats.hub.config.properties;

import javax.inject.Inject;

public class DynamoProperties {

    private final PropertiesLoader propertiesLoader;
    private final AppProperties appProperties;

    @Inject
    public DynamoProperties(PropertiesLoader propertiesLoader, AppProperties appProperties) {
        this.propertiesLoader = propertiesLoader;
        this.appProperties = appProperties;
    }

    private String getLegacyChannelTableName() {
        return appProperties.getAppName() + "-" + appProperties.getEnv() + "-" + "channelMetaData";
    }

    private String getLegacyWebhookTableName() {
        return appProperties.getAppName() + "-" + appProperties.getEnv() + "-" + "GroupConfig";
    }

    public String getWebhookConfigTableName(){
        return propertiesLoader.getProperty("dynamo.table_name.webhook_configs", getLegacyWebhookTableName());
    }

    public String getChannelConfigTableName(){
        return propertiesLoader.getProperty("dynamo.table_name.channel_configs", getLegacyChannelTableName());
    }

    public String getEndpoint() {
        return propertiesLoader.getProperty("dynamo.endpoint", "dynamodb.us-east-1.amazonaws.com");
    }

    public int getMaxConnections() {
        return propertiesLoader.getProperty("dynamo.maxConnections", 50);
    }

    public int getConnectionTimeout() {
        return propertiesLoader.getProperty("dynamo.connectionTimeout", 10 * 1000);
    }

    public int getSocketTimeout() {
        return propertiesLoader.getProperty("dynamo.socketTimeout", 30 * 1000);
    }

}