package client;

import api.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class TestModule {

    private static Map<String, Consumer<CliContext>> testModules = null;
    private static String testList;

    public static void run(String testName, CliContext context) {
        if (testModules == null) {
            loadTests();
        }
        if (testModules.containsKey(testName)) {
            testModules.get(testName).accept(context);
        } else {
            context.err.println("Test not found: " + testName);
            context.err.println("Available tests: " + testList);
        }
    }

    private static void simpleMoves(CliContext context) {
        try (DTDataLake dl = context.connection.getResource()) {
            int dtStart = dl.getDTTime();
            int ptStart = dl.getPTTime();
            DLTwin twin = dl.forTwin(context.twinId);

            context.out.println("Executing test case...");

            movetoAndWait(dl, twin, new Position(90, 90, 180, 172, 90, 10));
            movetoAndWait(dl, twin, new Position(90, 55, 170, 86, 90, 10));
            movetoAndWait(dl, twin, new Position(0, 90, 90, 90, 90, 43));
            movetoAndWait(dl, twin, new Position(90, 90, 90, 90, 90, 73));

            int dtEnd = dl.getDTTime();
            int ptEnd = dl.getPTTime();

            context.out.println("Collecting and saving snapshots...");

            List<OutputSnapshot> dtSnapshots = twin.getOutputSnapshotsInRange(TwinTarget.DIGITAL, dtStart, dtEnd);
            List<OutputSnapshot> ptSnapshots = twin.getOutputSnapshotsInRange(TwinTarget.PHYSICAL, ptStart, ptEnd);
            String dtFilename = context.twinId + "_" + context.executionId + "_dt.csv";
            String ptFilename = context.twinId + "_" + context.executionId + "_pt.csv";
            saveSnapshotsToCSV(dtSnapshots, dtFilename, dtStart);
            saveSnapshotsToCSV(ptSnapshots, ptFilename, ptStart);

            context.out.println("Snapshots have been saved to " + dtFilename + " and " + ptFilename);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void loadTests() {
        testModules = new HashMap<>();
        testModules.put("simpleMoves", TestModule::simpleMoves);

        StringJoiner joiner = new StringJoiner(", ", "", "");
        for (String testName : testModules.keySet()) {
            joiner.add(testName);
        }
        testList = joiner.toString();
    }

    private static void movetoAndWait(DTDataLake dl, DLTwin twin, Position position) throws InterruptedException {
        twin.putCommand(TwinTarget.BOTH, "moveto", position.toArguments());

        // Wait until command is received
        Thread.sleep(5000);

        // Advance time in the DT to execute the command and its movement
        dl.advanceDTTime(2000);

        // Wait until we get the result
        Thread.sleep(5000);
    }

    private static void saveSnapshotsToCSV(List<OutputSnapshot> snapshots, String filename, int startTime) {
        try (PrintWriter file = new PrintWriter(new FileWriter(filename), true)) {
            file.println("time,servo1,servo2,servo3,servo4,servo5,servo6");
            for (OutputSnapshot snapshot : snapshots) {
                Position position = snapshot.getCurrentAngles();
                StringBuilder builder = new StringBuilder();
                builder.append(snapshot.getTimestamp() - startTime);
                for (int i = 0; i < 6; i++) {
                    builder.append(",").append(position.get(i));
                }
                file.println(builder);
            }
        } catch (IOException ex) {
            System.err.println("Could not write.");
        }
    }

}
