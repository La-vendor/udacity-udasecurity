package com.udacity.securityservice.service;

import com.udacity.securityservice.data.SecurityRepository;
import com.udacity.securityservice.data.Sensor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SecurityServiceIntegrationTest {

    SecurityRepository securityRepository;
    SecurityService securityService;
    Sensor sensor;

    @BeforeAll
    void init(){
        securityRepository = new FakeSecurityRepository();
    }
}
