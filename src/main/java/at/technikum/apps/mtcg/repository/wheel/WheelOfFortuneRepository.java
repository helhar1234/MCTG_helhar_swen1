package at.technikum.apps.mtcg.repository.wheel;

public interface WheelOfFortuneRepository {
    boolean hasUserSpun(String id);

    boolean saveSpin(String id);
}
