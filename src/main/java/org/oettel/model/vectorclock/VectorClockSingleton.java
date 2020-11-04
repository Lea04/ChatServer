package org.oettel.model.vectorclock;

import org.oettel.configuration.ServerConfigurationSingleton;

import java.util.ArrayList;
import java.util.List;

public class VectorClockSingleton {
    public static VectorClockSingleton instance;
    List<VectorClockEntry> vectorClockEntryList;

    private VectorClockSingleton() {
        VectorClockEntry serverEntry = new VectorClockEntry(ServerConfigurationSingleton.getInstance().getServerAddress(), 0);
        vectorClockEntryList = new ArrayList<>();
        vectorClockEntryList.add(serverEntry);
    }

    public static VectorClockSingleton getInstance() {
        if (VectorClockSingleton.instance == null) {
            VectorClockSingleton.instance = new VectorClockSingleton();
        }
        return VectorClockSingleton.instance;
    }

    public List<VectorClockEntry> getVectorClockEntryList() {
        return vectorClockEntryList;
    }

    public void setVectorClockEntryList(List<VectorClockEntry> vectorClockEntryList) {
        this.vectorClockEntryList = new ArrayList<VectorClockEntry>(vectorClockEntryList.size());
        for(int i = 0; i < this.vectorClockEntryList.size(); i++){
            this.vectorClockEntryList.set(i,vectorClockEntryList.get(i));
        }
    }

    public void addVectorClockEntryToList(VectorClockEntry vectorClockEntry) {
        this.vectorClockEntryList.add(vectorClockEntry);
    }

    public void updateVectorClock() {
        String internalAdress = ServerConfigurationSingleton.getInstance().getServerAddress().toString();
        this.vectorClockEntryList.forEach(vectorClockEntry -> {
            if(vectorClockEntry.getIpAdress().toString().contains(internalAdress)) {
                vectorClockEntry.addCount();
            }
        });
    }

    public void updateExternalValues(VectorClockEntry externalVectorClockEntry) {
        String internalAdress = ServerConfigurationSingleton.getInstance().getServerAddress().toString();
        this.vectorClockEntryList.forEach(vectorClockEntry -> {
            if(!vectorClockEntry.getIpAdress().toString().contains(internalAdress)
            && vectorClockEntry.getIpAdress().toString().contains(externalVectorClockEntry.getIpAdress().getHostAddress())) {
                int higherValue = compareVectorClockValues(vectorClockEntry, externalVectorClockEntry);
                vectorClockEntry.setClockCount(higherValue);
            }
        });
    }

    private int compareVectorClockValues(VectorClockEntry vectorClockEntry, VectorClockEntry externalVectorClockEntry) {
        if(vectorClockEntry.getClockCount() < externalVectorClockEntry.getClockCount()) {
            return externalVectorClockEntry.getClockCount();
        } else if (vectorClockEntry.getClockCount() > externalVectorClockEntry.getClockCount()) {
            return vectorClockEntry.getClockCount();
        } else {
            return vectorClockEntry.getClockCount();
        }
    }
}
