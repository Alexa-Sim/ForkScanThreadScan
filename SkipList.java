import java.util.Random;

class SkipList {
    private static final int MAX_LEVEL = 16;
    private final Node head;
    private final Random random = new Random();

    private static class Node {
        int value;
        Node[] next;

        Node(int value, int level) {
            this.value = value;
            this.next = new Node[level + 1];
        }
    }

    public SkipList() {
        head = new Node(Integer.MIN_VALUE, MAX_LEVEL);
    }

    public void insert(int value) {
        Node[] update = new Node[MAX_LEVEL + 1];
        Node current = head;

        for (int i = MAX_LEVEL; i >= 0; i--) {
            while (current.next[i] != null && current.next[i].value < value) {
                current = current.next[i];
            }
            update[i] = current;
        }

        int level = randomLevel();
        Node newNode = new Node(value, level);

        for (int i = 0; i <= level; i++) {
            if (update[i] == null) update[i] = head; // Ensure update[i] is not null
            newNode.next[i] = (update[i].next[i] != null) ? update[i].next[i] : null;
            update[i].next[i] = newNode;
        }
    }

    public boolean remove(int value) {
        Node[] update = new Node[MAX_LEVEL + 1];
        Node current = head;

        for (int i = MAX_LEVEL; i >= 0; i--) {
            while (current.next[i] != null && current.next[i].value < value) {
                current = current.next[i];
            }
            update[i] = current;
        }

        if (current.next[0] == null) return false;

        current = current.next[0];
        if (current != null && current.value == value) {
            for (int i = 0; i <= MAX_LEVEL && update[i].next[i] == current; i++) {
                update[i].next[i] = (current.next[i] != null) ? current.next[i] : null;
            }
            return true;
        }
        return false;
    }

    public boolean search(int value) {
        Node current = head;

        for (int i = MAX_LEVEL; i >= 0; i--) {
            while (current.next[i] != null && current.next[i].value < value) {
                current = current.next[i];
            }
        }

        current = current.next[0];
        return current != null && current.value == value;
    }

    private int randomLevel() {
        int level = 0;
        while (random.nextDouble() < 0.5 && level < MAX_LEVEL) {
            level++;
        }
        return level;
    }
}