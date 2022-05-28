package client;

import api.DLTwin;
import api.DTDataLake;
import api.OutputSnapshot;
import api.TwinTarget;

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
            int ptStart = dl.getPTTime();
            int dtStart = dl.getDTTime();
            DLTwin twin = dl.forTwin(context.twinId);

            context.out.println("Executing test case...");

            Thread.sleep(1000);
            dl.advanceDTTime(1000);

            twin.putCommand(TwinTarget.BOTH, "moveto", "90", "90", "180", "172", "90", "10");
            Thread.sleep(500);
            dl.advanceDTTime(5000);

            twin.putCommand(TwinTarget.BOTH, "moveto", "90", "55", "170", "86", "90", "10");
            Thread.sleep(1000);
            dl.advanceDTTime(1000);

            twin.putCommand(TwinTarget.BOTH, "moveto", "0", "90", "90", "90", "90", "43");
            Thread.sleep(1000);
            dl.advanceDTTime(1000);

            twin.putCommand(TwinTarget.BOTH, "moveto", "90", "90", "90", "90", "90", "73");
            Thread.sleep(1000);
            dl.advanceDTTime(1000);

            int ptEnd = dl.getPTTime();
            int dtEnd = dl.getDTTime();

            context.out.println("Waiting output snapshots...");
            Thread.sleep(5000);

            List<OutputSnapshot> ptSnapshots = twin.getOutputSnapshotsInRange(TwinTarget.PHYSICAL, ptStart, ptEnd);
            List<OutputSnapshot> dtSnapshots = twin.getOutputSnapshotsInRange(TwinTarget.DIGITAL, dtStart, dtEnd);

            context.out.println("PHYSICAL TWIN:");
            for (OutputSnapshot snapshot : ptSnapshots) {
                context.out.println(snapshot);
            }

            context.out.println("\nDIGITAL TWIN:" + dtStart + "..." + dtEnd);
            for (OutputSnapshot snapshot : dtSnapshots) {
                context.out.println(snapshot);
            }


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

    private void wait(int millis, DTDataLake dl) throws InterruptedException {
        dl.advanceDTTime(millis);
        Thread.sleep(millis);
    }

}
