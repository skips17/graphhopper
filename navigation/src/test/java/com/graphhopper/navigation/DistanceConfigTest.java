package com.graphhopper.navigation;
import static org.junit.jupiter.api.Assertions.*;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.TransportationMode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DistanceConfigTest {

    @Test
    public void distanceConfigTest() {
        // from TransportationMode
        DistanceConfig car = new DistanceConfig(DistanceUtils.Unit.METRIC, null, null, TransportationMode.CAR);
        assertEquals(4, car.voiceInstructions.size());
        DistanceConfig foot = new DistanceConfig(DistanceUtils.Unit.METRIC, null, null, TransportationMode.FOOT);
        assertEquals(1, foot.voiceInstructions.size());
        DistanceConfig bike = new DistanceConfig(DistanceUtils.Unit.METRIC, null, null, TransportationMode.BIKE);
        assertEquals(1, bike.voiceInstructions.size());
        DistanceConfig bus = new DistanceConfig(DistanceUtils.Unit.METRIC, null, null, TransportationMode.BUS);
        assertEquals(4, bus.voiceInstructions.size());

        // from Profile
        GraphHopper hopper = new GraphHopper().setProfiles(
                new Profile("my_truck"),
                new Profile("foot"),
                new Profile("ebike").putHint("navigation_mode", "bike"));
        assertEquals(TransportationMode.CAR, hopper.getNavigationMode("unknown"));
        assertEquals(TransportationMode.CAR, hopper.getNavigationMode("my_truck"));
        assertEquals(TransportationMode.FOOT, hopper.getNavigationMode("foot"));
        assertEquals(TransportationMode.BIKE, hopper.getNavigationMode("ebike"));

        // from String
        DistanceConfig driving = new DistanceConfig(DistanceUtils.Unit.METRIC, null, null, "driving");
        assertEquals(4, driving.voiceInstructions.size());
        DistanceConfig anything = new DistanceConfig(DistanceUtils.Unit.METRIC, null, null, "anything");
        assertEquals(4, anything.voiceInstructions.size());
        DistanceConfig none = new DistanceConfig(DistanceUtils.Unit.METRIC, null, null, "");
        assertEquals(4, none.voiceInstructions.size());
        DistanceConfig biking = new DistanceConfig(DistanceUtils.Unit.METRIC, null, null, "biking");
        assertEquals(1, biking.voiceInstructions.size());
    }
    @Test
    void TestConfigImperialCar() {
        Distanceonfig dc = new DistanceConfig(DistanceUtils.Unit.IMPERIAL, null, null, "driving");
        assertEquals(4, dc.voiceInstructions.size());
        assertTrue(dc.voiceInstructions.get(0) instanceof InitialVoiceInstructionConfig);
        assertTrue(dc.voiceInstructions.get(1) instanceof FixedDistanceVoiceInstructionConfig);
        assertTrue(dc.voiceInstructions.get(2) instanceof FixedDistanceVoiceInstructionConfig);
        assertTrue(dc.voiceInstructions.get(3) instanceof ConditionalDistanceVoiceInstructionConfig);
    }
    @Test
    void TestConfigMetriclWalking() {
        DistanceConfig dc = new DistanceConfig(DistanceUtils.Unit.METRIC, null, null, TransportationMode.FOOT);
        assertEquals(1, dc.voiceInstructions.size());
        assertTrue(dc.voiceInstructions.get(0) instanceof ConditionalDistanceVoiceInstructionConfig);
    }
    @Test
    void TestConfigImperialCycling() {
        DistanceConfig dc = new DistanceConfig(DistanceUtils.Unit.IMPERIAL, null, null, TransportationMode.BIKE);
        assertEquals(1, dc.voiceInstructions.size());
        assertTrue(dc.voiceInstructions.get(0) instanceof ConditionalDistanceVoiceInstructionConfig);
    }


}
