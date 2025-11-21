package com.graphhopper.navigation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.util.TranslationMap;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NavigateResourceTest {

    @Test
    public void voiceInstructionsTest() {

        List<Double> bearings = NavigateResource.getBearing("");
        assertEquals(0, bearings.size());
        assertEquals(Collections.EMPTY_LIST, bearings);

        bearings = NavigateResource.getBearing("100,1");
        assertEquals(1, bearings.size());
        assertEquals(100, bearings.get(0), .1);

        bearings = NavigateResource.getBearing(";100,1;;");
        assertEquals(4, bearings.size());
        assertEquals(100, bearings.get(1), .1);
    }
    @Disabled
    @Test
    void TestGetBearingErreurNonNumeric() {
        assertThrows(IllegalArgumentException.class, () -> NavigateResource.getBearing("abc,1"));
    }
    @Disabled
    @Test
    void TestGetBearingParseWithNaN() {
        var b = NavigateResource.getBearing("100,1;;200,1;");
        assertEquals(4, b.size());
        assertEquals(100d, b.get(0), 0.1);
        assertTrue(Double.isNaN(b.get(1)));
        assertEquals(200d, b.get(2), 0.1);
        assertTrue(Double.isNaN(b.get(3)));
    }
    @Disabled
    @Test
    void TestDoGetStepsDesactive() {
        NavigateResource res = new NavigateResource(null, new TranslationMap(), new GraphHopperConfig());

        assertThrows(IllegalArgumentException.class, () ->
            res.doGet(
                null, null, null,         
                false,               
                true,                  
                true,                  
                true,                
                "metric",                 
                "simplified",             
                "polyline6",              
                "",                       
                "en",                     
                "driving"                 
            )
        );
    }
}
