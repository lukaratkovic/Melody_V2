import enums.CrossoverMethod;
import enums.EndCondition;
import enums.MutationMethod;
import enums.SelectionMethod;

public class App {
//    Mary Had a Little Lamb
//    public static final String TARGET_MELODY_STRING = "E D C D E E Eh D D Dh E G Gh";
//    Happy Birthday
//    public static final String TARGET_MELODY_STRING = "A A B A D C# A A B A E D A A A F# D E D A A B A D C# A A B A E D A A A F# D C# B G G F# D E D";
    // Jingle Bells
//    public static final String TARGET_MELODY_STRING = "E E Eh E E Eh E G C D Ew F F F F F E E E E E D D E Dh Gh";
//    Ode to Joy
    public static final String TARGET_MELODY_STRING = "E E F G G F E D C C D E E D Dh E E F G G F E D C C D E D C Ch D D E C D F E C D F E D C D Gh E E F G G F E D C C D E D C Ch";
    public static final Melody TARGET_MELODY = MelodyUtil.stringToMelody(TARGET_MELODY_STRING);
    public static final int MELODY_LENGTH = TARGET_MELODY_STRING.split(" ").length;

    public static void main(String[] args) {
        Genetics genetics =  new Genetics.Builder(20)
                .endCondition(EndCondition.EXACT_MATCH)
//                .endCondition(EndCondition.MAX_GENERATIONS)
//                .maxGenerations(100)
//                .endCondition(EndCondition.CLOSE_ENOUGH)
//                .closeEnoughPercentage(0.8)
                .crossoverMethod(CrossoverMethod.TWO_POINT)
                .mutationLikelihood(0.5)
                .mutationMethod(MutationMethod.CHROMOSOME)
                .selectionMethod(SelectionMethod.TOURNAMENT)
                .tournamentK(10)
                .elitismCount(2)
                .build();

        genetics.run();
        genetics.statistics();

        genetics.playResult();
    }
}