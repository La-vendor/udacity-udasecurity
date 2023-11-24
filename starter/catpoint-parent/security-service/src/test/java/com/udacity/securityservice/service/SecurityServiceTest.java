package com.udacity.securityservice.service;

import com.udacity.imageservice.service.FakeImageService;
import com.udacity.securityservice.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    SecurityService securityService;
    FakeImageService fakeImageService;

    @Mock
    SecurityRepository securityRepository;


    @BeforeEach
    void init() {
        fakeImageService = new FakeImageService();
        securityService = new SecurityService(securityRepository, fakeImageService);
    }


    @ParameterizedTest(name = "When {0} and {1} and sensor activated, put system in {2}")
    @MethodSource("systemStatusProvider")

    public void changeSystemStatusWhenSensorActivated(AlarmStatus alarmStatus, ArmingStatus armingStatus, AlarmStatus expectedAlarmStatus) {

        Sensor sensor = new Sensor("testSensor1", SensorType.DOOR);
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
}