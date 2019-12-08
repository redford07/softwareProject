package example.todolistexample;

public interface ItemActionListener {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemSwipe(int position, int direction);
}
