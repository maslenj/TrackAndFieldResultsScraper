import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
//
//        Scanner f = new Scanner(new FileReader(new File("BrokenData")));
//        PrintWriter out = new PrintWriter(new File("FixedData"));
//
//        fixResults(f, out);
//
//        out.close();

        String fileToWriteTo = "DataCollection1";
        PrintWriter out = new PrintWriter(new File(fileToWriteTo));
        double previousTime = System.currentTimeMillis();
        final int printFrequency = 5;
        double dataCollectionStart = System.currentTimeMillis();
        Scraper myScraper = new Scraper(out);

        int numUsers = 0;
        for(String schoolURL: getUserURLsFromSchools()){
            for(String userURl: getUserURLs(new SimpleURL(schoolURL).readText())){
                System.out.println("working on: " + userURl);
                numUsers++;
                myScraper.printCommaSeparatedValues(new SimpleURL(userURl).readText());
                if(numUsers % printFrequency == 0){
                    double currentTimeSpent = (System.currentTimeMillis() - previousTime)/1000.0;
                    previousTime = System.currentTimeMillis();
                    System.out.println("People: " + numUsers + " || Rate of collection: " + currentTimeSpent/printFrequency + " seconds per user");
                }
            }
            if(numUsers > 15000){
                break;
            }
        }

        out.println();
        out.println("Number of meets found: " + myScraper.meetLocationHashMap.size());
        out.println("Time Collecting Data: " + (System.currentTimeMillis() - dataCollectionStart)/1000 + " seconds");
        out.close();

    }

    private static ArrayList<String> getUserURLs(String schoolPage){
        int placeToStart = 0;
        int placeToEnd = 0;
        for(int i = 0; i < schoolPage.length() - 10; i++){
            if(schoolPage.startsWith("\"athletes\"", i)){
                placeToStart = i;
                break;
            }
        }
        for(int i = 0; i < schoolPage.length() - 9; i++){
            if(schoolPage.startsWith("\"coaches\"", i)){
                placeToEnd = i-4;
                break;
            }
        }
        ArrayList<String> users = new ArrayList<>();
        for(int i = placeToStart; i < placeToEnd; i++){
            if(schoolPage.startsWith("\"ID\"", i)){
                int IDStart = i + 5;
                int IDEnd = IDStart;
                while (schoolPage.charAt(IDEnd) != ','){
                    IDEnd++;
                }
                users.add("https://www.athletic.net/TrackAndField/Athlete.aspx?AID=" + schoolPage.substring(IDStart, IDEnd) + "#!/L0");
            }
        }
        return users;
    }

    private static ArrayList<String> getUserURLsFromSchools() {
        ArrayList<String> urls = new ArrayList<>();
        String text = new SimpleURL("https://www.athletic.net/TrackAndField/Oregon/").readText();
        String schoolURLs = text.substring(80000, text.length()-10);
        for(int i = 0; i < schoolURLs.length()-6; i++){
            if(schoolURLs.startsWith("a href", i)) {
                int urlStart = i +8;
                int urlEnd = urlStart;
                while(schoolURLs.charAt(urlEnd) != '"') {
                    urlEnd++;
                }
                if(schoolURLs.substring(urlStart+3, urlEnd).contains("School")){
                    urls.add("https://www.athletic.net/TrackAndField/" + schoolURLs.substring(urlStart+3, urlEnd));
                }
            }
        }
        return urls;
    }

    private static void fixResults(Scanner f, PrintWriter out){
        int numLines = 0;

        while(f.hasNext()){
            if(numLines % 1000 == 0){
                System.out.println(numLines);
            }
            numLines ++;
            String line = f.nextLine();
            String editedLine = line;
            if(line.contains("<") && line.contains(">")){
                editedLine = line.substring(0, line.indexOf('<')) + line.substring(line.indexOf('>'), line.length()-1);
            }
            out.println(editedLine);
        }
    }

}
