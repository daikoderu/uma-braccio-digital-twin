package client;

import api.ClockController;
import api.DTDataLake;

import javax.xml.stream.events.DTD;

public abstract class ShellCommands {

    public static void quit(CliContext context) {
        context.clockController.stop();
        try {
            context.clockControllerThread.join();
        } catch (InterruptedException ignored) { }
        context.quitting = true;
    }

    public static void dtclock(String[] args, CliContext context) {
        String usage = "Usage: dtclock {start|stop|tick <amount>}";
        if (args.length < 1) {
            context.out.println(usage);
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
                    context.out.println(usage);
                } else {
                    context.out.println("Time passage on the Digital Twin has been stopped");
                    context.clockController.setTicking(false);
                }
            }
            case "tick" -> {
                if (args.length == 1) {
                    context.out.println(usage);
                    context.out.println("Missing argument <amount>.");
                } else {
                    try (DTDataLake dl = context.connection.getResource()) {
                        int amount = Integer.parseInt(args[1]);
                        if (amount < 0) {
                            context.out.println(usage);
                            context.out.println("<amount> must be non-negative.");
                        } else {
                            dl.advanceDTTime(amount);
                        }
                    } catch (NumberFormatException ex) {
                        context.out.println(usage);
                        context.out.println("<amount> must be a valid integer.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            default -> context.out.println(usage);
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
