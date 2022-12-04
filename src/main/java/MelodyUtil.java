import java.util.*;

public class MelodyUtil {
    private static char[] notes = {'C', 'D', 'E', 'F', 'G', 'A', 'B'};
    private static String[] lengths = {"w", "h", "q", "i", "s"};
    private static String[] modifiers = {"", "#", "b"};

    /**
     * Generates a melody of random notes
     * @return Melody object
     */
    public static Melody randomMelody(){
        int length = App.MELODY_LENGTH;
        List<Note> notes = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            notes.add(randomNote());
        }
        return new Melody(notes);
    }

    /**
     * Generates a note with random MIDI number and length
     * @return Note object
     */
    public static Note randomNote(){
        Random random = new Random();

        int note = random.nextInt(128);
        char length = lengths[random.nextInt(lengths.length)].charAt(0);

        return new Note(note, length);
    }

    /**
     * Converts a string of notes in English notation into a Melody object
     * @param string notes in English notation
     * @return Melody object
     */
    public static Melody stringToMelody(String string){
        String[] noteStrings = string.split(" ");
        List<Note> noteList = new ArrayList<>();

        for(String s : noteStrings){
            noteList.add(stringToNote(s));
        }

        return new Melody(noteList);
    }

    /**
     * Converts a single note in English notation into a Note object
     * @param string note in English notation
     * @return Note object
     */
    public static Note stringToNote(String string){
        //Default octave, length and modifier values
        char octave = '5';
        char length = 'q';
        String modifier = "";
        //Set note
        for(char c : string.toCharArray()){
            if(c >= '0' && c <= '8') octave = c;
            else if(Arrays.asList(lengths).contains(Character.toString(c))) length = c;
            else if(Arrays.asList(modifiers).contains(Character.toString(c))) modifier += c;
        }
        return new Note(string.charAt(0), Character.getNumericValue(octave), length, modifier);
    }
}
