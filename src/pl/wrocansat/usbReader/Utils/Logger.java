package pl.wrocansat.usbReader.Utils;

public class Logger {

    public static void sendError(String error) {
        System.out.println(ConsoleColors.RED_BOLD + "Error: " + ConsoleColors.RED + error + ConsoleColors.RESET);
    }

    public static void sendInfo(String info) {
        System.out.println(ConsoleColors.GREEN_BOLD + "Info: " + ConsoleColors.GREEN + info + ConsoleColors.RESET);
    }

    public static void sendLog(String log) {
        System.out.println(ConsoleColors.YELLOW_BOLD + "Log: " + ConsoleColors.YELLOW + log + ConsoleColors.RESET);
    }
}
