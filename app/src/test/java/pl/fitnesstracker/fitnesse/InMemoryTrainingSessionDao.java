package pl.fitnesstracker.fitnesse;

import pl.fitnesstracker.dao.TrainingSessionDao;
import pl.fitnesstracker.model.Note;
import pl.fitnesstracker.model.SessionRecord;
import pl.fitnesstracker.model.TrainingSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryTrainingSessionDao extends TrainingSessionDao {
    private final List<TrainingSession> sessions = new ArrayList<>();
    private final List<SessionRecord> records = new ArrayList<>();
    private final List<Note> notes = new ArrayList<>();
    private final AtomicInteger sessionIdGen = new AtomicInteger(2000);
    private final AtomicInteger recordIdGen = new AtomicInteger(3000);

    @Override
    public Integer startSession(Integer userId, Integer planId) {
        TrainingSession s = new TrainingSession();
        s.setId(sessionIdGen.incrementAndGet());
        s.setPlanId(planId); 
        s.setStatus("W toku");
        sessions.add(s);
        return s.getId();
    }

    @Override
    public void finishSession(Integer sessionId, String duration) {
        sessions.stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .ifPresent(s -> {
                    s.setDuration(duration);
                    s.setStatus("Zakonczona");
                });
    }

    @Override
    public boolean addSessionRecord(SessionRecord record) {
        record.setId(recordIdGen.incrementAndGet());
        records.add(record);
        return true;
    }
    
    @Override
    public List<SessionRecord> getSessionRecords(int sessionId) {
         return records.stream()
                .filter(r -> r.getSessionId() == sessionId)
                .collect(Collectors.toList());
    }

    @Override
    public void addNote(Note note) {
        notes.add(note);
    }

    @Override
    public List<Note> getSessionNotes(int sessionId) {
         return notes.stream()
                .filter(n -> n.getSessionId() == sessionId)
                .collect(Collectors.toList());
    }

    private String getStatus(TrainingSession s) {
        return "Zakonczona";
    }
}
