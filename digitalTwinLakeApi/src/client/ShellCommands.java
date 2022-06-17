package client;

import api.DTDataLake;

public abstract class ShellCommands {

    public static void quit(CliContext ctx) {
        ctx.stopClockController();
        ctx.setQuitting(true);
    }

    public static void dtclock(String[] args, CliContext ctx) {
        String usage = "Usage: dtclock {start|stop|tick <amount>}";
        if (args.length < 1) {
            ctx.error(usage);
            return;
        }
        switch (args[0]) {
            case "start" -> {
                ctx.print("The Digital Twin clock is running.");
                ctx.setClockControllerTicking(true);
            }
            case "stop" -> {
                ctx.print("The Digital Twin clock has been stopped.");
                ctx.setClockControllerTicking(false);
            }
            case "tick" -> {
                if (args.length == 1) {
                    ctx.error(usage);
                    ctx.error("Missing argument <amount>.");
                } else {
                    try (DTDataLake dl = ctx.getDataLake()) {
                        int amount = Integer.parseInt(args[1]);
                        if (amount < 0) {
                            ctx.error(usage);
                            ctx.error("<amount> must be non-negative.");
                        } else {
                            dl.advanceDTTime(amount);
                        }
                    } catch (NumberFormatException ex) {
                        ctx.error(usage);
                        ctx.error("<amount> must be a valid integer.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            default -> ctx.error(usage);
        }
    }

    public static void test(String[] args, CliContext ctx) {
        if (args.length != 1) {
            ctx.error("Usage: test <testname>");
            return;
        }
        TestModule.run(args[0], ctx);
    }

}
