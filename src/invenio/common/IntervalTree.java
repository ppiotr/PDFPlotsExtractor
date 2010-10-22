/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *   An implementation of the balanced BST - interval tree
 *    each interval is represented by a pair of integer numbers
 *    every interval has an object associated with it. The object is of the type T
 *    all the intervals are closed (touching intervals are considered intersecting)
 * @author piotr
 */
public class IntervalTree<IntervalObjectType> {

    public class IntervalTreeNode {

        public IntervalTreeNode parent;
        public IntervalTreeNode firstChild;
        public IntervalTreeNode secondChild;
        public List<IntervalObjectType> associatedObjects;
        public int number;
        public int intBeginning, intEnd; // describes the interval of a given node
        // either the span of the entire subtree
        // or in case of leaf teh stored interval
        public int height; // The height of the subtree starting with this node

        public void recalculateInterval() {
            if (this.firstChild != null) {
                this.intBeginning = this.firstChild.intBeginning;
            }

            if (this.secondChild != null) {
                this.intEnd = this.secondChild.intEnd;
            }
        }

        /**
         * Creates a node containing the same data as a given node... except links to teh parent and children
         * @param orig
         */
        public IntervalTreeNode(IntervalTreeNode orig) {
            this();
            this.number = orig.number;
            this.associatedObjects = new ArrayList<IntervalObjectType>(orig.associatedObjects);
            this.intBeginning = orig.intBeginning;
            this.intEnd = orig.intEnd;
        }

        public IntervalTreeNode() {
            this.firstChild = null;
            this.secondChild = null;
            this.number = 0;
            this.associatedObjects = new ArrayList<IntervalObjectType>();
            this.height = 0;
        }

        public IntervalTreeNode(int num) {
            /**
             *  Creates a node that describes a regular number
             */
            this();
            this.number = num;
        }

        public IntervalTreeNode(int beginning, int ending) {
            /**
             * Creates a node that represents a node which represents the interval
             */
            this();
            this.intBeginning = beginning;
            this.intEnd = ending;
            this.height = 1; // the height of this tree is always 1 ... this is a leaf
        }

        public boolean isNumberNode() {
            /**
             * Is this a number node ?
             * Number nodes always hae children.
             */
            return (this.firstChild != null) && (this.secondChild != null);
        }

        public void recalculateHeight() {
            this.height = 1;
            if (this.firstChild != null) {
                this.height = this.firstChild.height + 1;
            }

            if (this.secondChild != null && this.height < (this.secondChild.height + 1)) {
                this.height = this.secondChild.height + 1;
            }
        }
    };
    public IntervalTreeNode root;
    private int min;
    private int max;
    public Map<IntervalObjectType, int[]> intervalsStored;

    /**
     * Create an interval tree stroing intervals from the range (min, max)
     */
    public IntervalTree(int mi, int ma) {

        this.min = mi;
        this.max = ma;
        this.root = new IntervalTreeNode(min, max);
        this.intervalsStored = new HashMap<IntervalObjectType, int[]>();
    }

    /**
     * Create an instance being a copy of the given tree
     * @param original
     */
    public IntervalTree(IntervalTree original) {
        this.min = original.min;
        this.max = original.max;
        // now we copy the entire tree node by node
        Stack<IntervalTreeNode> nodesToProcess = new Stack<IntervalTreeNode>();
        HashMap<IntervalTreeNode, IntervalTreeNode> nodesCopies = new HashMap<IntervalTreeNode, IntervalTreeNode>();

        nodesToProcess.push(original.root);
        //  first pass -> create all the objects
        while (!nodesToProcess.empty()) {
            IntervalTreeNode currentNode = nodesToProcess.pop();
            if (currentNode.firstChild != null) {
                nodesToProcess.push(currentNode.firstChild);
            }
            if (currentNode.secondChild != null) {
                nodesToProcess.push(currentNode.secondChild);
            }
            nodesCopies.put(currentNode, new IntervalTreeNode(currentNode));
        }
        // second pass -> establish links between objects
        this.root = nodesCopies.get(original.root);
        nodesToProcess.push(original.root);
        while (!nodesToProcess.empty()) {
            IntervalTreeNode currentNode = nodesToProcess.pop();
            IntervalTreeNode currentCopyNode = nodesCopies.get(currentNode);

            if (currentNode.firstChild != null) {
                nodesToProcess.push(currentNode.firstChild);
                IntervalTreeNode firstChildNode = nodesCopies.get(currentNode.firstChild);
                currentCopyNode.firstChild = firstChildNode;
                firstChildNode.parent = currentCopyNode;
            }

            if (currentNode.secondChild != null) {
                nodesToProcess.push(currentNode.secondChild);
                IntervalTreeNode secondChildNode = nodesCopies.get(currentNode.secondChild);
                currentCopyNode.secondChild = secondChildNode;
                secondChildNode.parent = currentCopyNode;
            }
        }

        // now dealing with saved intervals
        this.intervalsStored = new HashMap<IntervalObjectType, int[]>(original.intervalsStored);
    }

    private void addInterval(IntervalTreeNode tmpRoot,
            int b, int e, IntervalObjectType data, Boolean avoidRotations) {
        /**
         * Adding new interval to the tree rooted in a given point
         */
        if (tmpRoot == null) {
            // something went wrong ! - let's add tis code to be able to sotp with the debugger
            return;
        }

        if (b == e) { // we do not like dots
            return;
        }

        if (e < b) {
            // we enforce the beginning of the interval to be before its end
            addInterval(tmpRoot, e, b, data, avoidRotations);
            return;
        }

        if (tmpRoot.intBeginning == b && tmpRoot.intEnd == e) {
            // in this case we have to add new reference to the txisting node
            tmpRoot.associatedObjects.add(data);
        }

        if ((tmpRoot.intBeginning != b || tmpRoot.intEnd != e) && tmpRoot.isNumberNode()) {

            if (tmpRoot.number <= b) {
                // the interval fits into the right branch entirely
                addInterval(tmpRoot.secondChild, b, e, data, avoidRotations);
                tmpRoot.recalculateHeight();
            }

            if (e <= tmpRoot.number) {
                // the interval fits into the left branch entirely
                addInterval(tmpRoot.firstChild, b, e, data, avoidRotations);
                tmpRoot.recalculateHeight();
            }

            if (b < tmpRoot.number && tmpRoot.number < e) {
                // we have to split hte interval
                if (b != tmpRoot.number) {
                    addInterval(tmpRoot.firstChild, b, tmpRoot.number, data, avoidRotations);
                }

                if (e != tmpRoot.number) {
                    addInterval(tmpRoot.secondChild, tmpRoot.number, e, data, avoidRotations);
                }
                tmpRoot.recalculateHeight();
            }
        }

        if ((tmpRoot.intBeginning != b || tmpRoot.intEnd != e) && !tmpRoot.isNumberNode()) {
            // we have to split the current interval in order to add a new one
            if (b > tmpRoot.intBeginning && e < tmpRoot.intEnd) {
                // we are completely inside the new interval
                IntervalTreeNode nodeA = new IntervalTreeNode(tmpRoot.intBeginning, b);
                IntervalTreeNode nodeB = new IntervalTreeNode(b, e);
                nodeB.associatedObjects.add(data);
                IntervalTreeNode nodeC = new IntervalTreeNode(e, tmpRoot.intEnd);

                IntervalTreeNode nodeBeg = new IntervalTreeNode(b);
                IntervalTreeNode nodeEnd = new IntervalTreeNode(e);

                //
                // now connecting nodes in a tree structure
                // tmpRoot -> disconnected, all the attached objects go to nodeBeg
                //             nodeBeg
                //            /       \
                //        nodeA        nodeEnd
                //                    /       \
                //                nodeB       nodeC

                nodeBeg.associatedObjects = tmpRoot.associatedObjects;
                nodeBeg.parent = tmpRoot.parent;
                if (tmpRoot == this.root) {
                    this.root = nodeBeg;
                } else {
                    if (tmpRoot.parent.firstChild == tmpRoot) {
                        tmpRoot.parent.firstChild = nodeBeg;
                    } else {
                        tmpRoot.parent.secondChild = nodeBeg;
                    }
                }

                nodeBeg.firstChild = nodeA;
                nodeA.parent = nodeBeg;

                nodeBeg.secondChild = nodeEnd;
                nodeEnd.parent = nodeBeg;

                nodeEnd.firstChild = nodeB;
                nodeB.parent = nodeEnd;

                nodeEnd.secondChild = nodeC;
                nodeC.parent = nodeEnd;

                nodeEnd.recalculateHeight();
                nodeBeg.recalculateHeight();

                nodeEnd.recalculateInterval();
                nodeBeg.recalculateInterval();
            }

            if ((b == tmpRoot.intBeginning && e < tmpRoot.intEnd)
                    || (b > tmpRoot.intBeginning && e == tmpRoot.intEnd)) {
                // only two new elementary intervals
                IntervalTreeNode nodeA, nodeB, nodeEnd;
                if (b == tmpRoot.intBeginning) {
                    nodeA = new IntervalTreeNode(tmpRoot.intBeginning, e);
                    nodeB = new IntervalTreeNode(e, tmpRoot.intEnd);
                    nodeEnd = new IntervalTreeNode(e);
                    nodeA.associatedObjects.add(data);
                } else {
                    nodeA = new IntervalTreeNode(tmpRoot.intBeginning, b);
                    nodeB = new IntervalTreeNode(b, tmpRoot.intEnd);
                    nodeEnd = new IntervalTreeNode(b);
                    nodeB.associatedObjects.add(data);
                }
                // connecting new nodes:
                //              nodeEnd
                //             /       \
                //         nodeA      nodeB
                // tmpRoot -> detach and copy all the attached objects to the nodeEnd as it covers the same intervals

                nodeEnd.associatedObjects = tmpRoot.associatedObjects;
                nodeEnd.parent = tmpRoot.parent;

                if (tmpRoot == this.root) {
                    this.root = nodeEnd;
                } else {
                    if (tmpRoot.parent.firstChild == tmpRoot) {
                        tmpRoot.parent.firstChild = nodeEnd;
                    } else {
                        tmpRoot.parent.secondChild = nodeEnd;
                    }
                }
                nodeEnd.firstChild = nodeA;
                nodeA.parent = nodeEnd;

                nodeEnd.secondChild = nodeB;
                nodeB.parent = nodeEnd;

                nodeEnd.recalculateHeight();
                nodeEnd.recalculateInterval();
            }

            if (b == tmpRoot.intBeginning && e == tmpRoot.intEnd) {
                // we already have exactly this interval
                tmpRoot.associatedObjects.add(data);
            }
        }
        // now rebalancing the tree... before returning tothe upper call of the
        // function, we perform a neceessary rotation
        tmpRoot.recalculateInterval();
        if (!avoidRotations) {
            this.performNecessaryRotations(tmpRoot);
        }
    }

//        void addInterval
    public void addInterval(
            int b, int e, IntervalObjectType data, Boolean avoidRotations) {
        if (b != e) {
            this.addInterval(this.root, b, e, data, avoidRotations);
            int[] val = new int[]{b, e};
            if (val == null) {
                System.out.println("Epic failure ! ");
            }
            this.intervalsStored.put(data, val);
        }
    }

    /**
     * add the interval to the tree
     *
     * @param b    - the beginning of teh interval
     * @param e    - the ending of the interval
     * @param data - data associated with the interval
     */
    public void addInterval(
            int b, int e, IntervalObjectType data) {
        addInterval(b, e, data, false);
    }

    /**
     * Removal of the interval from the tree. Each interval is identified by its beginning,
     * end and associated data.
     *
     * Principle of operation:
     * Step down recursively into subtrees until the node describes an interval.
     * Each descent is connected with a potential splitting of the interval into parts.
     * (maximum 2 tree depths of splitting, what is trivial to prove).
     *
     * When encountered an exactly matching node, remove object from teh reference list
     * and return.
     *
     * After returning from the recurrent call, check if node has 2 empty leafs as children
     * if so, remove the node with children and replace with one empty leaf node describing the
     * same interval as the internal node.
     *
     * If no two leaf children, check if any rotations are necessary, perform them and return.
     * There might be an additional type of rotation necessary -> in case, we shrinked by 2 !
     *           A            If in this situation we remove the interval consisting of l.2, l.3
     *        /     \         the tree will recurentl y shrink to a single simple interval (not father
     *      B         C       because in eery correct tree, the maximal difference of heights of a subtree is 1
     *    /  \       /  \     in this case, the parrent of A will need a special treatment, that is a double simple rotation
     * l.1   l.2   l3    l4
     * @param operationRoot - node from which we request the removal
     * @param b the beginning of teh interval
     * @param e the ending of the interval
     * @param data the data associated with the interval - to be removed from the reference list
     *
     */
    public void removeInterval(IntervalTreeNode operationRoot, int b, int e, IntervalObjectType data) {

        if (operationRoot.intBeginning == b && operationRoot.intEnd == e) {
            // we have found the exact node
            operationRoot.associatedObjects.remove(data); // removes only the 1st occurance !
        } else {
            // we have to descent deeper ... if we did not ma

            if (e <= operationRoot.number) {
                // entirely left subtree
                removeInterval(operationRoot.firstChild, b, e, data);
            }

            if (b >= operationRoot.number) {
                // entirely right subtree
                removeInterval(operationRoot.secondChild, b, e, data);
            }

            if (b < operationRoot.number && e > operationRoot.number) {
                // we have to descend into both subtrees
                removeInterval(operationRoot.firstChild, b, operationRoot.number, data);
                removeInterval(operationRoot.secondChild, operationRoot.number, e, data);
            }

            // performing the maintenance - rotations, compactifications

            if ((!operationRoot.firstChild.isNumberNode()) && (!operationRoot.secondChild.isNumberNode())
                    && operationRoot.firstChild.associatedObjects.isEmpty() && operationRoot.secondChild.associatedObjects.isEmpty()) {
                // parent for two empty leafs.... compactifying

                IntervalTreeNode newNode = new IntervalTreeNode(operationRoot.intBeginning, operationRoot.intEnd);
                newNode.associatedObjects = operationRoot.associatedObjects;
                newNode.parent = operationRoot.parent;
                if (this.root == operationRoot) {
                    this.root = newNode;
                } else {
                    if (operationRoot.parent.firstChild == operationRoot) {
                        operationRoot.parent.firstChild = newNode;
                    } else {
                        operationRoot.parent.secondChild = newNode;
                    }
                }
            } else {
                balanceSubtree(operationRoot);
            }
        }
    }

    /**
     * Removes an interval from the entire tree. This call is a shortcut for
     * removing from the tree root.
     *
     * @param b the beginning of the interval
     * @param e the ending of the interval
     * @param data the data associated with the interval
     */
    public void removeInterval(int b, int e, IntervalObjectType data) {
        removeInterval(this.root, b, e, data);
        this.intervalsStored.remove(data);
    }

    public HashMap<IntervalObjectType, TreeMap<Integer, int[]>> collectIntervalsFromNodes(List<IntervalTreeNode> nodes) {
        HashMap<IntervalObjectType, TreeMap<Integer, int[]>> intervals =
                new HashMap<IntervalObjectType, TreeMap<Integer, int[]>>();

        for (IntervalTreeNode curNode : nodes) {
            for (IntervalObjectType curObj : curNode.associatedObjects) {
                if (!intervals.containsKey(curObj)) {
                    intervals.put(curObj, new TreeMap<Integer, int[]>());
                }

                TreeMap<Integer, int[]> ints = intervals.get(curObj);

                if (ints.containsKey(curNode.intBeginning)) {
                    // this should not happen if every added interval had a different identifier
                    int end = (curNode.intEnd > ints.get(curNode.intBeginning)[1]) ? curNode.intEnd : ints.get(curNode.intBeginning)[1];
                    ints.put(curNode.intBeginning, new int[]{curNode.intBeginning, end});
                }

                ints.put(curNode.intBeginning, new int[]{curNode.intBeginning, curNode.intEnd});
            }
        }

        return intervals;
    }

    /**
     * After performing a tree rotation, it might happen that the intervals
     * associated with moved nodes have to move.
     * Only the intervals from potentialy moved nodes have to change !
     * (in the notation from this implementation, those marked as capital
     *  letters and as stN, where N is a number).
     * This function fixes the interval associations. the arguments are:
     * @param nodes - a list of affected tree nodes
     * @param rotationRoot - the highest affected node (after rotation, not before !!!)
     */
    public void repairIntervalsAfterRotation(HashMap<IntervalObjectType, TreeMap<Integer, int[]>> intervals,
            List<IntervalTreeNode> nodes, IntervalTreeNode rotationRoot) {
        // now reseting the associated intervals in affected nodes
        for (IntervalTreeNode curNode : nodes) {
            curNode.associatedObjects = new ArrayList<IntervalObjectType>();
        }

        // now adding the intervals to the tree


        for (IntervalObjectType objectData : intervals.keySet()) {
            // connecting intervals having the same key
            TreeMap<Integer, int[]> ints = intervals.get(objectData);
            Boolean dbgIsAdded = false;

            do {
                Integer firstBeginning = ints.firstKey();
                int[] currentInt = ints.get(firstBeginning);
                ints.remove(firstBeginning);
                // extending as much as possible
                while (!ints.isEmpty() && ints.firstKey() <= currentInt[1]) {
                    Integer currentBeginning = ints.firstKey();
                    currentInt[1] = ints.get(currentBeginning)[1];
                    ints.remove(currentBeginning);
                }
                if (dbgIsAdded) {
                    Random r = new Random();
                    int caseIdentifier = r.nextInt(1000);

                    System.out.println("" + caseIdentifier + " we have a fragmented interval ! rotated node " + rotationRoot.number + " the fragmented interval id is " + objectData);


                    try {
                        Images.writeImageToFile(this.renderTree(), "c:\\intervalTrees\\fragmented_interval_" + caseIdentifier + ".png");
                    } catch (IOException ex) {
                        System.out.println("Epic failure !");
                    }
                }
                this.addInterval(rotationRoot, currentInt[0], currentInt[1], objectData, true);
                dbgIsAdded = true;
            } while (!ints.isEmpty());
        }
    }

    // now functions performing the tree rotations
    public boolean isRotationANecessary(IntervalTreeNode rotationRoot) {
        int depthSt1 = 0;
        int depthSt3 = 0;

        if (rotationRoot.firstChild != null && rotationRoot.firstChild.firstChild != null) {
            depthSt1 = rotationRoot.firstChild.firstChild.height;
        } else {
            return false;
        }

        if (rotationRoot.secondChild != null) {
            depthSt3 = rotationRoot.secondChild.height;
        }
        if (depthSt3 < depthSt1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  Performing a rotation of the following type:
     *
     *          A                    B
     *        /   \                /   \
     *       B     st3    ===>   st1    A
     *      / \                        /  \
     *    st1  st2                   st2  st3
     *
     * this roattion is useful in case, the depth of st1 increased causing inbalance.
     * it can be also used if both st1 and st2 increased (in this case the total depth increases)
     *
     * @return The tree node replacing rotationRoot
     */
    public IntervalTreeNode rotationA(IntervalTreeNode rotationRoot) {

        IntervalTreeNode A = rotationRoot;
        IntervalTreeNode B = rotationRoot.firstChild;
        IntervalTreeNode st1 = B.firstChild;
        IntervalTreeNode st2 = B.secondChild;
        IntervalTreeNode st3 = A.secondChild;

//        assert isTreeNodeCorrect(A);
//        assert isTreeNodeCorrect(B);
//        assert isTreeNodeCorrect(st1);
//        assert isTreeNodeCorrect(st2);
//        assert isTreeNodeCorrect(st3);
        // st3 remains at the same place

        // changing the place, where B is attached as a child
        if (A.parent != null) {
            if (A.parent.firstChild == A) {
                A.parent.firstChild = B;
            } else {
                A.parent.secondChild = B;
            }
        } else {
            this.root = B;
        }
        B.parent = A.parent;
        // establishing a parential relation between A and B
        B.secondChild = A;
        A.parent = B;
        //attaching subtrees
        B.firstChild = st1;
        st1.parent = B;

        A.firstChild = st2;
        st2.parent = A;

        //recalculating the depths ... all the way into the tree root
        IntervalTreeNode currentNode = A;

        while (currentNode != null) {
            currentNode.recalculateHeight();
            currentNode = currentNode.parent;
        }

        HashMap<IntervalObjectType, TreeMap<Integer, int[]>> ints =
                collectIntervalsFromNodes(Arrays.asList(A, B, st1, st2, st3));
        A.recalculateInterval();
        B.recalculateInterval();

        repairIntervalsAfterRotation(ints, Arrays.asList(A, B, st1, st2, st3), B);

//        assert isTreeNodeCorrect(A);
//        assert isTreeNodeCorrect(B);
//        assert isTreeNodeCorrect(st1);
//        assert isTreeNodeCorrect(st2);
//        assert isTreeNodeCorrect(st3);

        return B;
    }

    public boolean isRotationBNecessary(IntervalTreeNode rotationRoot) {
        int depthSt1 = 0;
        int depthSt3 = 0;

        if (rotationRoot.secondChild != null && rotationRoot.secondChild.secondChild != null) {
            depthSt3 = rotationRoot.secondChild.secondChild.height;
        } else {
            return false;
        }

        if (rotationRoot.firstChild != null) {
            depthSt1 = rotationRoot.firstChild.height;
        }

        if (depthSt1 < depthSt3) {
            return true;
        } else {
            return false;
        }

    }

    /**
     *  Performing a rotation of the following type:
     *
     *          A                      B
     *        /   \                  /   \
     *      st1    B    ===>        A    st3
     *            /  \            /  \
     *           st2  st3        st1  st2
     *
     * this roattion is useful in case, the depth of st3 increased causing inbalance.
     * it can be also used if both st2 and st3 increased (in this case the total depth increases)
     * @return the tree node replacing the rotation root (B)
     */
    private IntervalTreeNode rotationB(IntervalTreeNode rotationRoot) {
        IntervalTreeNode A = rotationRoot;
        IntervalTreeNode B = rotationRoot.secondChild;
        IntervalTreeNode st1 = A.firstChild;
        IntervalTreeNode st2 = B.firstChild;
        IntervalTreeNode st3 = B.secondChild;

//        assert isTreeNodeCorrect(A);
//        assert isTreeNodeCorrect(B);
//        assert isTreeNodeCorrect(st1);
//        assert isTreeNodeCorrect(st2);
//        assert isTreeNodeCorrect(st3);


        // st3 remains at the same place

        // changing the place, where B is attached as a child
        if (A.parent != null) {
            if (A.parent.firstChild == A) {
                A.parent.firstChild = B;
            } else {
                A.parent.secondChild = B;
            }
        } else {
            this.root = B;
        }
        B.parent = A.parent;
        // establishing a parential relation between A and B
        B.firstChild = A;
        A.parent = B;
        //attaching subtrees
        A.secondChild = st2;
        st2.parent = A;
        B.secondChild = st3;
        st3.parent = B;
        //recalculating the depths ... all the way into the tree root
        IntervalTreeNode currentNode = A;

        while (currentNode != null) {
            currentNode.recalculateHeight();
            currentNode = currentNode.parent;
        }
        HashMap<IntervalObjectType, TreeMap<Integer, int[]>> ints =
                collectIntervalsFromNodes(Arrays.asList(A, B, st1, st2, st3));

        A.recalculateInterval();
        B.recalculateInterval();

        repairIntervalsAfterRotation(ints, Arrays.asList(A, B, st1, st2, st3), B);

//        assert isTreeNodeCorrect(A);
//        assert isTreeNodeCorrect(B);
//        assert isTreeNodeCorrect(st1);
//        assert isTreeNodeCorrect(st2);
//        assert isTreeNodeCorrect(st3);

        return B;
    }

    public boolean isRotationCNecessary(IntervalTreeNode rotationRoot) {
        int depthSt1 = 0;
        int depthC = 0;
        int depthSt4 = 0;

        if (rotationRoot.secondChild != null && rotationRoot.secondChild.firstChild != null) {
            depthC = rotationRoot.secondChild.firstChild.height;
        } else {
            return false;
        }

        if (rotationRoot.firstChild != null) {
            depthSt1 = rotationRoot.firstChild.height;
        } else {
            return false;
        }

        if (rotationRoot.secondChild.secondChild != null) {
            depthSt4 = rotationRoot.secondChild.secondChild.height;
        }

        if (depthSt1 < depthC) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  Performing a rotation of the following type:
     *
     *          A                       C
     *        /   \                  /     \
     *      st1    B    ===>        A        B
     *            /  \            /   \    /   \
     *           C  st4        st1   st2  st3  st4
     *         /  \
     *       st2  st3
     * This rotation is useful when only the tree rooted in C grew, making the tree inbalanced
     * @return The tree node replacing the rotation root (C)
     */
    private IntervalTreeNode rotationC(IntervalTreeNode rotationRoot) {

        IntervalTreeNode A = rotationRoot;
        IntervalTreeNode B = A.secondChild;
        IntervalTreeNode C = B.firstChild;

        // only st2 and st3 move
        IntervalTreeNode st1 = A.firstChild;
        IntervalTreeNode st4 = B.secondChild;

        IntervalTreeNode st2 = C.firstChild;
        IntervalTreeNode st3 = C.secondChild;

//        assert isTreeNodeCorrect(A);
//        assert isTreeNodeCorrect(B);
//        assert isTreeNodeCorrect(C);
//
//        assert isTreeNodeCorrect(st1);
//        assert isTreeNodeCorrect(st2);
//        assert isTreeNodeCorrect(st3);
//        assert isTreeNodeCorrect(st4);
        // changing the place, where B is attached as a child
        if (A.parent != null) {
            if (A.parent.firstChild == A) {
                A.parent.firstChild = C;
            } else {
                A.parent.secondChild = C;
            }
        } else {
            this.root = C;
        }
        C.parent = A.parent;

        // establishing a parential relation between A and C
        C.firstChild = A;
        A.parent = C;
        C.secondChild = B;
        B.parent = C;

        //attaching subtrees
        A.secondChild = st2;
        st2.parent = A;

        B.firstChild = st3;
        st3.parent = B;

        //recalculating the depths ... all the way into the tree root
        B.recalculateHeight();
        IntervalTreeNode currentNode = A;
        while (currentNode != null) {
            currentNode.recalculateHeight();
            currentNode = currentNode.parent;
        }

        HashMap<IntervalObjectType, TreeMap<Integer, int[]>> ints =
                collectIntervalsFromNodes(Arrays.asList(A, B, C, st1, st2, st3, st4));

        A.recalculateInterval();
        B.recalculateInterval();
        C.recalculateInterval();

        repairIntervalsAfterRotation(ints, Arrays.asList(A, B, C, st1, st2, st3, st4), C);

//        assert isTreeNodeCorrect(A);
//        assert isTreeNodeCorrect(B);
//        assert isTreeNodeCorrect(C);
//
//        assert isTreeNodeCorrect(st1);
//        assert isTreeNodeCorrect(st2);
//        assert isTreeNodeCorrect(st3);
//        assert isTreeNodeCorrect(st4);
        return C;
    }

    public boolean isRotationDNecessary(IntervalTreeNode rotationRoot) {
        int depthSt1 = 0;
        int depthC = 0;
        int depthSt4 = 0;

        if (rotationRoot.firstChild != null && rotationRoot.firstChild.secondChild != null) {
            depthC = rotationRoot.firstChild.secondChild.height;
        } else {
            return false;
        }

        if (rotationRoot.secondChild != null) {
            depthSt4 = rotationRoot.secondChild.height;
        } else {
            return false;
        }

//        if (rotationRoot.secondChild.secondChild != null){
//            depthSt4 = rotationRoot.secondChild.secondChild.height;
//        }

        if (depthSt4 < depthC) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  Performing a rotation of the following type:
     *
     *          A                       C
     *        /   \                  /     \
     *       B    st4    ===>       B        A
     *      /  \                  /   \    /   \
     *    st1   C              st1   st2  st3  st4
     *         /  \
     *       st2   st3
     * This rotation is useful when only the tree rooted in C grew, making the tree inbalanced
     * @return The tree node replacing the rotation root (C)
     */
    private IntervalTreeNode rotationD(IntervalTreeNode rotationRoot) {
        IntervalTreeNode A = rotationRoot;
        IntervalTreeNode B = A.firstChild;
        IntervalTreeNode C = B.secondChild;

        // only st2 and st3 move
        IntervalTreeNode st1 = B.firstChild;
        IntervalTreeNode st4 = A.secondChild;

        IntervalTreeNode st2 = C.firstChild;
        IntervalTreeNode st3 = C.secondChild;


//        assert isTreeNodeCorrect(A);
//        assert isTreeNodeCorrect(B);
//        assert isTreeNodeCorrect(C);
//
//        assert isTreeNodeCorrect(st1);
//        assert isTreeNodeCorrect(st2);
//        assert isTreeNodeCorrect(st3);
//        assert isTreeNodeCorrect(st4);

        // changing the place, where B is attached as a child
        if (A.parent != null) {
            if (A.parent.firstChild == A) {
                A.parent.firstChild = C;
            } else {
                A.parent.secondChild = C;
            }
        } else {
            this.root = C;
        }
        C.parent = A.parent;

        // establishing a parential relation between A and C
        C.firstChild = B;
        B.parent = C;
        C.secondChild = A;
        A.parent = C;

        //attaching subtrees
        B.secondChild = st2;
        st2.parent = B;

        A.firstChild = st3;
        st3.parent = A;

        //recalculating the depths ... all the way into the tree root
        B.recalculateHeight();
        IntervalTreeNode currentNode = A;
        while (currentNode != null) {
            currentNode.recalculateHeight();
            currentNode = currentNode.parent;
        }

        HashMap<IntervalObjectType, TreeMap<Integer, int[]>> ints =
                collectIntervalsFromNodes(Arrays.asList(A, B, C, st1, st2, st3, st4));

        A.recalculateInterval();
        B.recalculateInterval();
        C.recalculateInterval();

        repairIntervalsAfterRotation(ints, Arrays.asList(A, B, C, st1, st2, st3, st4), C);

//        assert isTreeNodeCorrect(A);
//        assert isTreeNodeCorrect(B);
//        assert isTreeNodeCorrect(C);
//
//        assert isTreeNodeCorrect(st1);
//        assert isTreeNodeCorrect(st2);
//        assert isTreeNodeCorrect(st3);
//        assert isTreeNodeCorrect(st4);

        return C;
    }

    /**
     * Performing series of rotations on the tree and its subtrees ... balances only under certain circumstances (
     * there exists a wide rango of trees that will not be balaced by this algorithm.
     * 
     * The algorithm requires that the anomalies are visible already on the 1st level !
     * designed for having a single leaf attached to a bigger tree
     *
     * @return
     */
    public IntervalTreeNode balanceSubtree(IntervalTreeNode operationRoot) {
        IntervalTreeNode currentRoot = null;
        IntervalTreeNode newRoot = operationRoot;
        while (newRoot != currentRoot) {
            currentRoot = newRoot;
            newRoot = performNecessaryRotations(operationRoot);
            balanceSubtree(newRoot.firstChild);
            balanceSubtree(newRoot.secondChild);
        }
        return currentRoot;
    }

    /**
     * An internal funciton adding all the intercals intersecting with the current subtree
     * @param operationRoot
     * @param partialResults
     * @param b
     * @param e
     */
    private void addIntersectingIntervals(IntervalTreeNode operationRoot, Map<IntervalObjectType, Boolean> partialResults, int b, int e) {
        // if we are called, we intersects with this interval
        for (IntervalObjectType obj : operationRoot.associatedObjects) {
            if (!partialResults.containsKey(obj)) {
                partialResults.put(obj, false);
            }
            if (this.intervalsStored.get(obj) == null) {
                System.out.println("Epic failure !");
            }
        }

        // now we descend
        if (operationRoot.isNumberNode()) {
            if (e <= operationRoot.number) {
                addIntersectingIntervals(operationRoot.firstChild, partialResults, b, e);
            }

            if (b >= operationRoot.number) {
                addIntersectingIntervals(operationRoot.secondChild, partialResults, b, e);
            }

            if (b < operationRoot.number && e > operationRoot.number) {
                addIntersectingIntervals(operationRoot.firstChild, partialResults, b, operationRoot.number);
                addIntersectingIntervals(operationRoot.secondChild, partialResults, operationRoot.number, e);
            }
        }
    }

    /** returns all the intervals stored in the tree
     * @return map intervalObject -> boundary
     */
    public Map<IntervalObjectType, int[]> getAllIntervals() {
        // we want to return a shallow copy ! (because we do not know anything
        // about the creation of IntervalObjectType)
        return new HashMap<IntervalObjectType, int[]>(this.intervalsStored);
    }

    /**
     * Return a list of intervals intersecting teh current one
     * @param b beginnign of the interval to check
     * @param e ending of the interval to check
     * @return
     */
    public Map<IntervalObjectType, int[]> getIntersectingIntervals(int b, int e) {
        Map<IntervalObjectType, Boolean> partialResult = new HashMap<IntervalObjectType, Boolean>();
        addIntersectingIntervals(this.root, partialResult, b, e);
        // now adding real interval extents
        Map<IntervalObjectType, int[]> result = new HashMap<IntervalObjectType, int[]>();
        for (IntervalObjectType obj : partialResult.keySet()) {
            result.put(obj, this.intervalsStored.get(obj));
            if (this.intervalsStored.get(obj) == null) {
                try {
                    // something went wrong - maybe an interval has been removed from the structure but not completely from the tree ?
                    Images.writeImageToFile(this.renderTree(), "c:\\intervalTrees\\wrongStoredIntervals.png");
                } catch (IOException ex) {
                    Logger.getLogger(IntervalTree.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.print("WRONG INTERVAL!");
            }
        }
        return result;
    }

    /**
     * This function should be used only for testing the consistency of the tree.
     * It checks if the interval is present in the tree structure.
     * If the tree works correctly, it should be enought to look in the
     * associative table intervalsStored
     * @return
     */
    public Boolean isIntervalPresentInTree(int b, int e, IntervalObjectType data) {
        return this.isIntervalPresentInTree(this.root, b, e, data);
    }

    /**
     * A low-level version of the above
     * @param b
     * @param e
     * @param data
     * @return
     */
    public Boolean isIntervalPresentInTree(IntervalTreeNode currentNode, int b, int e, IntervalObjectType data) {
        if (b == currentNode.intBeginning && e == currentNode.intEnd) {
            return currentNode.associatedObjects.contains(data);
        }
        if (currentNode.isNumberNode()) {
            if (e <= currentNode.number) {
                return isIntervalPresentInTree(currentNode.firstChild, b, e, data);
            }
            if (b >= currentNode.number) {
                return isIntervalPresentInTree(currentNode.secondChild, b, e, data);
            }
            if (b < currentNode.number && e > currentNode.number) {
                return isIntervalPresentInTree(currentNode.firstChild, b, currentNode.number, data)
                        && isIntervalPresentInTree(currentNode.secondChild, currentNode.number, e, data);
            }
        }
        return false; // we are in the leaf which is not matching
    }

    /**
     * If necessary, performs a rotation on a node.
     * @param rotationRoot roor of the place that will be rotated
     * @return a node replacing the previous root
     */
    public IntervalTreeNode performNecessaryRotations(IntervalTreeNode rotationRoot) {
//        return rotationRoot;

        if (isRotationANecessary(rotationRoot)) {
            return rotationA(rotationRoot);
        }

        if (isRotationBNecessary(rotationRoot)) {
            return rotationB(rotationRoot);
        }

        if (isRotationCNecessary(rotationRoot)) {
            return rotationC(rotationRoot);
        }

        if (isRotationDNecessary(rotationRoot)) {
            return rotationD(rotationRoot);
        }
        return rotationRoot;
    }
    // debugging functions ... drawing the tree into a canvas... so that it can be displayed
    private final int nodeWidth = 300;
    private final int nodeHeight = 120;
    private final int nodeHorizontalSpacing = 50;
    private final int nodeVerticalSpacing = 70;
    private final int nodeMargin = 5;
    private final int nodeTextLineHeight = 15;

    private List<IntervalTreeNode> getTreeLeafs() {
        /** 
         *  return all teh elafs of the tree
         */
        List<IntervalTreeNode> leafs = new ArrayList<IntervalTreeNode>();

        Stack<IntervalTreeNode> processingStack = new Stack<IntervalTreeNode>();
        processingStack.push(this.root);

        while (!processingStack.empty()) {
            IntervalTreeNode currentNode = processingStack.pop();
            if (currentNode.isNumberNode()) {
                processingStack.push(currentNode.firstChild);
                processingStack.push(currentNode.secondChild);
            } else {
                leafs.add(0, currentNode);
            }
        }

        return leafs;
    }

    private int calculateTreeDepth(IntervalTreeNode curNode) {
        if (curNode.isNumberNode()) {
            int d1 = calculateTreeDepth(curNode.firstChild);
            int d2 = calculateTreeDepth(curNode.secondChild);

            return ((d1 > d2) ? d1 : d2) + 1;
        } else {
            return 1;
        }
    }

    private int calculateTreeDepth() {
        return calculateTreeDepth(this.root);
    }

    private Point2D.Double assignNodesPositions(IntervalTreeNode node,
            HashMap<IntervalTreeNode, Point2D.Double> nodeCenters) {
        if (nodeCenters.containsKey(node)) {
            return nodeCenters.get(node);
        }

        Point2D.Double fc = assignNodesPositions(node.firstChild, nodeCenters);
        Point2D.Double sc = assignNodesPositions(node.secondChild, nodeCenters);

        double x = (fc.x + sc.x) / 2;
        double y = ((fc.y > sc.y) ? sc.y : fc.y) - this.nodeVerticalSpacing - this.nodeHeight;
        Point2D.Double nodePosition = new Point2D.Double(x, y);
        nodeCenters.put(node, nodePosition);
        return nodePosition;
    }

    private void connectNodes(IntervalTreeNode node1, IntervalTreeNode node2, Graphics2D imageGraphics,
            HashMap<IntervalTreeNode, Point2D.Double> nodeCenters) {
        Point2D.Double c1 = nodeCenters.get(node1);
        Point2D.Double c2 = nodeCenters.get(node2);

        imageGraphics.drawLine((int) c1.x, (int) (c1.y + (this.nodeHeight / 2)),
                (int) c2.x, (int) (c2.y - (this.nodeHeight / 2)));
    }

    /**
     * Check a node for being correct
     * @param node
     */
    public Boolean isTreeNodeCorrect(IntervalTreeNode node) {
        if (node.parent == null) {
            if (node != this.root) {
                return false;
            }
        } else {
            if (node.parent.firstChild != node && node.parent.secondChild != node) {
                return false;
            }
        }
        if (node.firstChild != null) {
            if (node.firstChild.parent != node) {
                return false;
            }
        }
        if (node.secondChild != null) {
            if (node.secondChild.parent != node) {
                return false;
            }
        }
        if (node.isNumberNode()) {
            if (node.firstChild.intBeginning != node.intBeginning || node.secondChild.intEnd != node.intEnd) {
                return false;
            }
        }
        return true;
    }

    private void renderNode(IntervalTreeNode node, Graphics2D imageGraphics,
            HashMap<IntervalTreeNode, Point2D.Double> nodeCenters) {
        // first drawing connections with children

        if (node.firstChild != null) {
            this.connectNodes(node, node.firstChild, imageGraphics, nodeCenters);
        }
        if (node.secondChild != null) {
            this.connectNodes(node, node.secondChild, imageGraphics, nodeCenters);
        }

        // now drawing the node body
        Point2D.Double nC = nodeCenters.get(node);
        imageGraphics.drawRect((int) (nC.x - (this.nodeWidth / 2)), (int) (nC.y - (this.nodeHeight / 2)),
                this.nodeWidth, this.nodeHeight);

        // writing some text information
        int textStartingY = (int) (nC.y - (this.nodeHeight / 2) + this.nodeMargin + this.nodeTextLineHeight);
        int textStartningX = (int) (nC.x - (this.nodeWidth / 2) + this.nodeMargin);
        if (node.isNumberNode()) {
            imageGraphics.drawString("Number: " + node.number, textStartningX, textStartingY);
            textStartingY += this.nodeTextLineHeight;
        }

        imageGraphics.drawString("Interval: (" + node.intBeginning + ", " + node.intEnd + ")", textStartningX, textStartingY);
        textStartingY += this.nodeTextLineHeight;

        imageGraphics.drawString(node.associatedObjects.size() + " intervals", textStartningX, textStartingY);
        textStartingY += this.nodeTextLineHeight;
        // now drawing all teh strings
        String intervalsString = "";
        boolean first = true;
        for (IntervalObjectType obj : node.associatedObjects) {
            if (!first) {
                intervalsString += ", ";
            }
            intervalsString += obj.toString();
            first = false;
        }
        imageGraphics.drawString("contained intervals: " + intervalsString, textStartningX, textStartingY);
        textStartingY += this.nodeTextLineHeight;

    }

    public BufferedImage renderTree() {
        // calculating the image dimensions first
        List<IntervalTreeNode> treeLeafs = this.getTreeLeafs();
        int treeDepth = this.calculateTreeDepth();
        int width = (this.nodeWidth + this.nodeHorizontalSpacing) * treeLeafs.size();
        int height = (this.nodeHeight + this.nodeVerticalSpacing) * treeDepth;

        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D imageGraphics = (Graphics2D) resultImage.getGraphics();
        imageGraphics.fillRect(0, 0, width, height);
        imageGraphics.setBackground(Color.WHITE);
        imageGraphics.setColor(Color.BLACK);

        HashMap<IntervalTreeNode, Point2D.Double> nodeCenters = new HashMap<IntervalTreeNode, Point2D.Double>();
        // leafs are already ordered ! (we enforce this in the method getting them)
        int lastRowY = (treeDepth - 1) * (this.nodeHeight + this.nodeVerticalSpacing)
                + ((this.nodeVerticalSpacing + this.nodeHeight) / 2);
        int currentX = (int) (this.nodeHorizontalSpacing + this.nodeWidth) / 2;

        for (IntervalTreeNode treeLeaf : treeLeafs) {
            nodeCenters.put(treeLeaf, new Point2D.Double((double) currentX, (double) lastRowY));
            currentX += this.nodeWidth + this.nodeHorizontalSpacing;
        }
//        int dep1 = calculateTreeDepth(this.root.firstChild);
//        int dep2 = calculateTreeDepth(this.root.secondChild);
        assignNodesPositions(this.root, nodeCenters);

        // now we are really rendering the nodes and their relations

        for (IntervalTreeNode node : nodeCenters.keySet()) {
            this.renderNode(node, imageGraphics, nodeCenters);
        }
        // rendering some global tree information
        imageGraphics.drawString(" Total space: (" + this.min + ", " + this.max + ")", 10, 15);

        return resultImage;
    }
    private int ident = 0;

    private String getNextIdentifier() {
        ident++;
        return "_" + ident;
    }
}
