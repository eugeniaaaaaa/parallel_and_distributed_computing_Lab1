package lab1;

import java.util.concurrent.atomic.AtomicReference;

public class MichaelScottQueue<E> {
    private static class Node<E> {
        E item;
        AtomicReference<Node<E>> next;

        Node(E item, Node<E> next) {
            this.item = item;
            this.next = new AtomicReference<>(next);
        }
    }

    private final Node<E> dummy = new Node<>(null, null);
    private final AtomicReference<Node<E>> tail = new AtomicReference<>(dummy);
    private final AtomicReference<Node<E>> head = new AtomicReference<>(tail.get());

    public E poll() {
       while (true) {
           Node<E> first = head.get();
           Node<E> last = tail.get();
           Node<E> next = first.next.get();
           if (first == head.get()) {
               if (first == last) {
                   if (next == null) return null;
                   tail.compareAndSet(last, next);
               } else {
                   E item = next.item;
                   if (head.compareAndSet(first, next)) return item;
               }
           }
       }
    }

    public void offer(E item) {
        Node<E> newNode = new Node<>(item, null);

        while (true) {
            Node<E> curTail = tail.get();
            Node<E> tailNext = curTail.next.get();
            if (curTail == tail.get()) {
                if (tailNext != null) {
                    tail.compareAndSet(curTail, tailNext);
                } else {
                    if (curTail.next.compareAndSet(null, newNode)) {
                        tail.compareAndSet(curTail, newNode);
                        return;
                    }
                }
            }
        }
    }
}
