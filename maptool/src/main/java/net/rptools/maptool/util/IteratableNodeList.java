package net.rptools.maptool.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IteratableNodeList implements Iterable<Node> {
	final NodeList nodeList;

	public IteratableNodeList(final NodeList _nodeList) {
		nodeList = _nodeList;
	}

	@Override
	public Iterator<Node> iterator() {
		return new Iterator<Node>() {
			private int index = -1;
			private Node lastNode = null;

			private boolean isCurrentReplaced() {
				return lastNode != null && index < nodeList.getLength() &&
						lastNode != nodeList.item(index);
			}

			@Override
			public boolean hasNext() {
				return index + 1 < nodeList.getLength() || isCurrentReplaced();
			}

			@Override
			public Node next() {
				if (hasNext()) {
					if (isCurrentReplaced()) {
						//  It got removed by a change in the DOM.
						lastNode = nodeList.item(index);
					} else {
						lastNode = nodeList.item(++index);
					}
					return lastNode;
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public Stream<Node> stream() {
		Spliterator<Node> spliterator = Spliterators.spliterator(iterator(), nodeList.getLength(), 0);
		return StreamSupport.stream(spliterator, false);
	}
}