import java.util.Random; // Import the Random class for generating random levels

// Implementation of a SkipList data structure
class SkipList {
    private static final int MAX_LEVEL = 16; // Define the maximum level for the skip list
    private final Node head; // Head node of the skip list
    private final Random random = new Random(); // Random number generator for level assignment

    // Node class representing an element in the skip list
    private static class Node {
        int value; // The value stored in the node
        Node[] next; // Array of pointers to the next nodes at different levels

        // Constructor to initialize a node with a given value and level
        Node(int value, int level) {
            this.value = value;
            this.next = new Node[level + 1]; // Allocate space for level+1 pointers
        }
    }

    // Constructor to initialize the skip list with a head node having the smallest possible value
    public SkipList() {
        head = new Node(Integer.MIN_VALUE, MAX_LEVEL);
    }

    // Insert a value into the skip list
    public void insert(int value) {
        Node[] update = new Node[MAX_LEVEL + 1]; // Array to store nodes that need to be updated
        Node current = head; // Start from the head node

        // Traverse the skip list from the topmost level to the lowest level
        for (int i = MAX_LEVEL; i >= 0; i--) {
            while (current.next[i] != null && current.next[i].value < value) {
                current = current.next[i]; // Move forward in the current level
            }
            update[i] = current; // Store the node at which insertion needs to happen
        }

        int level = randomLevel(); // Generate a random level for the new node
        Node newNode = new Node(value, level); // Create a new node

        // Insert the new node at the appropriate levels
        for (int i = 0; i <= level; i++) {
            if (update[i] == null) update[i] = head; // Ensure update[i] is not null
            newNode.next[i] = (update[i].next[i] != null) ? update[i].next[i] : null; // Link new node to the next node
            update[i].next[i] = newNode; // Update the previous node to point to the new node
        }
    }

    // Remove a value from the skip list
    public boolean remove(int value) {
        Node[] update = new Node[MAX_LEVEL + 1]; // Array to store nodes that need to be updated
        Node current = head; // Start from the head node

        // Traverse the skip list to locate the position of the node to be deleted
        for (int i = MAX_LEVEL; i >= 0; i--) {
            while (current.next[i] != null && current.next[i].value < value) {
                current = current.next[i]; // Move forward in the current level
            }
            update[i] = current; // Store the last node before the target value
        }

        if (current.next[0] == null) return false; // If the value is not found, return false

        current = current.next[0]; // Move to the actual node containing the value
        if (current != null && current.value == value) {
            // Update pointers at all levels where the node appears
            for (int i = 0; i <= MAX_LEVEL && update[i].next[i] == current; i++) {
                update[i].next[i] = (current.next[i] != null) ? current.next[i] : null;
            }
            return true; // Return true if deletion was successful
        }
        return false; // Return false if value was not found
    }

    // Search for a value in the skip list
    public boolean search(int value) {
        Node current = head; // Start from the head node

        // Traverse the skip list from the topmost level to the lowest level
        for (int i = MAX_LEVEL; i >= 0; i--) {
            while (current.next[i] != null && current.next[i].value < value) {
                current = current.next[i]; // Move forward in the current level
            }
        }

        current = current.next[0]; // Move to the lowest level to check the actual value
        return current != null && current.value == value; // Return true if value is found, otherwise false
    }

    // Generate a random level for a new node
    private int randomLevel() {
        int level = 0; // Start from level 0
        while (random.nextDouble() < 0.5 && level < MAX_LEVEL) {
            level++; // Increase level with probability 0.5
        }
        return level; // Return the generated level
    }
}
