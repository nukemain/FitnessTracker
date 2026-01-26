package pl.fitnesstracker.fitnesse;

import pl.fitnesstracker.dao.DayOfWeekDao;

public class InMemoryDayOfWeekDao extends DayOfWeekDao {
    @Override
    public void assignPlanToDay(int planId, String dayName) {
    }
    
    @Override
    public boolean isPlanScheduledForDay(int planId, String dayName) {
        return true; 
    }
    
    
}
