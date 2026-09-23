package apiParts.generators.suppliers;

import apiParts.models.encounter.CreateEncounterRequest.Obs;
import apiParts.testdata.VitalsTestData;

import java.util.List;
import java.util.function.Supplier;

// Every vitals concept with a random value inside its range
public class RandomVitalsObs implements Supplier<List<Obs>> {
    @Override
    public List<Obs> get() {
        return VitalsTestData.vitalsObs();
    }
}
