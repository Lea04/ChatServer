package org.oettel.businesslogic;

import org.oettel.configuration.ServerConfigurationSingleton;

public class Test implements Runnable {
    @Override

    public void run() {
/*        if((!ServerConfigurationSingleton.getInstance().getIsLeader()) && ServerConfigurationSingleton.getInstance().getLeader().equals(ServerConfigurationSingleton.getInstance().getServerAddress())){
        ServerConfigurationSingleton.getInstance().setIsLeader(true);
    }else{ServerConfigurationSingleton.getInstance().setIsLeader(false);}*/

        System.out.println("----I AM THE LEADER: "+ServerConfigurationSingleton.getInstance().getIsLeader() +".   The Leader is: "+ServerConfigurationSingleton.getInstance().getLeader());

    }
}
