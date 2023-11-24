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

    //preference keys
    private static final String SENSORS = "SENSORS";
    private static final String ALARM_STATUS = "ALARM_STATUS";
    private static final String ARMING_STATUS = "ARMING_STATUS";

    public FakeSecurityRepository(){
        sensors = new HashSet<>();
        this.alarmStatus = AlarmStatus.NO_ALARM;
        this.armingStatus = ArmingStatus.ARMED_AWAY;

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
    public boolean allSensorsInactive() {
        return false;
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
