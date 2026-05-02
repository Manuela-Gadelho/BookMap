package com.bookmap.app.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testes unitarios para LocationHelper.
 * Verifica o calculo de distancia usando a formula de Haversine.
 * Teste nao-funcional: valida a precisao do calculo de geolocalizacao.
 */
public class LocationHelperTest {

    @Test
    public void testCalculateDistanceSamePoint() {
        double distance = LocationHelper.calculateDistance(
                -23.5505, -46.6333, -23.5505, -46.6333);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    public void testCalculateDistanceSaoPauloToRio() {
        double distance = LocationHelper.calculateDistance(
                -23.5505, -46.6333,  // Sao Paulo
                -22.9068, -43.1729); // Rio de Janeiro
        assertTrue("Distancia SP-RJ deve ser ~360km", distance > 340 && distance < 380);
    }

    @Test
    public void testCalculateDistanceShort() {
        double distance = LocationHelper.calculateDistance(
                -23.5505, -46.6333,
                -23.5510, -46.6340);
        assertTrue("Distancia curta deve ser < 1km", distance < 1.0);
    }

    @Test
    public void testCalculateDistanceLong() {
        double distance = LocationHelper.calculateDistance(
                -23.5505, -46.6333,  // Sao Paulo
                48.8566, 2.3522);    // Paris
        assertTrue("Distancia SP-Paris deve ser ~9400km", distance > 9300 && distance < 9500);
    }

    @Test
    public void testCalculateDistanceEquator() {
        double distance = LocationHelper.calculateDistance(
                0.0, 0.0, 0.0, 1.0);
        assertTrue("1 grau no equador ~ 111km", distance > 110 && distance < 112);
    }

    @Test
    public void testCalculateDistanceAntipodes() {
        double distance = LocationHelper.calculateDistance(
                0.0, 0.0, 0.0, 180.0);
        assertTrue("Antipodas devem ter ~20015km", distance > 20000 && distance < 20030);
    }

    @Test
    public void testCalculateDistanceNegativeCoordinates() {
        double distance = LocationHelper.calculateDistance(
                -10.0, -50.0, -20.0, -40.0);
        assertTrue("Distancia com coordenadas negativas deve ser > 0", distance > 0);
    }

    @Test
    public void testCalculateDistanceSymmetric() {
        double d1 = LocationHelper.calculateDistance(-23.5, -46.6, -22.9, -43.2);
        double d2 = LocationHelper.calculateDistance(-22.9, -43.2, -23.5, -46.6);
        assertEquals("Distancia deve ser simetrica", d1, d2, 0.001);
    }

    @Test
    public void testCalculateDistancePrecision() {
        double distance = LocationHelper.calculateDistance(
                -23.5505, -46.6333,
                -23.5605, -46.6433);
        assertTrue("Distancia ~1.5km deve ter precisao adequada", distance > 1.0 && distance < 2.0);
    }
}
