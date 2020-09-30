package org.oettel.businesslogic;

import org.oettel.configuration.ServerConfigurationSingleton;

public class Test implements Runnable {
    @Override
    public void run() {
        System.out.println("I am the Leader: "+ServerConfigurationSingleton.getInstance().getIsLeader() +".   The Leader is: "+ServerConfigurationSingleton.getInstance().getLeader());

    }
}
