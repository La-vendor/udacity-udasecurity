package com.udacity.securityservice.service;

import com.udacity.imageservice.service.FakeImageService;
import com.udacity.imageservice.service.ImageService;
import com.udacity.securityservice.data.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SecurityServiceIntegrationTest {

    SecurityRepository securityRepository;
    SecurityService securityService;
    Sensor sensor1, sensor2;
    ImageService imageService;
    BufferedImage image;

    @BeforeAll
    void beforeAll() {
        securityRepository = new FakeSecurityRepository();
        imageService = new FakeImageService();
        securityService = new SecurityService(securityRepository, imageService);

        sensor1 = new Sensor("testSensor1", SensorType.DOOR);
        sensor2 = new Sensor("testSensor2", SensorType.DOOR);

        securityService.addSensor(sensor1);
        securityService.addSensor(sensor2);

        image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);


    }


    //1 If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @Test
    public void ifAlarmIsArmedAndSensorActivatedPutSystemInPendingAlarm() {

        securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_AWAY);

        securityService.changeSensorActivationStatus(sensor1, true);
        assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());

    }

    //2 If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    @Test
    public void ifAlarmIsArmedAndSensorActivatedAndSystemIsInPendingPutSystemInAlarm() {

        securityRepository.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_AWAY);

        securityService.changeSensorActivationStatus(sensor2, true);
        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());

    }

    //3 If pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    public void ifPendingAlarmAndAllSensorsInactiveReturnToNoAlarmState() {

        securityService.changeSensorActivationStatus(sensor1, false);
        securityService.changeSensorActivationStatus(sensor2, false);
        securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_AWAY);

        securityService.changeSensorActivationStatus(sensor1, true);

        assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());

        securityService.changeSensorActivationStatus(sensor1, false);

        assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());


    }

    //4 If alarm is active, change in sensor state should not affect the alarm state.
    @Test
    public void ifAlarmIsActiveChangeInSensorStateWillNotAffectAlarmState() {

        securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_AWAY);

        securityService.changeSensorActivationStatus(sensor1, true);
        securityService.changeSensorActivationStatus(sensor2, true);

        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());

        securityService.changeSensorActivationStatus(sensor2, false);

        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    //5 If a sensor is activated while already active and the system is in pending state, change it to alarm state.
    @Test
    public void ifSensorIsActivatedAndAlreadyActiveAndSystemIsInPendingChangeToAlarm() {

        securityRepository.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_AWAY);

        securityService.changeSensorActivationStatus(sensor1, true);

        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    //6 If a sensor is deactivated while already inactive, make no changes to the alarm state.
    @Test
    public void ifSensorIsDeactivatedWhileAlreadyInactiveMakeNoChangesToAlarmStatus() {

        securityRepository.setAlarmStatus(AlarmStatus.ALARM);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_AWAY);

        securityService.changeSensorActivationStatus(sensor2, false);

        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    //7 If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.

    @RepeatedTest(10)
    public void ifImageServiceDetectsCatWhileSystemIsArmedHomeChangeSystemStatusToAwooga() {

        securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_HOME);

        securityService.processImage(image);

        if (securityRepository.isCatDetected())
            assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
        else
            assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
    }

    //8 If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    @RepeatedTest(10)
    public void ifImageServiceDoesNotDetectCatAndSensorsAreActivatedDoNotChangeSystemStatus() {

        securityRepository.setAlarmStatus(AlarmStatus.ALARM);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_HOME);

        securityService.changeSensorActivationStatus(sensor1, true);

        securityService.processImage(image);

        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());

    }

    @RepeatedTest(10)
    public void ifImageServiceDoesNotDetectCatAndSensorsAreNotActivatedChangeSystemStatusToNoAlarm() {

        securityRepository.setArmingStatus(ArmingStatus.DISARMED);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityRepository.setAlarmStatus(AlarmStatus.ALARM);


        securityService.processImage(image);

        securityService.changeSensorActivationStatus(sensor1, false);
        securityService.changeSensorActivationStatus(sensor2, false);

        if (securityRepository.isCatDetected())
            assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
        else
            assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
    }

    //9 If the system is disarmed, set the status to no alarm.
    @Test
    public void ifSystemIsDisarmedSetSystemStatusToNoAlarm() {

        securityRepository.setAlarmStatus(AlarmStatus.ALARM);
        securityRepository.setArmingStatus(ArmingStatus.ARMED_AWAY);

        securityService.setArmingStatus(ArmingStatus.DISARMED);

        assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
    }

    //10 If the system is armed, reset all sensors to inactive.
    @Test
    public void ifSystemIsArmedResetAllSensorsToInactive() {

        securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
        securityRepository.setArmingStatus(ArmingStatus.DISARMED);

        securityService.changeSensorActivationStatus(sensor1, true);
        securityService.changeSensorActivationStatus(sensor2, true);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        assertTrue(securityRepository.allSensorsInactive());
    }

    //11 If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    @RepeatedTest(10)
    public void ifSystemIsArmedHomeWhileCatIsDetectedPutSystemInAlarm() {

        securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
        securityRepository.setArmingStatus(ArmingStatus.DISARMED);

        securityService.processImage(image);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        if (securityRepository.isCatDetected())
            assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
        else
            assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
    }
}
