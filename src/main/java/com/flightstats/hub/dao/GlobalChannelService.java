package com.flightstats.hub.dao;

import com.diffplug.common.base.Errors;
import com.flightstats.hub.app.HubProvider;
import com.flightstats.hub.model.*;
import com.google.common.base.Optional;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The GlobalChannelService is a pass through for non-global channels
 * For Global channels, it can take different paths ...
 */
@Singleton
public class GlobalChannelService implements ChannelService {

    private ChannelService channelService = HubProvider.getInstance(LocalChannelService.class);

    public static <X> X handleGlobal(ChannelConfig channel, Supplier<X> local, Supplier<X> satellite, Supplier<X> master) {
        if (channel.isGlobal()) {
            if (channel.isGlobalMaster()) {
                return master.get();
            } else {
                return satellite.get();
            }
        } else {
            return local.get();
        }
    }

    @Override
    public boolean channelExists(String channelName) {
        return false;
    }

    @Override
    public ChannelConfig createChannel(ChannelConfig channel) {
        if (channel.isGlobal()) {
            //todo - gfm - 5/19/16 - call global master with PUT /internal/global/master/{channel}
            return null;
        } else {
            return channelService.createChannel(channel);
        }
    }

    @Override
    public ChannelConfig updateChannel(ChannelConfig channel, ChannelConfig oldConfig) {
        if (channel.isGlobal()) {
            //todo - gfm - 5/19/16 - call global master with PUT /internal/global/master/{channel}
            return null;
        } else {
            return channelService.updateChannel(channel, oldConfig);
        }
    }

    @Override
    public ContentKey insert(String channelName, Content content) throws Exception {
        Supplier<ContentKey> local = Errors.rethrow().wrap(() -> {
            return channelService.insert(channelName, content);
        });
        Supplier<ContentKey> satellite = () -> {
            //todo - gfm - 5/19/16 - call global master with POST /channel/{channel}/
            return null;
        };
        return handleGlobal(channelName, local, satellite);
    }

    @Override
    public Collection<ContentKey> insert(BulkContent bulkContent) throws Exception {
        Supplier<Collection<ContentKey>> local = Errors.rethrow().wrap(() -> {
            return channelService.insert(bulkContent);
        });
        Supplier<Collection<ContentKey>> satellite = () -> {
            //todo - gfm - 5/19/16 - call global master with POST /channel/{channel}/bulk
            return null;
        };
        return handleGlobal(bulkContent.getChannel(), local, satellite);
    }

    private <X> X handleGlobal(String channelName, Supplier<X> local, Supplier<X> satellite) {
        return handleGlobal(channelName, local, satellite, local);
    }

    private <X> X handleGlobal(String channelName, Supplier<X> local, Supplier<X> satellite, Supplier<X> master) {
        return handleGlobal(getCachedChannelConfig(channelName), local, satellite, master);
    }

    @Override
    public boolean isReplicating(String channelName) {
        return channelService.isReplicating(channelName);
    }

    @Override
    public Optional<ContentKey> getLatest(String channelName, boolean stable, boolean trace) {
        Supplier<Optional<ContentKey>> local = () -> channelService.getLatest(channelName, stable, trace);
        Supplier<Optional<ContentKey>> satellite = () -> {
            //todo - gfm - 5/20/16 - call Spoke directly
            //todo - gfm - 5/19/16 - if we don't find a latest in spoke, try master

            //todo - gfm - 5/19/16 - call global master with GET /channel/{channel}/latest
            return null;
        };
        return handleGlobal(channelName, local, satellite);
    }

    @Override
    public void deleteBefore(String name, ContentKey limitKey) {
        Supplier<Void> local = () -> {
            channelService.deleteBefore(name, limitKey);
            return null;
        };
        Supplier<Void> satellite = () -> {
            //do nothing
            return null;
        };
        handleGlobal(name, local, satellite);
    }

    @Override
    public Optional<Content> getValue(Request request) {
        Supplier<Optional<Content>> local = () -> channelService.getValue(request);
        Supplier<Optional<Content>> satellite = () -> {
            //todo - gfm - 5/20/16 - call Spoke directly
            //todo - gfm - 5/19/16 - if we don't find a Value in spoke, try master
            //todo - gfm - 5/19/16 - call global master with GET /channel/{channel}/{key}
            return null;
        };
        return handleGlobal(request.getChannel(), local, satellite);
    }

    @Override
    public Iterable<String> getTags() {
        return channelService.getTags();
    }

    @Override
    public SortedSet<ContentKey> queryByTime(TimeQuery query) {
        Supplier<SortedSet<ContentKey>> local = () -> channelService.queryByTime(query);
        Supplier<SortedSet<ContentKey>> satellite = () -> {
            //todo - gfm - 5/19/16 - if the time is entirely within Spoke, answer the question locally
            //todo - gfm - 5/19/16 - otherwise merge the values from the local Spoke and the Master
            //todo - gfm - 5/19/16 - call global master with ... GET /channel/{channel}/{time}
            return null;
        };
        return handleGlobal(query.getChannelName(), local, satellite);
    }

    @Override
    public SortedSet<ContentKey> getKeys(DirectionQuery query) {
        Supplier<SortedSet<ContentKey>> local = () -> channelService.getKeys(query);
        ;
        Supplier<SortedSet<ContentKey>> satellite = () -> {
            //todo - gfm - 5/19/16 - if the time is entirely within Spoke, answer the question locally
            //todo - gfm - 5/19/16 - otherwise merge the values from the local Spoke and the Master
            //todo - gfm - 5/19/16 - call global master with ... GET /channel/{channel}/{key}/{n|p}
            return null;
        };
        return handleGlobal(query.getChannelName(), local, satellite);
    }

    @Override
    public void getValues(String channel, SortedSet<ContentKey> keys, Consumer<Content> callback) {
        //todo - gfm - 5/19/16 - what does this do again?
    }

    @Override
    public boolean delete(String channelName) {
        Supplier<Boolean> local = () -> channelService.delete(channelName);
        Supplier<Boolean> satellite = () -> {
            //todo - gfm - 5/19/16 - call global master with ... DELETE /channel/{channel}
            return null;
        };
        return handleGlobal(channelName, local, satellite);
    }


    @Override
    public ChannelConfig getChannelConfig(String channelName, boolean allowChannelCache) {
        return channelService.getChannelConfig(channelName, allowChannelCache);
    }

    @Override
    public ChannelConfig getCachedChannelConfig(String channelName) {
        return channelService.getCachedChannelConfig(channelName);
    }

    @Override
    public Iterable<ChannelConfig> getChannels() {
        return channelService.getChannels();
    }

    @Override
    public Iterable<ChannelConfig> getChannels(String tag) {
        return channelService.getChannels(tag);
    }

}
