import enums.CrossoverMethod;
import enums.EndCondition;
import enums.MutationMethod;
import enums.SelectionMethod;
import org.jfugue.player.Player;

import java.util.*;
import java.util.stream.IntStream;

public class Genetics {
    public static class Builder{
        private int populationSize, maxGenerations, tournamentK;
        private EndCondition endCondition;
        private CrossoverMethod crossoverMethod;
        private double mutationLikelihood;
        private MutationMethod mutationMethod;
        private SelectionMethod selectionMethod;
        private double closeEnoughPercentage;
        private int elitismCount = 0;

        public Builder(int populationSize){
            this.populationSize = populationSize;
        }

        /**
         * Condition under which genetic algorithm ends
         * EndCondition.EXACT_MATCH -> don't stop until 100% match is found
         * EndCondition.MAX_GENERATIONS -> run for a number of generations set by maxGenerations parameter
         * EndCondition.CLOSE_ENOUGH -> run until fitness match equals percentage set by closeEnoughPercentage parameter
         * @param endCondition
         */
        public Builder endCondition(EndCondition endCondition){
            this.endCondition = endCondition;
            return this;
        }

        /**
         * Number of generations to run algorithm for. Only applicable when MAX_GENERATIONS is set as endCondition
         * @param maxGenerations number of generations to run algorithm for
         */
        public Builder maxGenerations(int maxGenerations){
            this.maxGenerations = maxGenerations;
            return this;
        }

        /**
         * How a child is created from two parent chromosomes
         * CrossoverMethod.ONE_POINT -> One point crossover
         * CrossoverMethod.TWO_POINT -> Two point crossover
         * CrossoverMethod.UNIFORM -> Uniform crossover
         * @param crossoverMethod
         */
        public Builder crossoverMethod(CrossoverMethod crossoverMethod){
            this.crossoverMethod = crossoverMethod;
            return this;
        }

        /**
         * Likelihood that a mutation occurs
         * @param mutationLikelihood double in range [0,1]
         */
        public Builder mutationLikelihood(double mutationLikelihood){
            if(mutationLikelihood < 0 || mutationLikelihood > 1)
                throw new IllegalArgumentException("Mutation likelihood must be in range [0,1]");
            this.mutationLikelihood = mutationLikelihood;
            return this;
        }

        /**
         * Defines whether mutation occurs once per chromosome (MutationMethod.CHROMOSOME) or can occur multiple times (MutationMethod.GENE)
         * @param mutationMethod
         */
        public Builder mutationMethod(MutationMethod mutationMethod){
            this.mutationMethod = mutationMethod;
            return this;
        }

        /**
         * How parents for recombination are picked from population
         * SelectionMethod.TOURNAMENT -> K-tournament selection, tournamentK must be set
         * SelectionMethod.ROULETTE -> Roulette selection
         * SelectionMethod.RANK -> Rank selection
         * @param selectionMethod
         */
        public Builder selectionMethod(SelectionMethod selectionMethod){
            this.selectionMethod = selectionMethod;
            return this;
        }

        /**
         * Number of candidates from initial population from which parent will be chosen for K-tournament selection
         * @param k Integer in range [1, populationSize]
         */
        public Builder tournamentK(Integer k){
            if(k > populationSize || k < 1)
                throw new IllegalArgumentException("tournamentK must be in range [1, populationSize]");
            this.tournamentK = k;
            return this;
        }

        /**
         * How close a generation's max fitness must be to target fitness for  CLOSE_ENOUGH selection method
         * @param closeEnoughPercentage double in range [0,1]
         * @return
         */
        public Builder closeEnoughPercentage(double closeEnoughPercentage){
            if(closeEnoughPercentage < 0 || closeEnoughPercentage > 1)
                throw new IllegalArgumentException("Close enough percentage must be between 0 and 1");
            this.closeEnoughPercentage = closeEnoughPercentage;
            return this;
        }

        /**
         * Number of the best chromosomes to copy into new generation
         * @param elitismCount Integer in range [0, populationSize]
         */
        public Builder elitismCount(int elitismCount){
            if(elitismCount < 0 || elitismCount > populationSize)
                throw new IllegalArgumentException("Elitism count must be in range [0, populationSize]");
            this.elitismCount = elitismCount;
            return this;
        }

        /**
         * Creates a Genetics object with parameters set through Pattern Builder
         * @return Genetics object
         */
        public Genetics build(){
            Genetics genetics = new Genetics();
            genetics.populationSize = this.populationSize;
            genetics.maxGenerations = this.maxGenerations;
            genetics.endCondition = this.endCondition;
            genetics.crossoverMethod = this.crossoverMethod;
            genetics.mutationLikelihood = this.mutationLikelihood;
            genetics.mutationMethod = this.mutationMethod;
            genetics.selectionMethod = this.selectionMethod;
            genetics.tournamentK = this.tournamentK;
            genetics.closeEnoughPercentage = this.closeEnoughPercentage;
            genetics.elitismCount = this.elitismCount;
            genetics.random = new Random();
            genetics.generation = 0;

            return genetics;
        }
    }

    private List<Melody> population = new ArrayList<>();
    private int populationSize, maxGenerations, tournamentK;
    private EndCondition endCondition;
    private CrossoverMethod crossoverMethod;
    private double mutationLikelihood;
    private MutationMethod mutationMethod;
    private SelectionMethod selectionMethod;
    private double closeEnoughPercentage;
    private int elitismCount;
    private Random random;
    private int generation;
    private long elapsedTime;
    private Melody best;
    List<Melody> playList = new ArrayList<>();

    private Genetics(){}

    /**
     * Runs the genetic algorithm
     */
    public void run(){
        long startTime = System.nanoTime();
        //Initial population
        for (int i = 0; i < populationSize; i++) {
            population.add(MelodyUtil.randomMelody());
        }
        generation = 1;

        boolean exitCondition = false;
        //Run genetic algorithm
        while(!exitCondition){
            //Fill new population
            List<Melody> newPopulation = new ArrayList<>();
            //Elitism
            if(elitismCount > 0){
                population.sort(Comparator.comparing(Melody::fitness).reversed());
                population.subList(0, elitismCount).forEach(m -> newPopulation.add(m));
            }

            while(newPopulation.size() != populationSize){
                //Select parents
                Melody parent1 = switch(selectionMethod){
                    case TOURNAMENT -> tournament();
                    case ROULETTE -> roulette();
                    case RANK -> rank();
                };
                Melody parent2 = parent1;
                while(parent1 == parent2){
                    parent2 = switch(selectionMethod){
                        case TOURNAMENT -> tournament();
                        case ROULETTE -> roulette();
                        case RANK -> rank();
                    };
                }
                //Create new melody from parents
                Melody newMelody = switch(crossoverMethod){
                    case ONE_POINT -> onePointCrossover(parent1, parent2);
                    case TWO_POINT -> twoPointCrossover(parent1, parent2);
                    case UNIFORM -> uniformCrossover(parent1, parent2);
                };
                //Undergo mutation randomly
                mutate(newMelody);

                newPopulation.add(newMelody);
            }

            int bestFitness = newPopulation.stream().max(Comparator.comparing(Melody::fitness)).get().fitness();
            population = newPopulation;

            generation++;

            if(generation % 1000 == 0) playList.add(population.stream().max(Comparator.comparing(Melody::fitness)).get());

            if(endCondition == EndCondition.MAX_GENERATIONS && generation == maxGenerations
            || endCondition == EndCondition.EXACT_MATCH && App.TARGET_MELODY.fitness() == bestFitness
            || endCondition == EndCondition.CLOSE_ENOUGH && bestFitness/(double)App.TARGET_MELODY.fitness() >= closeEnoughPercentage)
                exitCondition = true;
        }

        best = population.stream().max(Comparator.comparing(Melody::fitness)).get();

        elapsedTime = System.nanoTime() - startTime;
    }

    /**
     * Selects K random distinct chromosomes from current population, then returns the one with highest fitness
     * tournamentK must be set
     * @return selected Melody object
     */
    private Melody tournament(){
        List<Melody> candidates = new ArrayList<>();
        //Select K random distinct chromosomes to enter tournament
        random.ints(0, population.size()-1)
                .distinct()
                .limit(tournamentK)
                .forEach(i -> candidates.add(population.get(i)));

        //Return best chromosome from candidates
        return candidates.stream().max(Comparator.comparing(Melody::fitness)).get();
    }

    /**
     * Selects a chromosome from current population using Roulette Wheel Selection
     * @return selected Melody object
     */
    private Melody roulette(){
        int populationFitness = population.stream().mapToInt(m -> m.fitness()).sum();
        int current = 0, rouletteResult = random.nextInt(populationFitness);
        for (Melody melody : population) {
            current += melody.fitness();
            if(rouletteResult < current){
                return melody;
            }
        }
        return null;
    }

    /**
     * Selects a chromosome from current population using Rank Selection
     * @return selected Melody object
     */
    private Melody rank(){
        population.sort(Comparator.comparing(Melody::fitness).reversed());
        double current = 0, randomValue = random.nextDouble();
        for (int i = 0; i < populationSize; i++) {
            current += ((double)(populationSize-i)/(populationSize*(populationSize+1)/2));
            if(randomValue < current){
                return population.get(i);
            }
        }
        return null;
    }

    /**
     * Performs one point crossover of two parent chromosomes
     * @param parent1
     * @param parent2
     * @return child chromosome
     */
    private Melody onePointCrossover(Melody parent1, Melody parent2){
        long startTime = System.nanoTime();
        int crossoverPoint = 1 + new Random().nextInt(App.MELODY_LENGTH-2);
        List<Note> notes = new ArrayList<>();
        for (int i = 0; i < crossoverPoint; i++) {
            notes.add(parent1.getNotes().get(i));
        }
        for (int i = crossoverPoint; i < App.MELODY_LENGTH; i++) {
            notes.add(parent2.getNotes().get(i));
        }
        System.out.println(System.nanoTime() - startTime);
        return new Melody(notes);
    }

    /**
     * Performs two point crossover of two parent chromosomes
     * @param parent1
     * @param parent2
     * @return child chromosome
     */
    private Melody twoPointCrossover(Melody parent1, Melody parent2){
        List<Note> notes = new ArrayList<>();
        int[] crossoverPoints = random.ints(1, App.MELODY_LENGTH-1)
                .distinct()
                .limit(2)
                .toArray();
        for(int i=0; i < crossoverPoints[0]; i++){
            notes.add(parent1.getNotes().get(i));
        }
        for(int i = crossoverPoints[0]; i < crossoverPoints[1]; i++){
            notes.add(parent2.getNotes().get(i));
        }
        for (int i = crossoverPoints[1]; i < App.MELODY_LENGTH; i++) {
            notes.add(parent1.getNotes().get(i));
        }
        return new Melody(notes);
    }

    /**
     * Performs uniform crossover of two parent chromosomes
     * @param parent1
     * @param parent2
     * @return child chromosome
     */
    private Melody uniformCrossover(Melody parent1, Melody parent2){
        List<Note> notes = new ArrayList<>();
        for (int i = 0; i < App.MELODY_LENGTH; i++) {
            Melody targetParent = random.nextBoolean() ? parent1 : parent2;
            notes.add(targetParent.getNotes().get(i));
        }
        return new Melody(notes);
    }

    /**
     * Mutates a given melody dependent on mutationMethod parameter
     * if CHROMOSOME mutation is selected mutation occurs up to once per chromosome, with mutationLikelihood determining whether a single random gene inside the chromosome is mutated
     * if Gene mutation is selected mutation can occur any number of times per chromosome, with mutationLikelihood determining whether a given gene is mutated
     * @param melody to mutate
     */
    private void mutate(Melody melody){
        switch(mutationMethod){
            case CHROMOSOME -> {
                // Mutation can occur up to once per chromosome
                // Mutation likelihood determines if a random gene inside chromosome is mutated
                if(mutationMethod == MutationMethod.CHROMOSOME){
                    if(random.nextInt(100) < mutationLikelihood*100){
                        int selectedNote = random.nextInt(melody.getNotes().size());
                        melody.getNotes().set(selectedNote, MelodyUtil.randomNote());
                    }
                }
            }
            case GENE -> {
                // Every gene can be mutated, regardless of other mutations in chromosome
                // Mutation likelihood determines if a gene is mutated
                for (int i = 0; i < melody.getNotes().size(); i++) {
                    if(random.nextInt(100) < mutationLikelihood*100){
                        melody.getNotes().set(i, MelodyUtil.randomNote());
                    }
                }
            }
        }
    }

    /**
     * Prints statistics of most recent run
     */
    public void statistics(){
        StringBuilder output = new StringBuilder();
        output.append("Total generations: ").append(generation).append("\n")
              .append("Total execution time: ").append(elapsedTime / 1000000).append("ms").append("\n")
              .append("Final fitness: ").append(best.fitness())
              .append(" (target: ").append(App.TARGET_MELODY.fitness()).append(")");

        System.out.println(output);
    }

    /**
     * Plays all stored melodies and the final melody
     */
    public void playResult() {
        Player player = new Player();
        for (int i = 0; i < playList.size(); i++) {
            System.out.printf("Now playing melody %d/%d%n",(i+1), playList.size());
            player.play(playList.get(i).toString());
        }
        System.out.println("Now playing final melody");
        player.play(this.best.toString());
    }
}
