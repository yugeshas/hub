package com.flightstats.hub.cluster;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CuratorLeader calls Leader when it gets leadership.
 * Use a Leader when you have a long running task which needs to be canceled if the ZooKeeper connection is lost.
 * A Leader is useful when you always want a process with leadership.
 * Brief processes which don't always run can use a CuratorLock.
 */
public class CuratorLeader {

    private final static Logger logger = LoggerFactory.getLogger(CuratorLeader.class);

    private String leaderPath;
    private Leader leader;
    private final CuratorFramework curator;
    private LeaderSelector leaderSelector;

    public CuratorLeader(String leaderPath, Leader leader, CuratorFramework curator) {
        this.leaderPath = leaderPath;
        this.leader = leader;
        this.curator = curator;
    }

    /**
     * Attempt leadership. This attempt is done in the background - i.e. this method returns
     * immediately.
     * The ElectedLeader will be called from an ExecutorService.
     */
    public void start() {
        leaderSelector = new LeaderSelector(curator, leaderPath, new CuratorLeaderSelectionListener());
        leaderSelector.start();
    }

    private class CuratorLeaderSelectionListener extends LeaderSelectorListenerAdapter {

        public void takeLeadership(final CuratorFramework client) throws Exception {
            logger.info("have leadership for " + leaderPath);
            try {
                leader.takeLeadership();
            } catch (Exception e) {
                logger.warn("exception thrown from ElectedLeader " + leaderPath, e);
            }
            leaderSelector.close();
            logger.info("lost leadership " + leaderPath);
        }
    }

}

