package com.udacity.securityservice.service;

import com.udacity.imageservice.service.ImageService;
import com.udacity.securityservice.application.StatusListener;
import com.udacity.securityservice.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    SecurityService securityService;
    Sensor sensor;

    @Mock
    SecurityRepository securityRepository;

    @Mock
    ImageService imageService;

    @Mock
    StatusListener statusListener;

    @BeforeEach
    void init() {

        securityService = new SecurityService(securityRepository, imageService);
        securityService.addStatusListener(statusListener);

        sensor = new Sensor("testSensor1", SensorType.DOOR);
    }

    //1 & 2
    @ParameterizedTest(name = "When {0} and {1} and sensor activated, put system in {2}")
    @MethodSource("systemStatusProvider")
    public void changeAlarmStateWhenSensorActivated(AlarmStatus alarmStatus, ArmingStatus armingStatus, AlarmStatus expectedAlarmStatus) {

        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(expectedAlarmStatus);
    }

    private static Stream<Arguments> systemStatusProvider() {
        return Stream.of(
                //add test name
                Arguments.of(AlarmStatus.NO_ALARM, ArmingStatus.ARMED_AWAY, AlarmStatus.PENDING_ALARM),
                Arguments.of(AlarmStatus.NO_ALARM, ArmingStatus.ARMED_HOME, AlarmStatus.PENDING_ALARM),
                Arguments.of(AlarmStatus.PENDING_ALARM, ArmingStatus.ARMED_AWAY, AlarmStatus.ALARM),
                Arguments.of(AlarmStatus.PENDING_ALARM, ArmingStatus.ARMED_HOME, AlarmStatus.ALARM)
        );
    }

    //3
    @Test
    public void ifPendingAlarmAndAllSensorsInactiveReturnToNoAlarmState() {

        sensor.setActive(true);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.allSensorsInactive()).thenReturn(true);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //4
    @ParameterizedTest(name = "Initial sensor state: {0}, change to state: {1}")
    @CsvSource({
            "true, false",
            "false, true"
    })
    public void ifAlarmIsActiveChangeInSensorStateWillNotAffectAlarmState(boolean startSensorState) {

        sensor.setActive(startSensorState);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, !startSensorState);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //5
    @Test
    public void ifSensorIsActivatedAndAlreadyActiveAndSystemIsInPendingChangeToAlarm() {

        sensor.setActive(true);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //6
    @ParameterizedTest(name = "Initial alarm state: {0}")
    @CsvSource({
            "NO_ALARM",
            "PENDING_ALARM",
            "ALARM"
    })
    public void ifSensorIsDeactivatedWhileAlreadyInactiveMakeNoChangesToAlarmStatus(AlarmStatus alarmStatus) {

        sensor.setActive(false);

        lenient().when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //7
    @Test
    public void ifImageServiceDetectsCatWhileSystemIsArmedHomeChangeSystemStatusToAwooga() {

        BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);

        securityService.processImage(image);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 8
    @Test
    public void ifImageServiceDoesNotDetectCatAndSensorsAreNotActivatedChangeSystemStatusToNoAlarm() {

        BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);

        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(false);
        when(securityRepository.allSensorsInactive()).thenReturn(true);
        securityService.processImage(image);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //9
    @Test
    public void ifSystemIsDisarmedSetSystemStatusToNoAlarm() {

        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //10
    @ParameterizedTest
    @CsvSource({
            "ARMED_HOME",
            "ARMED_AWAY"
    })
    public void ifSystemIsArmedResetAllSensorsToInactive(ArmingStatus armingStatus){

        securityService.setArmingStatus(armingStatus);

        verify(securityRepository).resetAllSensors();
    }

    //11
    @Test
    public void ifSystemIsArmedHomeWhileCatIsDetectedPutSystemInAlarm(){

        when(securityRepository.isCatDetected()).thenReturn(true);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

}