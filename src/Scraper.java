import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Scraper {
    public HashMap<Integer, Meet> meetLocationHashMap;
    private final PrintWriter out;


    Scraper(PrintWriter out) {
        meetLocationHashMap = new HashMap<>();
        this.out = out;
    }

    public void printCommaSeparatedValues(String htmlData){

        // Get Name //

        String firstName = "";
        String lastName = "";
        String gender = "";

        for(int i = 0; i < htmlData.length()-9; i++) {
            if (htmlData.startsWith("\"first\"", i)) {
                int i2 = i + 9;
                while (htmlData.charAt(i2) != '"') {
                    i2++;
                }
                firstName = htmlData.substring(i + 9, i2);
            }
            if (htmlData.startsWith("\"last\"", i)) {
                int i2 = i + 8;
                while (htmlData.charAt(i2) != '"') {
                    i2++;
                }
                lastName = htmlData.substring(i + 8, i2);
            }
            if(htmlData.startsWith("gender", i)){
                gender = htmlData.substring(i+7, i+8);
            }
        }


        out.println("user|" + firstName + "|" + lastName + "|" + gender);

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //Get seasons and events done in each season

        for(int i = 0; i < htmlData.length()-9; i++) {
            if(htmlData.startsWith("!season_", i)) {

                // Get Season //

                int seasonStart = i;
                while (!htmlData.startsWith(">2", seasonStart)) {
                    seasonStart++;
                }
                int seasonEnd = seasonStart + 1;

                while (htmlData.charAt(seasonEnd) != '<') {
                    seasonEnd++;
                }
                String season = htmlData.substring(seasonStart + 1, seasonEnd).trim();
                season = season.substring(0, season.length()-1);

                // Get School //

                int schoolStart = seasonEnd;
                while (!htmlData.startsWith("hidden-lg-up", schoolStart)) {
                    schoolStart++;
                }
                while (!htmlData.startsWith("b>", schoolStart)) {
                    schoolStart++;
                }
                schoolStart += 2;
                int schoolEnd = schoolStart;
                while (htmlData.charAt(schoolEnd) != '<') {
                    schoolEnd++;
                }
                String school = htmlData.substring(schoolStart, schoolEnd);

                // Get Grade //

                int gradeStart = schoolEnd + 58;
                int gradeEnd = gradeStart;
                while (htmlData.charAt(gradeEnd) != '<') {
                    gradeEnd++;
                }
                String grade = htmlData.substring(gradeStart, gradeEnd);

                // Print Results //

                out.println("season|" + season + "|" + school + "|" + grade);

                /////////////////////////////////////////////////////////////////////////////////////////////////////////////

                // Get Event One Name //

                int firstEventStart = gradeEnd;
                while (!htmlData.startsWith("uib-collapse", firstEventStart)) {
                    firstEventStart++;
                }
                while (!htmlData.startsWith("5>", firstEventStart)) {
                    firstEventStart++;
                }
                firstEventStart += 2;
                int firstEventEnd = firstEventStart;
                while (!htmlData.startsWith("</", firstEventEnd)) {
                    firstEventEnd++;
                }
                String event1 = htmlData.substring(firstEventStart, firstEventEnd);
                if(event1.contains("Relay") || event1.contains("v>")){
                    break;
                }

                // Print Results //

                out.println("event type|" + event1);

                int eventPlaceToStart =  printEventInfo(htmlData, firstEventEnd);

                // Get Other Events //

                while(htmlData.startsWith("</tbody>", eventPlaceToStart)){

                    // Get New Event Name //

                    int eventNameStart = eventPlaceToStart + 20;
                    int eventNameEnd = eventNameStart;
                    while(htmlData.charAt(eventNameEnd) != '<'){
                        eventNameEnd++;
                    }
                    String eventName = htmlData.substring(eventNameStart, eventNameEnd);
                    if(eventName.contains("Relay") || eventName.contains("SMR") || eventName.contains("DMR") || eventName.contains("Shuttle") || eventName.contains("v>") || eventName.contains("4x")){
                        break;
                    }

                    // Print Results //

                    out.println("event type|" + eventName);

                    /////////////////////////////////////////////////////////////////////////////////////////////////////////

                    int eventInfoPlaceToStart = eventNameEnd;

                    eventPlaceToStart = printEventInfo(htmlData, eventInfoPlaceToStart);

                }

                //check to see if has event

            }

        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    private int printEventInfo(String htmlData, int eventInfoPlaceToStart){
        while(!htmlData.startsWith("</tbody>", eventInfoPlaceToStart)){

            // Get Event Instance Place //

            int placeStart = eventInfoPlaceToStart;
            while (!htmlData.startsWith("far fa-pencil", placeStart)) {
                placeStart++;
            }
            while (!htmlData.startsWith("\">", placeStart)) {
                placeStart++;
            }
            placeStart += 13;
            int placeEnd = placeStart;
            while (!htmlData.startsWith("</", placeEnd)) {
                placeEnd++;
            }
            String place = htmlData.substring(placeStart, placeEnd-1);

            // Get Event Instance Score //

            int scoreStart = placeEnd;
            while (!htmlData.startsWith("/result/", scoreStart)) {
                scoreStart++;
            }
            while (!htmlData.startsWith("\">", scoreStart)) {
                scoreStart++;
            }
            scoreStart += 2;
            int scoreEnd = scoreStart;
            while (htmlData.charAt(scoreEnd) != '<') {
                scoreEnd++;
            }
            String score = htmlData.substring(scoreStart, scoreEnd);

            // Get Event Instance One Date //

            int dateStart = scoreEnd;
            while (!htmlData.startsWith("text-nowrap", dateStart)) {
                dateStart++;
            }
            dateStart += 34;
            int dateEnd = dateStart;
            while (htmlData.charAt(dateEnd) != '<') {
                dateEnd++;
            }
            String date = htmlData.substring(dateStart, dateEnd);

            // Get Meet Page URL //

            int meetURLStart = dateEnd;
            while(!htmlData.startsWith("href", meetURLStart)){
                meetURLStart++;
            }
            meetURLStart += 6;
            int meetURLEnd = meetURLStart;
            while (htmlData.charAt(meetURLEnd) != '"') {
                meetURLEnd++;
            }
            String partialMeetURL = htmlData.substring(meetURLStart, meetURLEnd);
            String fullMeetURL = "https://www.athletic.net/TrackAndField/" + partialMeetURL + "/results";

            // Get Meet ID //

            int meetIDStart = meetURLStart + 5;
            int meetIDEnd = meetIDStart;
            while (htmlData.charAt(meetIDEnd) != '#') {
                meetIDEnd++;
            }
            String meetID = htmlData.substring(meetIDStart, meetIDEnd);

            String latitude;
            String longitude;

            if(meetLocationHashMap.containsKey(Integer.parseInt(meetID))){
                latitude = meetLocationHashMap.get(Integer.parseInt(meetID)).latitude;
                longitude = meetLocationHashMap.get(Integer.parseInt(meetID)).longitude;
            } else {

                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                SimpleURL meetURL = new SimpleURL(fullMeetURL);
                String meetURLData = meetURL.readText();

                // Get Meet Location //

                int latStart = 0;
                while (!meetURLData.startsWith("\"Lat\"", latStart)) {
                    latStart++;
                }
                latStart += 6;
                int latEnd = latStart;
                while (meetURLData.charAt(latEnd) != ',') {
                    latEnd++;
                }
                int longStart = 0;
                while (!meetURLData.startsWith("\"Long\"", longStart)) {
                    longStart++;
                }
                longStart += 7;
                int longEnd = longStart;
                while (meetURLData.charAt(longEnd) != ',') {
                    longEnd++;
                }
                latitude = meetURLData.substring(latStart, latEnd);
                longitude = meetURLData.substring(longStart, longEnd);

                meetLocationHashMap.put(Integer.parseInt(meetID), new Meet(latitude, longitude));
            }

            // Get Meet Name //

            int meetNameStart = meetURLEnd + 2;
            int meetNameEnd = meetNameStart;
            while (htmlData.charAt(meetNameEnd) != '<') {
                meetNameEnd++;
            }
            String meetName = htmlData.substring(meetNameStart, meetNameEnd);

            // Get Event Division //

            int divisionStart = meetNameEnd + 55;

            if(htmlData.substring(meetNameEnd+53, meetNameEnd+56).equals("\"><")){
                while(!htmlData.substring(divisionStart, divisionStart+2).equals("/a")){
                    divisionStart++;
                }
                divisionStart += 3;
            }

            int divisionEnd = divisionStart;
            while (htmlData.charAt(divisionEnd) != '<') {
                divisionEnd++;
            }
            String division = htmlData.substring(divisionStart, divisionEnd);

            // Print Results //

            out.println("result|" + place + "|" + score + "|" + date + "|" + meetName + "|" + meetID + "|" + latitude + "|" + longitude + "|" + division);

            // Prepare For Reset //

            eventInfoPlaceToStart = divisionEnd + 10;

        }

        return eventInfoPlaceToStart;
    }

    static class Meet {
        String latitude;
        String longitude;

        Meet(String latitude, String longitude){
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
