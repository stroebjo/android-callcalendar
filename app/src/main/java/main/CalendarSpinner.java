package main;

public class CalendarSpinner {

    public long id;
    public String name;

    public CalendarSpinner(long _id, String _name) {
        id = _id;
        name = _name;
    }

    public String toString() {
        return name;
    }
}
