package client;

import api.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class AutomatedTests {

    private static Map<String, Consumer<CliContext>> tests = null;
    private static String testList;
    private static final Position[] testMoveSequence = {
        new Position(90, 90, 180, 172, 90, 10),
        new Position(90, 55, 170, 86, 90, 10),
        new Position(0, 90, 90, 90, 90, 43),
        new Position(90, 90, 90, 90, 90, 73)
    };

    public static void run(String testName, CliContext context) {
        if (tests == null) {
            loadTests();
        }
        if (tests.containsKey(testName)) {
            tests.get(testName).accept(context);
        } else {
            context.error("Test not found: " + testName);
            context.error("Available tests: " + testList);
        }
    }

    private static void simpleMoves1(CliContext ctx) {
        movesToCSV(testMoveSequence, ctx, 4000, 4000);
    }

    private static void simpleMoves2(CliContext ctx) {
        movesToCSV(testMoveSequence, ctx, 2000, 2000);
    }

    private static void loadTests() {
        tests = new HashMap<>();
        tests.put("simpleMoves1", AutomatedTests::simpleMoves1);
        tests.put("simpleMoves2", AutomatedTests::simpleMoves2);

        StringJoiner joiner = new StringJoiner(", ", "", "");
        for (String testName : tests.keySet()) {
            joiner.add(testName);
        }
        testList = joiner.toString();
    }

    private static void movetoAndWait(DTDataLake dl, CliContext ctx, Position position, int ptWait, int dtWait)
            throws InterruptedException {
        // Send the command
        DLTwin twin = dl.forTwin(ctx.getTwinId());
        int commandId = twin.putCommand(TwinTarget.BOTH, "moveto", position.toArguments());

        // Wait until command is received and result is returned by both twins
        Thread.sleep(ptWait);
        dl.advanceDTTime(dtWait);

        Command command = twin.getCommand(TwinTarget.DIGITAL, commandId);
        ctx.print("DT" + command);
        command = twin.getCommand(TwinTarget.PHYSICAL, commandId);
        ctx.print("PT" + command);
    }

    private static void movesToCSV(Position[] positions, CliContext ctx, int ptWait, int dtWait) {
        try (DTDataLake dl = ctx.getDataLake()) {
            int dtStart = dl.getDTTime();
            int ptStart = dl.getPTTime();
            DLTwin twin = dl.forTwin(ctx.getTwinId());

            ctx.print("Executing test case...");
            for (Position p : positions) {
                movetoAndWait(dl, ctx, p, ptWait, dtWait);
            }

            int dtEnd = dl.getDTTime();
            int ptEnd = dl.getPTTime();

            ctx.print("Test case finished. Awaiting snapshot synchronization...");
            Thread.sleep(5000);

            ctx.print("Digital Twin timestamps: from " + dtStart + " to " + dtEnd);
            ctx.print("Physical Twin timestamps: from " + ptStart + " to " + ptEnd);

            ctx.print("Collecting snapshots...");
            List<OutputSnapshot> dtSnapshots = twin.getOutputSnapshotsInRange(TwinTarget.DIGITAL, dtStart, dtEnd);
            List<OutputSnapshot> ptSnapshots = twin.getOutputSnapshotsInRange(TwinTarget.PHYSICAL, ptStart, ptEnd);

            try {
                if (!dtSnapshots.isEmpty()) {
                    ctx.print("Saving Digital Twin snapshots...");
                    String dtFilename = ctx.getTwinId() + "_" + ctx.getExecutionId() + "_dt.csv";
                    saveSnapshotsToCSV(dtSnapshots, dtFilename, dtStart);
                    ctx.print("Digital Twin snapshots have been saved to " + dtFilename);
                }
                if (!ptSnapshots.isEmpty()) {
                    ctx.print("Saving Physical Twin snapshots...");
                    String ptFilename = ctx.getTwinId() + "_" + ctx.getExecutionId() + "_pt.csv";
                    saveSnapshotsToCSV(ptSnapshots, ptFilename, ptStart);
                    ctx.print("Physical Twin snapshots have been saved to " + ptFilename);
                }
            } catch (IOException ex) {
                ctx.error("IO Error: " + ex.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void saveSnapshotsToCSV(List<OutputSnapshot> snapshots, String filename, int startTime)
            throws IOException {
        PrintWriter file = new PrintWriter(filename);
        file.println("time,base,shoulder,elbow,wrist_ver,wrist_rot,gripper");
        for (OutputSnapshot snapshot : snapshots) {
            Position position = snapshot.getCurrentAngles();
            StringBuilder builder = new StringBuilder();
            builder.append(snapshot.getTimestamp() - startTime);
            for (int i = 0; i < 6; i++) {
                builder.append(",").append(position.get(i));
            }
            file.println(builder);
        }
        file.close();
    }

}
