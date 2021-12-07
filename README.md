# ScheduleGenerator

This program generates schedules from a list of different activities.
These activities will be assigned to different teams, but each activity
can only be assigned to one team.

It starts by collecting every activity and its length from the given
txt file, to then save it into an ArrayList variable. After that, it
will create a Scheduler object with these Activities  **and** the 
fixed Appointments, which in this case will be indicated in the code.

The Scheduler will then generate a schedule by continuously adding activities
to it (always respecting the fixed appointments timings) until the last
fixed appointment is added to it (since, in this case, the schedule ends after
the last fixed appointment occurs).

After a schedule is generated, the activities list is updated so the
already done activities are removed from it. Now another schedule is generated
because the number of teams in this case is 2, and when it ends, both 
schedules will be displayed as an output in the terminal.

*About the combination system: it is meant to be good for different, larger 
activities lists cases, although it may require few more checks. 
I could have gone for a simpler way since there is only 20 activities, but 
I wanted to make it "easy" to upgrade.*

## Usage

```java
/** 
 * In this part of the code, you should type the directory of the txt.file 
 * where the activities are stored. They should always follow the format 
 * of the firstly given txt file.   
 */
    try {
        scanner = new Scanner(new File("../activities.txt"));
    } catch (FileNotFoundException e) {
        System.out.println("File Doesn't Exist");
        return;
    }
```
```java
/**
 * In this case, the fixed appointments and number of teams is indicated
 * inside the code.
 */
//Declarations & Initializations
        final Fixed LUNCH = new Fixed("Lunch Break",60, 0, 180);
        final Fixed PRESENTATION = new Fixed("Staff Motivation Presentation", 0, 420, 480);
        ArrayList<Fixed> fixed = new ArrayList<Fixed>(
            Arrays.asList(LUNCH, PRESENTATION)
        );
        final int NUM_TEAMS = 2;
```