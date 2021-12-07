package digitawayday;

import digitawayday.domain.Activity;
import digitawayday.domain.Fixed;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Application {

    public void execute() {

        //Declarations & Initializations
        final Fixed LUNCH = new Fixed("Lunch Break",60, 0, 180);
        final Fixed PRESENTATION = new Fixed("Staff Motivation Presentation", 0, 420, 480);
        ArrayList<Fixed> fixed = new ArrayList<Fixed>(
                Arrays.asList(LUNCH, PRESENTATION)
        );
        final int NUM_TEAMS = 2; //Default number of teams set to 2
        int currentHour = 540; //Set to 9am by default

        Scanner scanner = null;
        String line = "";
        String name = "";
        String length = "";
        ArrayList<Activity> activities = new ArrayList<>();

        //Attempts to create scanner for file
        try {
            scanner = new Scanner(new File("activities.txt directory path"));
        } catch (FileNotFoundException e) {
            System.out.println("File Doesn't Exist");
            return;
        }

        //Begins reading .txt file, saving each line as an Activity
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            name = StringUtils.substringBeforeLast(line, " ");//Subtract name from each line
            length = StringUtils.substringAfterLast(line, " ");//Subtract length from each line
            if(length.equals("sprint")) {
                length = "15"; //If the activity length is set as "sprint", we make it "15" minutes
            } else {
                length = length.replaceAll("\\D+",""); //Remove every non-digit char (45min => 45)
            }

            activities.add(new Activity(name, Integer.parseInt(length)));
        }

        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");

        for (int i = 0; i < NUM_TEAMS; i++) {
            Scheduler scheduler = new Scheduler(activities, fixed);
            scheduler.startGeneration();
            System.out.println("Team "+(i+1)+":");
            String displayLength;

            for (Activity activity : scheduler.getSchedule()) {
                switch (activity.getLength()) {
                    case 15:
                        displayLength = "sprint";
                        break;
                    case 0:
                        displayLength  = "";
                        break;
                    default:
                        displayLength = activity.getLength() + "min";
                        break;
                }

                System.out.println(convertToHours(currentHour)+" : " + activity.getName()+" "+displayLength);
                currentHour += activity.getLength();
            }

            currentHour = 540; //Reset to 9am
            activities = new ArrayList<>(scheduler.activities); //Update the activities list for the next team
            System.out.println(" ");
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    /**
     * Converts given minutes into hh:mm am/pm hour format.
     * @param minutes The minutes that we want to turn into hh:mm am/pm hour format.
     * @return The hour calculated from the minutes given.
     */
    public String convertToHours(int minutes) {
        int hours = minutes/60;
        int mins = minutes%60;
        String txtHours = Integer.toString(hours);
        String txtMins = Integer.toString(mins);
        String aa;

        if (hours > 12){
            hours -=12;
            aa = "pm";
        } else{
            aa = "am";
        }
        if (hours < 10)
            txtHours = "0"+hours;
        if (mins < 10)
            txtMins = "0"+mins;

        return txtHours+":"+txtMins+" "+aa;
    }


    public static class Scheduler {

        //Declarations & Initializations
        private int[] activitiesMap;
        private int[] fixedMap;
        private ArrayList<Activity> activities = new ArrayList<>();
        private ArrayList<Fixed> fixedAppointments = new ArrayList<>();
        private int sum = 0;
        private boolean hasNext = true;
        private ArrayList<Activity> schedule = new ArrayList<>();
        private int minsAfterFixed = 0;

        public ArrayList<Activity> getSchedule() {
            return this.schedule;
        }

        public Scheduler(ArrayList<Activity> activities, ArrayList<Fixed> fixed) {

            this.activities = new ArrayList<>(activities);
            this.fixedAppointments = new ArrayList<>(fixed);

            this.fixedMap = new int[this.fixedAppointments.size()];
            this.activitiesMap = new int[this.activities.size()];

            this.hasNext = this.activitiesMap.length > 0 && this.fixedMap.length > 0;
        }

        /**
         * Checks if there are activities left to place. If not, finishes the generation.
         */
        private void nextActivity() {
            if (this.activitiesMap.length > 0){
                this.activitiesMap[0]++; //We keep adding ones to the mapper
                this.activitiesMapper();
                this.checkFixed();
                if (this.hasNext){
                    this.tryPlaceFixed();
                    this.sumLengths();
                    this.tryPlaceFixed();
                    this.sumLengths();
                }
            } else {
                this.hasNext = false;
            }
        }

        public void startGeneration() {
            while (this.hasNext) {
                this.nextActivity();
            }
        }

        /**
         * Creates combinations of activities marking them with "1" in the activitiesMap arrayList.
         */
        public void activitiesMapper() {
            for (int i = 1; i < this.activitiesMap.length; ++i) {
                if (this.activitiesMap[i - 1] > 1) {
                    this.activitiesMap[i - 1] = 0;
                    this.activitiesMap[i] += 1;
                }
            }
            if (this.activitiesMap[this.activitiesMap.length - 1] > 1)
                this.activitiesMap[this.activitiesMap.length - 1] = 0;
        }


        /**
         * Checks if there's fixed appointments left in the list.
         */
        private void checkFixed() {
            int count = fixedAppointments.size();
            for (int fixedMap : this.fixedMap) { //Checks if every fixed has been marked with a "1"
                if (fixedMap == 1)
                    count--;
            }
            if (count == 0) { //If count gets to 0, there's no fixed left so scheduler should end
                this.hasNext = false;
            }
        }

        /**
         * Checks if, depending on the currently marked activities, a fixed appointment can be placed in the schedule.
         * If so, it will mark the appointment in the map.
         */
        private void tryPlaceFixed() {

            int startTime = 0;
            int endTime = 0;
            int length = 0;

            for (int i = 0; i < fixedAppointments.size(); i++){
                startTime = fixedAppointments.get(i).getStartTime();
                endTime = fixedAppointments.get(i).getEndTime();
                length = fixedAppointments.get(i).getLength();

                if (startTime == 0) { //If it has no startTime, it still has to have an endTime to be fixed
                    if (this.sum < endTime) {
                        if(this.sum + length >= endTime && fixedMap[i] == 0) {
                            this.fixedMap[i] = 1;
                            this.sumLengths();
                            this.addToSchedule();
                        } else {
                            this.fixedMap[i] = 0;
                        }
                    }
                } else {
                    if (this.sum >= startTime) {
                        //If the startTime is passed, it will try to place more activities without passing the endTime
                        for (int j = 0; j < this.activities.size(); j++){
                            if (this.sum + this.activities.get(j).getLength() <= endTime && this.activitiesMap[j] == 0) {
                                this.sum += this.activities.get(j).getLength();
                                this.activitiesMap[j] = 1;
                            }

                        }
                        //When no more activities can be placed, it will place the last fixed appointment
                        if (allPicked(this.activitiesMap) || this.sum <= endTime) {
                            this.fixedMap[i] = 1;
                            this.sumLengths();
                            this.addToSchedule();
                        }
                    }
                }
            }

        }

        /**
         * Checks if a map indicates that every activity was marked with a "1"
         * @param map The map to be checked
         * @return True if every activity in map has a "1"
         */
        boolean allPicked(int[] map) {
            int count = 0;
            for (int num : map) {
                if (num == 1)
                    count++;
            }
            if (count == map.length){
                return true;
            }
            return false;
        }

        /**
         * Calculates the sum of lengths of the activities/fixed appointments marked in the maps and passes the value
         * to the attribute sum.
         */
        private void sumLengths() {
            this.sum = this.minsAfterFixed;
            for (int i = 0; i < this.activitiesMap.length; i++) {
                this.sum += this.activitiesMap[i] * this.activities.get(i).getLength(); //If length is marked with a "1" in map, it's added
            }
            for (int j = 0; j <this.fixedMap.length; j++) {
                this.sum += this.fixedMap[j] * this.fixedAppointments.get(j).getLength();
            }
        }

        /**
         * Takes every marked activity/fixed appointment currently marked with a "1", adds it to the Schedule and removes
         * it from the list. It also removes its position in the map.
         */
        public void addToSchedule() {
            //Update minsAfterFixed to match the currently summed minutes.
            this.minsAfterFixed = this.sum;
            for (int i = 0; i < this.activitiesMap.length; i++) {
                if (this.activitiesMap[i] == 1){
                    this.schedule.add(this.activities.get(i));
                    this.activities.remove(i);
                    this.activitiesMap = ArrayUtils.removeElement(this.activitiesMap, this.activitiesMap[i]);
                    i--;
                }
            }
            for (int j = 0; j <this.fixedMap.length; j++) {
                if (this.fixedMap[j] == 1) {
                    this.schedule.add(this.fixedAppointments.get(j));
                    this.fixedAppointments.remove(j);
                    this.fixedMap = ArrayUtils.removeElement(this.fixedMap, this.fixedMap[j]);
                    j--;
                }
            }
        }

    }
}
