package pl.fitnesstracker.fitnesse;

import pl.fitnesstracker.dao.WorkoutPlanDao;
import pl.fitnesstracker.model.WorkoutPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryWorkoutPlanDao extends WorkoutPlanDao {
    private final List<WorkoutPlan> plans = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1000);

    @Override
    public Integer createPlan(WorkoutPlan plan) {
        plan.setId(idGenerator.incrementAndGet());
        if (plan.getActive() == null) {
            plan.setActive(true);
        }
        plans.add(plan);
        return plan.getId();
    }

    @Override
    public List<WorkoutPlan> getPlansByUser(Integer userId) {
        return plans.stream()
                .filter(p -> p.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
