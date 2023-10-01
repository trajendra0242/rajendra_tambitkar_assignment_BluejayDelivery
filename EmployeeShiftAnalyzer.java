import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeShiftAnalyzer {

    public static void main(String[] args) {
        String file_path = "Assignment_Timecard.csv"; // Replace with the actual file path
        analyzeShifts(file_path);
    }

    public static void analyzeShifts(String filePath) {
        Map<String, List<Shift>> employeeShifts = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String employeeId = parts[0];
                String dateStr = parts[1];
                String startTime = parts[2];
                String endTime = parts[3];

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(dateStr);

                if (!employeeShifts.containsKey(employeeId)) {
                    employeeShifts.put(employeeId, new ArrayList<>());
                }
                employeeShifts.get(employeeId).add(new Shift(date, startTime, endTime));
            }

            for (Map.Entry<String, List<Shift>> entry : employeeShifts.entrySet()) {
                String employeeId = entry.getKey();
                List<Shift> shifts = entry.getValue();

                if (checkConsecutiveDays(shifts)) {
                    System.out.println("Employee ID: " + employeeId + " worked 7 consecutive days.");
                }

                for (int i = 0; i < shifts.size() - 1; i++) {
                    long timeDiff = timeDifference(shifts.get(i).getEndTime(), shifts.get(i + 1).getStartTime());
                    if (60 < timeDiff && timeDiff < 600) {
                        System.out.println("Employee ID: " + employeeId + " had less than 10 hours between shifts on " + shifts.get(i).getDateStr() + ".");
                    }
                }

                for (Shift shift : shifts) {
                    long shiftDuration = shift.getShiftDuration();
                    if (shiftDuration > 840) {
                        System.out.println("Employee ID: " + employeeId + " worked more than 14 hours on " + shift.getDateStr() + ".");
                    }
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkConsecutiveDays(List<Shift> shifts) {
        int consecutiveDays = 0;
        for (int i = 0; i < shifts.size() - 1; i++) {
            long dayDifference = daysBetween(shifts.get(i).getDate(), shifts.get(i + 1).getDate());
            if (dayDifference == 1) {
                consecutiveDays++;
                if (consecutiveDays == 6) { // 7 consecutive days including the current day
                    return true;
                }
            } else {
                consecutiveDays = 0;
            }
        }
        return false;
    }

    private static long timeDifference(String startTime, String endTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        try {
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);
            return (end.getTime() - start.getTime()) / (60 * 1000); // Return difference in minutes
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static long daysBetween(Date date1, Date date2) {
        long difference = date2.getTime() - date1.getTime();
        return difference / (24 * 60 * 60 * 1000);
    }

    private static class Shift {
        private Date date;
        private String startTime;
        private String endTime;

        public Shift(Date date, String startTime, String endTime) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getDateStr() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        }

        public Date getDate() {
            return date;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public long getShiftDuration() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            try {
                Date start = timeFormat.parse(startTime);
                Date end = timeFormat.parse(endTime);
                return (end.getTime() - start.getTime()) / (60 * 1000); // Return difference in minutes
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
}
