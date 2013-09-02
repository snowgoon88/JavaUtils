/**
 * 
 */
package utils.dataStructures.trees.thirdGenKD;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Itarator that access data in KdTree, data which is only in leaves.
 * 
 * @author alain.dutech@loria.fr
 */
public class KdTreeIterator<T> implements Iterator<T> {
	
	/** Store the nodes that have to be visited */
	LinkedList<KdNode<T>> _nodesToProcess;
	/** Current node to visit */
	KdNode<T> _currentNode;
	/** current index of data to give */
	int _currentIndex = 0;
	/** the next T data to give */
	T _next;
	
	/**
	 * The iterator will start with the KdNode 'tree'.
	 */
	public KdTreeIterator(KdTree<T> tree) {
		_nodesToProcess = new LinkedList<KdNode<T>>();
		_currentNode = tree;
		_currentIndex = 0;
		_next = null;
		lookForNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		if (_next != null) {
			return true;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public T next() {
		if (_next == null ) {
			throw new IllegalStateException("NearestNeighborIterator has reached end!");
		}
		T result = _next;
		lookForNext();
		return result;
	}
	/**
	 * Anticipate by looking for the next element to give. If none is 
	 * found, then _next is set to none and 'hasNext()' will answer 'false'.
	 */
	void lookForNext() {
//		if (_currentNode.left == null) {
//			System.out.println("KdTreeIterator.next() : LEAF / "+_nodesToProcess.size());
//		}
//		else {
//			System.out.println("KdTreeIterator.next() : LOOKING");
//		}
		// Find a leaf : ie.left == null
		while ( _currentNode.left != null) {
			_nodesToProcess.add( _currentNode.left );
			_nodesToProcess.add( _currentNode.right );
			_currentNode = _nodesToProcess.remove();
			_currentIndex = 0;
		}
		if( _currentIndex < _currentNode.size() ) {
//			System.out.println("KdTreeIterator.next() : index="+_currentIndex+ " / "+_currentNode.size());
			@SuppressWarnings("unchecked")
			T val = (T) _currentNode.data[_currentIndex];
			_currentIndex += 1;
			_next = val;
		}
		else if (_nodesToProcess.isEmpty() == false) {
//			System.out.println("KdTreeIterator.next() : REMOVE the next()");
			_currentNode = _nodesToProcess.remove();
			_currentIndex = 0;
			lookForNext();
		}
		else {
//			System.out.println("KdTreeIterator.next() : END REACHED");
			_next = null;
		}
	}
		

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
