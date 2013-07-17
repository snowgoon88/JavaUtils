package utils.dataStructures.trees.thirdGenKD;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;




// Added by Thomas Moinel
//import appdev.ArmState;

/**
 * A node for KDTree where (point,data) are stored in Node.
 * Node are split if their capacity reach 'bucketCapacity'
 * 
 * From https://bitbucket.org/rednaxela/knn-benchmark/src
 * See also http://home.wlu.edu/~levys/software/kd
 *          http://robowiki.net/wiki/User:Rednaxela/kD-Tree
 */
class KdNode<T> {
	static DecimalFormat df3_2 = new DecimalFormat( "0.00" );
	// Competence TODO usefull ? Generic ?
	protected int fenetre;
	protected int nb_split = 10;
	protected double interest;
	protected boolean interest_valid;
	
    // All types
    protected int dimensions;
    protected int bucketCapacity;
    protected int size;

    // Leaf only
    protected double[][] points;
    protected Object[] data;

    // Stem only
    protected KdNode<T> left, right;
    protected int splitDimension;
    protected double splitValue;

    // Bounds
    protected double[] minBound, maxBound;
    protected boolean singlePoint;

    /**
     * Creation of node with nb of dimension of 'points' and max capacity
     * 
     * @param dimensions nbOfDimension of 'points'
     * @param bucketCapacity max capacity of a 'KdNode'
     */
    protected KdNode(int dimensions, int bucketCapacity) {
        // Init base
        this.dimensions = dimensions;
        this.bucketCapacity = bucketCapacity;
        this.size = 0;
        this.singlePoint = true;

        // Init leaf elements
        this.points = new double[bucketCapacity+1][];
        this.data = new Object[bucketCapacity+1];
    }

    /* -------- SIMPLE GETTERS -------- */

    public int size() {
        return size;
    }

    public boolean isLeaf() {
        return points != null;
    }

    /* -------- OPERATIONS -------- */

    public void addPoint(double[] point, T value) {
        KdNode<T> cursor = this;
        while (!cursor.isLeaf()) {
            cursor.extendBounds(point);
            cursor.size++;
            if (point[cursor.splitDimension] > cursor.splitValue) {
                cursor = cursor.right;
            }
            else {
                cursor = cursor.left;
            }
        }
        cursor.addLeafPoint(point, value);
    }

    /**
     * Dump points\t data\t unique_id in 'filename_pt.Rdata'
     * and bounds segments (x,y,x,y) to 'filename_bd.Rdata'.
     * Can be plotted using plotKdTree in R.
     * 
     * @param filename
     * @throws IOException
     */
    public void dumpFile( String filename ) 
    			throws IOException {
    	// Open file for writing points
    	FileWriter aFilePoints = new FileWriter( filename+"_pt.Rdata" );
    	BufferedWriter writerPoints = new BufferedWriter( aFilePoints );
    	// Open file for writing Bounds
    	FileWriter aFileBounds = new FileWriter( filename+"_bd.Rdata" );
    	BufferedWriter writerBounds = new BufferedWriter( aFileBounds );
    	
    	dumpFileStep( writerPoints, writerBounds, 1);
    	
    	writerBounds.close();
    	aFileBounds.close();
    	writerPoints.close();
    	aFilePoints.close();
    }
    /** Called by dumpFile */
    int dumpFileStep( BufferedWriter wPt, BufferedWriter wBd, int id)
    		throws IOException {
    	int newId = id;
    	if (points != null ) {
    		for (int i = 0; i < points.length; i++) {
    			if (points[i] != null ) {
    				for (int k = 0; k < dimensions; k++) {
						wPt.write(points[i][k]+"\t");
					}
    				wPt.write(data[i]+"\t");
    				wPt.write(newId+"\n");
    			}
    		}
    		newId += 1;
    	}
    	else {
    		// Info about split
    		if (splitDimension == 0) {
    			wBd.write(splitValue+"\t");
    			wBd.write(minBound[1]+"\t");
    			wBd.write(splitValue+"\t");
    			wBd.write(maxBound[1]+"\n");
			}
    		else {
    			wBd.write(minBound[0]+"\t");
    			wBd.write(splitValue+"\t");
    			wBd.write(maxBound[0]+"\t");
    			wBd.write(splitValue+"\n");
    		}
    	}
    	if (left != null) {
    		newId = left.dumpFileStep( wPt, wBd, newId);
    	}
    	if (right != null) {
    		newId = right.dumpFileStep( wPt, wBd, newId);
    	}
    	return newId;
    }
    /**
     * Display a KdNode and, recursively, its children.
     * 
     * @param margin For pretty printing
     */
    public void dumpDisplay(String margin) {
    	String mar = margin+"  ";
    	System.out.println(mar+"<Node>");
    	if (points != null ) {
    		for (int i = 0; i < points.length; i++) {
    			if (points[i] != null ) {
    				System.out.println(mar + "("+points[i][0]+", "+points[i][1] + ") : " + data[i]);
    			}
    			else {
    				System.out.println(mar + "null -> null");
    			}
    		}
    	}
    	else {
    		System.out.println(mar+"no_points");
    		System.out.println(mar+"dim="+splitDimension+"  val="+splitValue);
    		String str = "";
    		for (int i = 0; i < dimensions; i++) {
				str += "("+df3_2.format(minBound[i])+", "+df3_2.format(maxBound[i])+") ";
			}
    		System.out.println("Bounds = "+str);
    	}
    	if (left != null) {
    		left.dumpDisplay(margin+"--");
    	}
    	else {
    		System.out.println(mar + "NO_LEFT");
    	}
    	if (right != null) {
    		right.dumpDisplay(margin+"++");
    	}
    	else {
    		System.out.println(mar + "NO_RIGHT");
    	}
    }
    
    /* -------- INTERNAL OPERATIONS -------- */

    void addLeafPoint(double[] point, T value) {
        // Add the data point
    
        points[size] = point;
        data[size] = value;
        extendBounds(point);
        size++;

        if (size == points.length - 1) {
        	
        	// Thomas MOINEL
        	//splitRandom();
        	
            // If the node is getting too large
        	
            if (calculateSplit()) {
                // If the node successfully had it's split value calculated, split node
                splitLeafNode();
            } else {
                // If the node could not be split, enlarge node
                increaseLeafCapacity();
            }
        }
    }

    @SuppressWarnings("unused")
	private boolean checkBounds(double[] point) {
        for (int i = 0; i < dimensions; i++) {
            if (point[i] > maxBound[i]) return false;
            if (point[i] < minBound[i]) return false;
        }
        return true;
    }

    private void extendBounds(double[] point) {
        if (minBound == null) {
            minBound = Arrays.copyOf(point, dimensions);
            maxBound = Arrays.copyOf(point, dimensions);
            return;
        }

        for (int i = 0; i < dimensions; i++) {
            if (Double.isNaN(point[i])) {
                if (!Double.isNaN(minBound[i]) || !Double.isNaN(maxBound[i])) {
                    singlePoint = false;
                }
                minBound[i] = Double.NaN;
                maxBound[i] = Double.NaN;
            }
            else if (minBound[i] > point[i]) {
                minBound[i] = point[i];
                singlePoint = false;
            }
            else if (maxBound[i] < point[i]) {
                maxBound[i] = point[i];
                singlePoint = false;
            }
        }
    }

    private void increaseLeafCapacity() {
        points = Arrays.copyOf(points, points.length*2);
        data = Arrays.copyOf(data, data.length*2);
    }

    private boolean calculateSplit() {
        if (singlePoint) return false;

        double width = 0;
        for (int i = 0; i < dimensions; i++) {
            double dwidth = (maxBound[i] - minBound[i]);
            if (Double.isNaN(dwidth)) dwidth = 0;
            if (dwidth > width) {
                splitDimension = i;
                width = dwidth;
            }
        }

        if (width == 0) {
            return false;
        }

        // Start the split in the middle of the variance
        splitValue = (minBound[splitDimension] + maxBound[splitDimension]) * 0.5;

        // Never split on infinity or NaN
        if (splitValue == Double.POSITIVE_INFINITY) {
            splitValue = Double.MAX_VALUE;
        }
        else if (splitValue == Double.NEGATIVE_INFINITY) {
            splitValue = -Double.MAX_VALUE;
        }

        // Don't let the split value be the same as the upper value as
        // can happen due to rounding errors!
        if (splitValue == maxBound[splitDimension]) {
            splitValue = minBound[splitDimension];
        }

        // Success
        return true;
    }

    @SuppressWarnings("unchecked")
	private void splitLeafNode() {
        right = new KdNode<T>(dimensions, bucketCapacity);
        left = new KdNode<T>(dimensions, bucketCapacity);

        // Move locations into children
        for (int i = 0; i < size; i++) {
            double[] oldLocation = points[i];
            Object oldData = data[i];
            if (oldLocation[splitDimension] > splitValue) {
                right.addLeafPoint(oldLocation, (T) oldData);
            }
            else {
                left.addLeafPoint(oldLocation, (T) oldData);
            }
        }

        points = null;
        data = null;
    }
// **************************************************************************
// The following code was added by Thomas MOINEL for the first version
// of HumanArm testing.
// *********************
    
//	private void splitRandom() {
//		double qual = -1.0;
//		KdNode<T> r = right;
//		KdNode<T> l = left;
//
//		for (int i = 0; i < nb_split; i++) {
//			if (calculateSplitRandom()) {
//				double q = splitLeafNodeRandom();
//				if (q > qual) {
//					qual = q;
//					r = right;
//					l = left;
//				}
//			}
//		}
//
//		right = r;
//		left = l;
//
//		points = null;
//		data = null;
//	}
//
//	private boolean calculateSplitRandom() {
//		if (singlePoint)
//			return false;
//
//		int i = (int) Math.ceil(Math.random() * dimensions);
//
//		double dwidth = (maxBound[i] - minBound[i]);
//		if (Double.isNaN(dwidth) || dwidth == 0)
//			return false;
//
//		splitValue = (maxBound[splitDimension] - minBound[splitDimension])
//				* Math.random() + minBound[splitDimension];
//
//		// Never split on infinity or NaN
//		if (splitValue == Double.POSITIVE_INFINITY) {
//			splitValue = Double.MAX_VALUE;
//		} else if (splitValue == Double.NEGATIVE_INFINITY) {
//			splitValue = -Double.MAX_VALUE;
//		}
//
//		// Don't let the split value be the same as the upper value as
//		// can happen due to rounding errors!
//		if (splitValue == maxBound[splitDimension]) {
//			splitValue = minBound[splitDimension];
//		}
//
//		// Success
//		return true;
//	}

//	@SuppressWarnings("unchecked")
//	private double splitLeafNodeRandom() {
//		right = new KdNode<T>(dimensions, bucketCapacity);
//		left = new KdNode<T>(dimensions, bucketCapacity);
//
//		// Move locations into children
//		for (int i = 0; i < size; i++) {
//			double[] oldLocation = points[i];
//			Object oldData = data[i];
//			if (oldLocation[splitDimension] > splitValue) {
//				right.addLeafPoint(oldLocation, (T) oldData);
//			} else {
//				left.addLeafPoint(oldLocation, (T) oldData);
//			}
//		}
//
//		double a = right.getInterest() - left.getInterest();
//		double qual = right.size() * left.size() * (a * a);
//		return qual;
//	}
	
//	public double getInterest() {
//		if (!interest_valid) {
//			int debut = Math.max(size-fenetre, 0);
//			int milieu = Math.max(size-fenetre/2, size/2);
//			int fin = size;
//			interest = 0.0;
//			for (int i=debut; i < milieu; i++) {
//				interest += ((ArmState) data[i]).competence;
//			}
//			for (int i=milieu+1; i < fin; i++) {
//				interest -= ((ArmState) data[i]).competence;
//			}
//			interest = Math.abs(interest) / Math.min(fenetre, size);
//			
//			interest_valid = true;
//		}
//		return interest;
//	}
}
