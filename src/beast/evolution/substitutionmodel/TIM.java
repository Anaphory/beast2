package beast.evolution.substitutionmodel;

import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.RealParameter;
import beast.evolution.datatype.DataType;
import beast.evolution.datatype.Nucleotide;

@Description("Transition model of nucleotide evolution (variable transition rates, two transversion rates). " +
        "Rates that are not specified are assumed to be 1.")
public class TIM extends GeneralSubstitutionModel {

    // Transition rates
    public Input<RealParameter> rateAGInput = new Input<RealParameter>("rateAG", "substitution rate for A to G (default 1)");
    public Input<RealParameter> rateCTInput = new Input<RealParameter>("rateCT", "substitution rate for C to T (default 1)");

    // Transversion rates
    public Input<RealParameter> rateTransversions1Input = new Input<RealParameter>("rateTransversions1", "substitution rate for A<->C and G<->T");
    public Input<RealParameter> rateTransversions2Input = new Input<RealParameter>("rateTransversions2", "substitution rate for C<->G and A<->T");

    RealParameter rateAG;
    RealParameter rateCT;
    RealParameter rateTransversions1;
    RealParameter rateTransversions2;

    public TIM() {
        ratesInput.setRule(Validate.OPTIONAL);
        try {
            ratesInput.setValue(null, this);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    @Override
    public void initAndValidate() throws Exception {
        if (ratesInput.get() != null) {
            throw new Exception("the rates attribute should not be used. Use the individual rates rateAG, rateCT, etc, instead.");
        }

        frequencies = frequenciesInput.get();
        updateMatrix = true;
        nrOfStates = frequencies.getFreqs().length;
        if (nrOfStates != 4) {
            throw new Exception("Frequencies has wrong size. Expected 4, but got " + nrOfStates);
        }

        eigenSystem = createEigenSystem();
        rateMatrix = new double[nrOfStates][nrOfStates];
        relativeRates = new double[nrOfStates * (nrOfStates - 1)];
        storedRelativeRates = new double[nrOfStates * (nrOfStates - 1)];

        rateAG = getParameter(rateAGInput);
        rateCT = getParameter(rateCTInput);
        rateTransversions1 = getParameter(rateTransversions1Input);
        rateTransversions2 = getParameter(rateTransversions2Input);
    }

    private RealParameter getParameter(Input<RealParameter> parameterInput) throws Exception {
        if (parameterInput.get() != null) {
            return parameterInput.get();
        }
        return new RealParameter("1.0");
    }

    @Override
    protected void setupRelativeRates() {
        relativeRates[0] = rateTransversions1.getValue(); // A->C
        relativeRates[1] = rateAG.getValue(); // A->G
        relativeRates[2] = rateTransversions2.getValue(); // A->T

        relativeRates[3] = rateTransversions1.getValue(); // C->A
        relativeRates[4] = rateTransversions2.getValue(); // C->G
        relativeRates[5] = rateCT.getValue(); // C->T

        relativeRates[6] = rateAG.getValue(); // G->A
        relativeRates[7] = rateTransversions2.getValue(); // G->C
        relativeRates[8] = rateTransversions1.getValue(); // G->T

        relativeRates[9] = rateTransversions2.getValue(); // T->A
        relativeRates[10] = rateCT.getValue(); //T->C
        relativeRates[11] = rateTransversions1.getValue(); //T->G
    }

    @Override
    public boolean canHandleDataType(DataType dataType) {
        return dataType instanceof Nucleotide;
    }
}