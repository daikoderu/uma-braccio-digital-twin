package client;

import api.ClockController;

public abstract class ShellCommands {

    public static void quit(CliContext context) {
        context.clockController.stop();
        try {
            context.clockControllerThread.join();
        } catch (InterruptedException ignored) { }
        context.quitting = true;
    }

    public static void dtclock(String[] args, CliContext context) {
        if (args.length != 1) {
            context.err.println("Usage: dtclock {start|stop}");
            return;
        }
        ClockController controller = context.clockController;
        switch (args[0]) {
            case "start" -> {
                if (controller.isTicking()) {
                    context.out.println("Time is already running on the Digital Twin");
                } else {
                    context.out.println("Time is running on the Digital Twin");
                    context.clockController.setTicking(true);
                }
            }
            case "stop" -> {
                if (!controller.isTicking()) {
                    context.out.println("Time is not running on the Digital Twin");
                } else {
                    context.out.println("Time passage on the Digital Twin has been stopped");
                    context.clockController.setTicking(false);
                }
            }
            default -> context.err.println("Usage: dtclock {start|stop}");
        }
    }

    public static void test(String[] args, CliContext context) {
        if (args.length != 1) {
            context.err.println("Usage: test <testname>");
            return;
        }
        TestModule.run(args[0], context);
    }

}
