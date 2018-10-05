package enigma;

public class Rotore {
    private int shift;
    private String rotore_stringa;

    /*
        Si porta il rotore in configurazione inziale indicata dal file di configurazione
     */
    public Rotore(String str, int configurazione_iniziale){
        this.rotore_stringa = str;
        this.shift = configurazione_iniziale;
    }

    /*
        Questo metodo restituisce il carattere corrispondente all'input c
        in base allo shift attuale
     */

    public void get_corresponding_char(char c){

        int index = (int) c - 65;
        c=rotore_stringa.charAt((shift+index+1)%26);

    }

    public void get_corrisponding_index(char c){
        int i=rotore_stringa.indexOf(c)-65;
        c = (char) i;
    }

    /*
        Incrementa lo shift del rotore.
        E' necessaria poichè lo shift del rotore dipende dalla sua posizione
     */
    public void shift(){
        this.shift += 1;
    }


}
