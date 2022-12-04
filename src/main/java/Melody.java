import java.util.List;

public class Melody {
    private List<Note> notes;

    public Melody(List<Note> notes) {
        this.notes = notes;
    }

    /**
     * Compares MIDI number and length of each note with its counterpart in target melody
     * @return value in range [0, 2*MELODY_LENGTH]
     */
    public Integer fitness(){
        int fitness = 0;
        //Compare each note individually
        for (int i = 0; i < App.MELODY_LENGTH; i++) {
            if(this.getNotes().get(i).getNote().equals(App.TARGET_MELODY.getNotes().get(i).getNote()))
                fitness++;
            if(this.getNotes().get(i).getLength().equals(App.TARGET_MELODY.getNotes().get(i).getLength()))
                fitness++;
        }
        return fitness;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for(Note note : notes){
            result.append(note.toString()).append(" ");
        }
        result.deleteCharAt(result.length()-1);
        return result.toString();
    }
}
