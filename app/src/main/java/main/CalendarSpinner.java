package main;

import java.util.Objects;

public class CalendarSpinner {

    public long id;
    public String name;

    public CalendarSpinner(long _id, String _name) {
        id = _id;
        name = _name;
    }

    public String toString() {
        return name + " (" + Long.toString(id) + ")" ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarSpinner that = (CalendarSpinner) o;
        return id == that.id &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
