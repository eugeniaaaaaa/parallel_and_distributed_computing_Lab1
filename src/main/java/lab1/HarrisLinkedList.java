package lab1;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class HarrisLinkedList<E> implements SimpleList<E> {
    private static class Node<E> {
        final E key;
        final AtomicMarkableReference<Node<E>> next;

        Node(E key) {
            this.key = key;
            this.next = new AtomicMarkableReference<Node<E>>(null, false);
        }
    }

    private static class Window<T> {
        public Node<T> left_node;
        public Node<T> right_node;

        Window(Node<T> myPred, Node<T> myCurr) {
            left_node = myPred;
            right_node = myCurr;
        }
    }

    final Node<E> head;
    final Node<E> tail;

    public HarrisLinkedList() {
        tail = new Node<E>(null);
        head = new Node<E>(null);
        head.next.set(tail, false);
    }

    @Override
    public boolean add(E key) {
        final Node<E> newNode = new Node<E>(key);
        while (true) {
            final Window<E> window = find(key);
            final Node<E> left_node = window.left_node;
            final Node<E> right_node = window.right_node;
            if (right_node.key == key) {
                return false;
            } else {
                newNode.next.set(right_node, false);
                if (left_node.next.compareAndSet(right_node, newNode, false, false)) {
                    return true;
                }
            }
        }
    }

    @Override
    public boolean remove(E key) {
        while (true) {
            final Window<E> window = find(key);
            final Node<E> left_node = window.left_node;
            final Node<E> right_node = window.right_node;
            if (right_node.key != key) {
                return false;
            }
            final Node<E> succ = right_node.next.getReference();
            if (!right_node.next.compareAndSet(succ, succ, false, true)) {
                continue;
            }
            left_node.next.compareAndSet(right_node, succ, false, false);
            return true;
        }
    }

    @Override
    public boolean contains(E key) {
        boolean[] marked = {false};
        Node<E> right_node = head.next.getReference();
        right_node.next.get(marked);
        @SuppressWarnings("unchecked")
        final Comparable<? super E> keyComp = (Comparable<? super E>)key;
        while (right_node != tail && keyComp.compareTo(right_node.key) > 0) {
            right_node = right_node.next.getReference();
            right_node.next.get(marked);
        }
        return (keyComp.compareTo(right_node.key) == 0 && !marked[0]);
    }

    private Window<E> find(E key) {
        Node<E> left_node;
        Node<E> right_node;
        Node<E> succ;
        boolean[] marked = {false};
        @SuppressWarnings("unchecked")
        final Comparable<? super E> keyComp = (Comparable<? super E>)key;

        if (head.next.getReference() == tail) {
            return new Window<E>(head, tail);
        }

        retry:
        while (true) {
            left_node = head;
            right_node = left_node.next.getReference();
            while (true) {
                succ = right_node.next.get(marked);
                while (marked[0]) {
                    if (!left_node.next.compareAndSet(right_node, succ, false, false)) {
                        continue retry;
                    }
                    right_node = succ;
                    succ = right_node.next.get(marked);
                }

                if (right_node == tail || keyComp.compareTo(right_node.key) <= 0) {
                    return new Window<E>(left_node, right_node);
                }
                left_node = right_node;
                right_node = succ;
            }
        }
    }
}