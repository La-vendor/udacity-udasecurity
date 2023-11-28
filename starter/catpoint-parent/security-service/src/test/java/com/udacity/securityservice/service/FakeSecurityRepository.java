package com.udacity.securityservice.service;

import com.udacity.securityservice.data.AlarmStatus;
import com.udacity.securityservice.data.ArmingStatus;
import com.udacity.securityservice.data.SecurityRepository;
import com.udacity.securityservice.data.Sensor;

import java.util.HashSet;
import java.util.Set;

public class FakeSecurityRepository implements SecurityRepository {

    private Set<Sensor> sensors;
    private AlarmStatus alarmStatus;
    private ArmingStatus armingStatus;

    private boolean catDetected;

    public FakeSecurityRepository() {

        sensors = new HashSet<>();
    }

    @Override
    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }

    @Override
    public void removeSensor(Sensor sensor) {
        sensors.remove(sensor);
    }

    @Override
    public void updateSensor(Sensor sensor) {
        sensors.remove(sensor);
        sensors.add(sensor);
    }

    @Override
    public void setAlarmStatus(AlarmStatus alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    @Override
    public void setArmingStatus(ArmingStatus armingStatus) {
        this.armingStatus = armingStatus;
    }

    @Override
    public void resetAllSensors() {
        for(Sensor sensor : sensors){
            sensor.setActive(false);
        }
    }

    @Override
    public void catDetected(boolean cat) {
        this.catDetected = cat;
    }

    @Override
    public boolean allSensorsInactive() {
        for(Sensor sensor : sensors){
            if (sensor.getActive()) return false;
        }
        return true;
    }

    @Override
    public boolean isCatDetected() {
        return this.catDetected;
    }

    @Override
    public Set<Sensor> getSensors() {
        return sensors;
    }

    @Override
    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    @Override
    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }
}
