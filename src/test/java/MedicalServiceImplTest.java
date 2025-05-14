import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalServiceImplTest {

    private PatientInfoRepository patientInfoRepository;
    private SendAlertService alertService;
    private MedicalServiceImpl medicalService;

    @BeforeEach
    void setUp() {
        patientInfoRepository = Mockito.mock(PatientInfoRepository.class);
        alertService = Mockito.mock(SendAlertService.class);
        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);

        PatientInfo testPatient = new PatientInfo(
                "test-id",
                "Иван",
                "Петров",
                LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(120, 80))
        );

        when(patientInfoRepository.getById("test-id")).thenReturn(testPatient);
    }

    @Test
    void checkBloodPressure_shouldSendAlertWhenPressureNotNormal() {

        BloodPressure abnormalPressure = new BloodPressure(140, 90);


        medicalService.checkBloodPressure("test-id", abnormalPressure);


        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(alertService).send(argumentCaptor.capture());
        assertEquals("Warning, patient with id: test-id, need help", argumentCaptor.getValue());
    }

    @Test
    void checkBloodPressure_shouldNotSendAlertWhenPressureNormal() {

        BloodPressure normalPressure = new BloodPressure(120, 80);


        medicalService.checkBloodPressure("test-id", normalPressure);


        verify(alertService, never()).send(anyString());
    }

    @Test
    void checkTemperature_shouldSendAlertWhenTemperatureNotNormal() {
        // Arrange
        BigDecimal abnormalTemperature = new BigDecimal("38.2");


        medicalService.checkTemperature("test-id", abnormalTemperature);


        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(alertService).send(argumentCaptor.capture());
        assertEquals("Warning, patient with id: test-id, need help", argumentCaptor.getValue());
    }

    @Test
    void checkTemperature_shouldNotSendAlertWhenTemperatureNormal() {

        BigDecimal normalTemperature = new BigDecimal("36.6");


        medicalService.checkTemperature("test-id", normalTemperature);


        verify(alertService, never()).send(anyString());
    }

    @Test
    void checkTemperature_shouldSendAlertWhenTemperatureTooLow() {

        BigDecimal tooLowTemperature = new BigDecimal("34.0");


        medicalService.checkTemperature("test-id", tooLowTemperature);


        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(alertService).send(argumentCaptor.capture());
        assertEquals("Warning, patient with id: test-id, need help", argumentCaptor.getValue());
    }

    @Test
    void getPatientInfo_shouldThrowExceptionWhenPatientNotFound() {

        when(patientInfoRepository.getById("unknown-id")).thenReturn(null);


        assertThrows(RuntimeException.class, () -> medicalService.checkBloodPressure("unknown-id", new BloodPressure(120, 80)));
    }
}