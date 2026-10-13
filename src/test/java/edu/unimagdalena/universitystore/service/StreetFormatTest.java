package edu.unimagdalena.universitystore.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StreetFormatTest {

    @Test
    void rewritesFullAddressesInTheFormLayout() {
        assertEquals("Calle 13 # 3-84", StreetFormat.normalize("CALLE 13 #3-84"));
        assertEquals("Calle 43 # 87A-8", StreetFormat.normalize("calle 43 #87A-8"));
        assertEquals("Avenida Calle 26 # 68B-70", StreetFormat.normalize(" avenida calle 26 # 68b - 70 "));
    }

    @Test
    void keepsWhatThereIsWhenCrossAndPlateAreMissing() {
        assertEquals("Calle 30", StreetFormat.normalize("calle 30"));
    }

    @Test
    void leavesUnrecognisedTextAlone() {
        assertEquals("Finca La Esperanza, vereda El Mango", StreetFormat.normalize("Finca La Esperanza, vereda El Mango"));
        assertNull(StreetFormat.normalize(null));
    }
}
