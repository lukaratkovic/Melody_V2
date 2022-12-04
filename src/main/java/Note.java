/**
 * Describes a note containing a MIDI code and length of note
 */
public class Note {
    private Integer note;
    private Character length;

    /**
     *
     * @param note MIDI value of note (0-127)
     * @param lenght length of note in JFugue notation (w, h, q, i, s)
     */
    public Note(Integer note, Character lenght) {
        this.note = note;
        this.length = lenght;
    }

    /**
     *
     * @param note English notation (C, D, E, F, G, A, B)
     * @param octave number 0-10
     * @param length length of note in JFugue notation (w, h, q, i, s)
     * @param modifier sharp (#) or flat (b), empty string if neither
     */
    public Note(Character note, Integer octave, Character length, String modifier){
        this.length = length;
        this.note = switch(note.toString()+modifier){
            case "C" -> 0;
            case "C#", "Db" -> 1;
            case "D" -> 2;
            case "D#", "Eb" -> 3;
            case "E" -> 4;
            case "F" -> 5;
            case "F#", "Gb" -> 6;
            case "G" -> 7;
            case "G#", "Ab" -> 8;
            case "A" -> 9;
            case "A#", "Bb" -> 10;
            case "B" -> 11;
            default -> throw new IllegalArgumentException("Supplied note or modifier is invalid");
        };
        this.note += octave * 12;
    }

    public Integer getNote() {
        return note;
    }

    public Character getLength() {
        return length;
    }

    @Override
    public String toString() {
        return note.toString()+ length;
    }
}
