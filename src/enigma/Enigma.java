package enigma;
import java.io.*;
import java.lang.String;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;
import java.lang.Exception;


public class Enigma {
    private final static Logger logger = Logger.getLogger(Enigma.class.getName());

    public static void main(String[] args){
        set_logger_handler();
        ArrayList<Integer> list_number_rotors = new ArrayList<>();
        ArrayList<Integer> initial_positions = new ArrayList<>();
        Map<Character, Character> plugboard = new HashMap<>();
        Map<Character, Character> reflector = new HashMap<>();
        ArrayList<String> rotors_strings;
        String cipher = "";
        String plain_text;

        try{
            read_configuration_files(list_number_rotors, initial_positions, plugboard, reflector);
        }catch(FileNotFoundException e){
            e.printStackTrace();
            logger.severe("Configuration file not found");
            return;
        }catch (NullPointerException e){
            e.printStackTrace();
            logger.severe("Configuration file is not set properly.");
            return;
        }catch(Exception e){
            e.printStackTrace();
            logger.severe("Configuration file is not set properly. Try with different configuration");
            return;
        }

        try {
            rotors_strings = read_rotors_info(list_number_rotors);
        }catch(IOException ex){
            ex.printStackTrace();
            logger.severe("Some problem occurs during the reading of rotori.txt file");
            return;
        }catch(Exception e){
            e.printStackTrace();
            logger.severe("Rotors file is not set properly.");
            return;
        }

        Rotore rot1 = new Rotore(rotors_strings.get(0),initial_positions.get(0));
        Rotore rot2 = new Rotore(rotors_strings.get(1),initial_positions.get(1));
        Rotore rot3 = new Rotore(rotors_strings.get(2),initial_positions.get(2));

        try {
            plain_text = read_file("cnf/testEnigma.txt");
        }catch (Exception e){
            logger.severe("Some problem occurs during the reading of the file");
            return;
        }
        cipher = encrypt(rot1,rot2,rot3,plugboard,reflector,plain_text);
        logger.info(cipher);

    }

    /*
        Metodo per la creazione del logger
     */
    private static void set_logger_handler(){
        try{
            File logger_dict = new File("log");
            boolean success;

            if (! logger_dict.exists()){
                success = logger_dict.mkdir();

                if (!success)
                    throw new IOException();
            }

            File logger_file = new File("./log/Enigma.log");
            if (! logger_file.isFile()){
                success = logger_file.createNewFile();

                if(!success)
                    throw new IOException();
            }

            Handler handler = new FileHandler("./log/Enigma.log");

            handler.setFormatter(new SimpleFormatter());

            logger.addHandler(handler);
        }catch(IOException e){
            e.printStackTrace();
            logger.severe("File Handler Error: it is not possible to log into file Enigma.log");
        }
    }

    private static String read_file(String path) throws Exception{
        File plain_text = new File(path);
        BufferedReader reader;
        String text, new_text="";


        reader = new BufferedReader(new FileReader(plain_text));

        while ((text = reader.readLine()) != null) {
            for(char a: text.toCharArray()){
                new_text += text.replaceAll("[^a-zA-Z ]", "");
            }

        }

        return new_text;

    }
    /*
        Metodo per la lettura del file di configurazione. La funzione lancia un'eccezione se il file di configurazione
        non è scritto secondo le seguenti specifiche:
            - Il numero di rotori deve essere uguale al numero di posizioni iniziali e devono essere diversi da 0
            - Il numero di coppie della plugboard deve essere compreso tra 7 e 10
            - Le lettere non sono accoppiate correttamente
            - Si incontra un carattere diverso dalle lettere dell'alfabeto.

        Decisioni:
        Se tra le posizioni iniziali è presente un carattere che non è possibile convertire in intero, viene interpretato
        come 0.
        Se si incontra un carattere minuscolo viene convertito automaticamente in maiuscolo.

     */
    private static void read_configuration_files(List<Integer> rotors, List<Integer> initial_positions, Map<Character, Character> plugboard, Map<Character, Character> reflector) throws Exception{
        File rotor_file = new File("./cnf/configurazione.txt");
        BufferedReader reader;
        String text;
        String[] splitted;
        String[] plugboard_string = null;
        String[] reflector_string = null;
        int number_rotor;

        reader = new BufferedReader(new FileReader(rotor_file));

        while ((text = reader.readLine()) != null) {
            splitted = text.split(":");

            switch (splitted[0]) {
                case "Rotori":
                    splitted = splitted[1].split(",");
                    for(String string_element : splitted) {
                        number_rotor = Integer.parseInt(string_element.trim());
                        if (rotors.contains(number_rotor))
                            throw new Exception();
                        else
                            rotors.add(number_rotor);
                    }
                    break;
                case "Posizione iniziale":
                    splitted = splitted[1].split(",");
                    for(String string_element : splitted) {
                        try {
                            initial_positions.add(Integer.parseInt(string_element.trim()));
                        }catch(NumberFormatException e){
                            logger.warning("Initial position is not a value, the programm will use 0 insteam of '" + string_element + "'");
                        }
                    }
                    break;
                case "Scambiatore":
                    plugboard_string = splitted[1].split(" ");
                    break;
                case "Riflettore":
                    reflector_string = splitted[1].split(" ");
                    break;
            }
        }

        if(rotors.size() == 0 || initial_positions.size() == 0 || rotors.size() != initial_positions.size()) {
            logger.severe("Rotors and thei initial positions are not configured correctly");
            reader.close();
            throw new Exception();
        }else{
            logger.info("Rotors: " + rotors);
            logger.info("Initial Positions: " + initial_positions);
        }

        if (plugboard_string != null) {
            create_map_from_pairs(plugboard_string, plugboard);
        }else{
            logger.severe("Plugboard is not configured in configuration file");
            throw new Exception();
        }

        if (reflector_string != null) {
            create_map_from_pairs(reflector_string, reflector);
        }else{
            logger.severe("Reflector is not configured in configuration file");
            throw new Exception();
        }


        if(reflector.size() != 26){
            if (reflector.size() == 24){
                reconstruct_pair_from_reflector(reflector);
                logger.info("Reflector reconstructed");
            }else {
                logger.severe("Reflector is not configured properly. Number of pairs not valid : " + reflector.size());
                throw new Exception();
            }
        }else{
            logger.info(reflector.toString());
        }

        if (plugboard.size() > 20 || plugboard.size() < 14){
            logger.severe("Plugboard is not configured properly. Plugboard should have from 7 to 10 pairs." +
                    "Number of pairs: " + plugboard.size()/2);
            throw new Exception();
        }else{
            logger.info(plugboard.toString());
        }

    }

    private static void reconstruct_pair_from_reflector(Map<Character, Character> reflector) throws Exception{
        char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
                'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        Set<Character> keys_set = reflector.keySet();
        char a = '0';
        char b = '0';

        boolean first = true;

        for(char character : alphabet){
            if (!keys_set.contains(character) && first){
                logger.info("Character found: " + character );
                a = character;
                first = false;
            }else if(!keys_set.contains(character) && !first){
                logger.info("Character found: " + character );
                b = character;
            }
        }

        if (a != '0' && b != '0')
            reflector.put(a, b);
        else{
            throw new Exception();
        }

    }

    private static char check_character(char a) throws Exception{
        if (Character.isAlphabetic(a)){
            return Character.toUpperCase(a);
        }else{
            throw new Exception();
        }
    }

    private static void create_map_from_pairs(String[] array_str, Map<Character, Character> map_to_create) throws Exception{
        String trim_string;
        char first_c, second_c;

        for (String pair : array_str){
            trim_string = pair.trim();
            if (trim_string.length() == 2){
                first_c = trim_string.charAt(0);
                second_c = trim_string.charAt(1);


                if(map_to_create.containsKey(first_c) || map_to_create.containsKey(second_c))
                    throw new Exception();

                first_c = check_character(first_c);
                second_c = check_character(second_c);

                map_to_create.put(first_c, second_c);
                map_to_create.put(second_c, first_c);
            }else if(trim_string.length() != 0){
                logger.severe("Pair not valid '" + trim_string + "'");
                throw new Exception();
            }
        }
    }

    /*
        Metodo per la lettura dei rotori.
     */

    private static boolean isAlphabeticString(String str){
        for (char c : str.toCharArray()) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    private static ArrayList<String> read_rotors_info(List<Integer> list_rotors) throws Exception{
        File rotor_file = new File("./cnf/rotori.txt");
        BufferedReader reader;
        int number_rotor;
        ArrayList<String> rotors_strings = new ArrayList<>();

        for(int number_rotor_in_list : list_rotors){
            rotors_strings.add("");
        }

        reader = new BufferedReader(new FileReader(rotor_file));

        String text;
        while ((text = reader.readLine()) != null) {
            if(text.contains("Rotore#")) {
                number_rotor = Integer.parseInt(text.split("#")[1]);
                if (list_rotors.contains(number_rotor)) {
                    text = reader.readLine();
                    if (text.length() == 26 && isAlphabeticString(text)) {
                        rotors_strings.set(list_rotors.indexOf(number_rotor), text.toUpperCase());
                    }else{
                        throw new Exception();
                    }
                }
            }

        }
        reader.close();

        logger.info("Rotors : " + rotors_strings.toString());
        if (rotors_strings.contains(""))
            throw new Exception();

        return rotors_strings;
    }


    // la funzione Encrypt prende in ingresso i rotori, la plugboard, il riflettore

    private static String encrypt(Rotore rot1, Rotore rot2,Rotore rot3, Map<Character, Character> plugboard,Map<Character, Character> reflector, String txt) {
        char c;
        String encrypted_test="";

        for (int i = 0; i < txt.length(); i++) {

            for (int j = 0; j < 26; j++) {

                for (int k = 0; k < 26; k++) {


                    //come seleziono i caratteri del testo?
                    if((k+26*j+676*i)>=txt.length())
                        return encrypted_test;

                    c = txt.charAt(k+26*j+676*i);

                    if(!Character.isWhitespace(c)) {

                        rot1.shift();
                        if( plugboard.containsKey(c)) {
                            c = plugboard.get(c);
                        }
                        rot1.get_corresponding_char(c);
                        rot2.get_corresponding_char(c);
                        rot3.get_corresponding_char(c);

                        //c = reflector.get(c);

                        //mi serve una funzione che fa l'inverso del get_corrisponding_char
                        rot3.get_corrisponding_index(c);
                        rot2.get_corrisponding_index(c);
                        rot1.get_corrisponding_index(c);

                        if( plugboard.containsKey(c)) {
                            c = plugboard.get(c);
                        }

                        //devo salvare c in una stringa

                    }
                    encrypted_test += Character.toString(c);

                }

                rot2.shift();

            }

            rot3.shift();
        }

        return encrypted_test;
    }
}
